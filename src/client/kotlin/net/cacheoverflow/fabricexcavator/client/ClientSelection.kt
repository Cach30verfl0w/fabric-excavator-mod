// SPDX-FileCopyrightText: 2026 Cedric Hammes
// SPDX-License-Identifier: MIT

package net.cacheoverflow.fabricexcavator.client

import net.cacheoverflow.fabricexcavator.ExcavationRegion
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import java.util.UUID

/**
 * @author Cedric Hammes
 * @since  20/10/2026
 */
object ClientSelection {
    var firstPoint: BlockPos? = null
        private set

    var region: ExcavationRegion? = null
        private set

    fun start(pos: BlockPos) {
        firstPoint = pos.immutable()
        region = null
    }

    fun complete(selection: ExcavationRegion) {
        region = selection
    }

    fun clear() {
        firstPoint = null
        region = null
    }

    fun regionFor(playerId: UUID): ExcavationRegion? {
        val localPlayerId = Minecraft.getInstance().player?.uuid
        if (playerId != localPlayerId) {
            return null
        }
        return region
    }
}
