// SPDX-FileCopyrightText: 2026 Cedric Hammes
// SPDX-License-Identifier: MIT

package net.cacheoverflow.fabricexcavator

import net.cacheoverflow.fabricexcavator.network.ClearSelectionPayload
import net.cacheoverflow.fabricexcavator.network.SelectionPointPayload
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.UUID

/**
 * Stores validated server selections and coordinates excavation work.
 *
 * Selection packets are treated as untrusted input. Each clicked point must be close to the player.
 * Completed regions are capped and must already be loaded before they can be stored.
 *
 * @author Cedric Hammes
 * @since  20/10/2026
 */
object ExcavationRegions {
    private const val MAX_SELECTION_DISTANCE_SQUARED = 64.0

    private data class ServerSelection(
        val first: BlockPos,
        val dimension: ResourceKey<Level>,
        val region: ExcavationRegion? = null,
    )

    private val serverSelections = mutableMapOf<UUID, ServerSelection>()
    private val calculatingDestroyProgress = ThreadLocal.withInitial { false }

    @Volatile
    private var clientRegionProvider: ((UUID) -> ExcavationRegion?)? = null

    fun setClientRegionProvider(provider: (UUID) -> ExcavationRegion?) {
        clientRegionProvider = provider
    }

    fun clear(playerId: UUID) {
        serverSelections.remove(playerId)
    }

    fun selectPoint(player: ServerPlayer, payload: SelectionPointPayload) {
        if (payload.pointIndex == 0) {
            serverSelections.remove(player.uuid)
            return
        }

        if (payload.pointIndex !in 1..2 || !isPointInReach(player, payload.pos)) {
            rejectSelection(player, "message.fabric-excavator-mod.selection_invalid")
            return
        }

        val dimension = player.level().dimension()
        if (payload.pointIndex == 1) {
            serverSelections[player.uuid] = ServerSelection(payload.pos.immutable(), dimension)
            return
        }

        val selection = serverSelections[player.uuid]
        if (selection == null || selection.dimension != dimension) {
            rejectSelection(player, "message.fabric-excavator-mod.selection_invalid")
            return
        }

        val region = ExcavationRegion.between(selection.first, payload.pos)
        if (region.blockCount > ExcavationRegion.MAX_BLOCK_COUNT) {
            rejectSelection(player, "message.fabric-excavator-mod.selection_too_large")
            return
        }

        val level = player.level()
        val allBlocksLoaded = region.positions().all(level::isLoaded)
        if (!allBlocksLoaded) {
            rejectSelection(player, "message.fabric-excavator-mod.selection_unloaded")
            return
        }

        serverSelections[player.uuid] = selection.copy(region = region)
    }

    /**
     * Returns the slowest destroy progress in the player's active region.
     *
     * The block-state method used here is also the mixin target. A thread-local guard allows the
     * nested vanilla calculations to finish without re-entering the regional calculation.
     */
    @JvmStatic
    fun adjustedDestroyProgress(player: Player, level: BlockGetter, pos: BlockPos): Float? {
        if (calculatingDestroyProgress.get()) {
            return null
        }

        val region = regionFor(player, level) ?: return null
        if (pos !in region) {
            return null
        }

        calculatingDestroyProgress.set(true)
        return try {
            region.positions()
                .map { candidate -> level.getBlockState(candidate) to candidate }
                .filterNot { (state, _) -> state.isAir }
                .minOfOrNull { (state, candidate) -> state.getDestroyProgress(player, level, candidate) }
        } finally {
            calculatingDestroyProgress.set(false)
        }
    }

    /**
     * Breaks the remaining blocks after the player finishes one block in a selected region.
     *
     * Every block goes through the normal server game-mode method. This preserves permission
     * checks, drops, enchantments, exhaustion, and tool durability for each position.
     */
    fun excavate(level: Level, player: Player, brokenPos: BlockPos) {
        if (level !is ServerLevel || player !is ServerPlayer) {
            return
        }

        val selection = serverSelections[player.uuid] ?: return
        val region = selection.region
        if (region == null) {
            clearAndSync(player)
            return
        }

        val isSameDimension = selection.dimension == level.dimension()
        if (!isSameDimension || brokenPos !in region) {
            clearAndSync(player)
            return
        }

        // Remove the region first so nested block-break events cannot start another excavation.
        clearAndSync(player)

        for (pos in region.positions()) {
            if (pos == brokenPos || level.getBlockState(pos).isAir) {
                continue
            }

            player.gameMode.destroyBlock(pos)
        }
    }

    private fun regionFor(player: Player, level: BlockGetter): ExcavationRegion? {
        if (level is ServerLevel) {
            val selection = serverSelections[player.uuid] ?: return null
            if (selection.dimension != level.dimension()) {
                return null
            }
            return selection.region
        }

        return clientRegionProvider?.invoke(player.uuid)
    }

    private fun isPointInReach(player: ServerPlayer, pos: BlockPos): Boolean {
        return player.distanceToSqr(Vec3.atCenterOf(pos)) <= MAX_SELECTION_DISTANCE_SQUARED
    }

    private fun rejectSelection(player: ServerPlayer, messageKey: String) {
        clearAndSync(player)
        val message = Component.translatable(messageKey)
        player.connection.send(ClientboundSetActionBarTextPacket(message))
    }

    private fun clearAndSync(player: ServerPlayer) {
        serverSelections.remove(player.uuid)
        ServerPlayNetworking.send(player, ClearSelectionPayload.INSTANCE)
    }
}
