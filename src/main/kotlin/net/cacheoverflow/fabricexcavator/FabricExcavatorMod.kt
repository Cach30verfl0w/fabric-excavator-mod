// SPDX-FileCopyrightText: 2026 Cedric Hammes
// SPDX-License-Identifier: MIT

package net.cacheoverflow.fabricexcavator

import net.fabricmc.api.ModInitializer
import net.cacheoverflow.fabricexcavator.network.ClearSelectionPayload
import net.cacheoverflow.fabricexcavator.network.SelectionPointPayload
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Cedric Hammes
 * @since  20/10/2026
 */
object FabricExcavatorMod : ModInitializer {
    const val MOD_ID: String = "fabric-excavator-mod"
    private val logger: Logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        PayloadTypeRegistry.playC2S().register(SelectionPointPayload.TYPE, SelectionPointPayload.CODEC)
        PayloadTypeRegistry.playS2C().register(ClearSelectionPayload.TYPE, ClearSelectionPayload.CODEC)

        ServerPlayNetworking.registerGlobalReceiver(SelectionPointPayload.TYPE) { payload, context ->
            ExcavationRegions.selectPoint(context.player(), payload)
        }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            ExcavationRegions.clear(handler.player.uuid)
        }

        PlayerBlockBreakEvents.AFTER.register { level, player, pos, _, _ ->
            ExcavationRegions.excavate(level, player, pos)
        }

        logger.info("Fabric Excavator initialized")
    }

    fun id(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
}
