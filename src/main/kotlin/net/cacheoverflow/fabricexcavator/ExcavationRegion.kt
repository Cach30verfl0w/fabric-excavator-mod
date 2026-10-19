// SPDX-FileCopyrightText: 2026 Cedric Hammes
// SPDX-License-Identifier: MIT

package net.cacheoverflow.fabricexcavator

import net.minecraft.core.BlockPos

/**
 * Describes an inclusive cuboid selection between two block positions.
 *
 * The coordinates are normalized when the region is created. Iterating the region always returns
 * immutable positions because Minecraft reuses mutable positions in several block iterators.
 *
 * @author Cedric Hammes
 * @since  20/10/2026
 */
class ExcavationRegion private constructor(
    val minX: Int,
    val minY: Int,
    val minZ: Int,
    val maxX: Int,
    val maxY: Int,
    val maxZ: Int,
) {
    val blockCount: Long =
        (maxX - minX + 1L) * (maxY - minY + 1L) * (maxZ - minZ + 1L)

    operator fun contains(pos: BlockPos): Boolean {
        val insideX = pos.x in minX..maxX
        val insideY = pos.y in minY..maxY
        val insideZ = pos.z in minZ..maxZ
        return insideX && insideY && insideZ
    }

    fun positions(): Sequence<BlockPos> = sequence {
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    yield(BlockPos(x, y, z))
                }
            }
        }
    }

    companion object {
        const val MAX_BLOCK_COUNT: Long = 4096

        fun between(first: BlockPos, second: BlockPos): ExcavationRegion = ExcavationRegion(
            minX = minOf(first.x, second.x),
            minY = minOf(first.y, second.y),
            minZ = minOf(first.z, second.z),
            maxX = maxOf(first.x, second.x),
            maxY = maxOf(first.y, second.y),
            maxZ = maxOf(first.z, second.z),
        )
    }
}
