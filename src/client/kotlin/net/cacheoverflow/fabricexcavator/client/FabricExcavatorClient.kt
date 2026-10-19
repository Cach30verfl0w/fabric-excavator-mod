// SPDX-FileCopyrightText: 2026 Cedric Hammes
// SPDX-License-Identifier: MIT

package net.cacheoverflow.fabricexcavator.client

import com.mojang.blaze3d.platform.InputConstants
import net.cacheoverflow.fabricexcavator.ExcavationRegion
import net.cacheoverflow.fabricexcavator.ExcavationRegions
import net.cacheoverflow.fabricexcavator.FabricExcavatorMod
import net.cacheoverflow.fabricexcavator.network.ClearSelectionPayload
import net.cacheoverflow.fabricexcavator.network.SelectionPointPayload
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.client.KeyMapping
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
import net.minecraft.world.InteractionResult

/**
 * @author Cedric Hammes
 * @since  20/10/2026
 */
object FabricExcavatorClient : ClientModInitializer {
    private val selectKey = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.fabric-excavator-mod.select",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_V,
            "key.category.fabric-excavator-mod.key_category",
        ),
    )

    private var handledCurrentClick = false

    override fun onInitializeClient() {
        ExcavationRegions.setClientRegionProvider { uuid ->
            ClientSelection.regionFor(uuid)
        }

        AttackBlockCallback.EVENT.register { player, level, _, pos, _ ->
            val isSelectionClick = level.isClientSide && selectKey.isDown
            if (!isSelectionClick) {
                return@register InteractionResult.PASS
            }

            if (handledCurrentClick || player.isSpectator) {
                return@register InteractionResult.FAIL
            }

            handledCurrentClick = true
            selectBlock(pos)
            InteractionResult.FAIL
        }

        UseBlockCallback.EVENT.register { _, level, _, hitResult ->
            val isSelectionAction = level.isClientSide && selectKey.isDown
            val hasSelection = ClientSelection.firstPoint != null
            if (!isSelectionAction || !hasSelection) {
                return@register InteractionResult.PASS
            }

            ClientSelection.clear()
            ClientPlayNetworking.send(SelectionPointPayload(0, hitResult.blockPos))
            sendMessage("message.fabric-excavator-mod.selection_cleared")
            InteractionResult.FAIL
        }

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!client.options.keyAttack.isDown) {
                handledCurrentClick = false
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(ClearSelectionPayload.TYPE) { _, _ ->
            ClientSelection.clear()
        }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            ClientSelection.clear()
        }

        SelectionOverlay.register()
    }

    private fun selectBlock(pos: net.minecraft.core.BlockPos) {
        val first = ClientSelection.firstPoint
        if (first == null || ClientSelection.region != null) {
            ClientSelection.start(pos)
            ClientPlayNetworking.send(SelectionPointPayload(1, pos))
            sendMessage("message.fabric-excavator-mod.select_second")
            return
        }

        val region = ExcavationRegion.between(first, pos)
        if (region.blockCount > ExcavationRegion.MAX_BLOCK_COUNT) {
            ClientSelection.clear()
            ClientPlayNetworking.send(SelectionPointPayload(0, pos))
            sendMessage("message.fabric-excavator-mod.selection_too_large")
            return
        }

        ClientSelection.complete(region)
        ClientPlayNetworking.send(SelectionPointPayload(2, pos))
        sendMessage("message.fabric-excavator-mod.selection_complete", region.blockCount)
    }

    private fun sendMessage(key: String, vararg arguments: Any) {
        val minecraft = net.minecraft.client.Minecraft.getInstance()
        val connection = minecraft.connection ?: return
        val message = Component.translatable(key, *arguments)
        ClientboundSetActionBarTextPacket(message).handle(connection)
    }
}
