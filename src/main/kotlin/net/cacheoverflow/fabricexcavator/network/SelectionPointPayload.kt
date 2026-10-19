// SPDX-FileCopyrightText: 2026 Cedric Hammes
// SPDX-License-Identifier: MIT

package net.cacheoverflow.fabricexcavator.network

import net.cacheoverflow.fabricexcavator.FabricExcavatorMod
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

/**
 * @author Cedric Hammes
 * @since  20/10/2026
 */
data class SelectionPointPayload(val pointIndex: Int, val pos: BlockPos) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        @JvmField
        val TYPE = CustomPacketPayload.Type<SelectionPointPayload>(FabricExcavatorMod.id("selection_point"))

        @JvmField
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, SelectionPointPayload> = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SelectionPointPayload::pointIndex,
            BlockPos.STREAM_CODEC,
            SelectionPointPayload::pos,
            ::SelectionPointPayload,
        )
    }
}
