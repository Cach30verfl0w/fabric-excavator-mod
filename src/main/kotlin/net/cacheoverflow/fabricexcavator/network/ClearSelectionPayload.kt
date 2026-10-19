// SPDX-FileCopyrightText: 2026 Cedric Hammes
// SPDX-License-Identifier: MIT

package net.cacheoverflow.fabricexcavator.network

import net.cacheoverflow.fabricexcavator.FabricExcavatorMod
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

/**
 * @author Cedric Hammes
 * @since  20/10/2026
 */
class ClearSelectionPayload private constructor() : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        @JvmField
        val INSTANCE = ClearSelectionPayload()

        @JvmField
        val TYPE = CustomPacketPayload.Type<ClearSelectionPayload>(FabricExcavatorMod.id("clear_selection"))

        @JvmField
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, ClearSelectionPayload> = StreamCodec.unit(INSTANCE)
    }
}
