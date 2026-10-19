// SPDX-FileCopyrightText: 2026 Cedric Hammes
// SPDX-License-Identifier: MIT

package net.cacheoverflow.fabricexcavator.client

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.world.phys.AABB
import kotlin.math.sin

/**
 * @author Cedric Hammes
 * @since  20/10/2026
 */
object SelectionOverlay {
    private const val BOX_INFLATION = 0.002

    fun register() {
        WorldRenderEvents.AFTER_ENTITIES.register(::render)
    }

    /**
     * Draws the current selection as a softly pulsing box.
     *
     * The box is moved into camera-relative space before its vertices are submitted. A small
     * expansion keeps its surfaces from overlapping the selected block faces.
     */
    private fun render(context: WorldRenderContext) {
        val region = ClientSelection.region
        val firstPoint = ClientSelection.firstPoint
        if (region == null && firstPoint == null) {
            return
        }

        val selectionBox = if (region != null) {
            AABB(
                region.minX.toDouble(),
                region.minY.toDouble(),
                region.minZ.toDouble(),
                region.maxX + 1.0,
                region.maxY + 1.0,
                region.maxZ + 1.0,
            )
        } else {
            AABB(firstPoint ?: return)
        }
        val box = selectionBox.inflate(BOX_INFLATION)
        val pulse = ((sin(System.nanoTime() / 600_000_000.0) + 1.0) / 2.0).toFloat()
        val fillAlpha = 0.08f + pulse * 0.08f
        val outlineAlpha = 0.45f + pulse * 0.3f
        val red: Float
        val green: Float
        val blue: Float
        if (region != null) {
            red = 0.1f
            green = 0.75f
            blue = 1.0f
        } else {
            red = 1.0f
            green = 0.75f
            blue = 0.1f
        }
        val poseStack = context.matrixStack() ?: return
        val consumers = context.consumers() ?: return
        val camera = context.camera().position

        poseStack.pushPose()
        poseStack.translate(-camera.x, -camera.y, -camera.z)
        val fillBuffer = consumers.getBuffer(RenderType.debugFilledBox())
        ShapeRenderer.addChainedFilledBoxVertices(
            poseStack,
            fillBuffer,
            box.minX,
            box.minY,
            box.minZ,
            box.maxX,
            box.maxY,
            box.maxZ,
            red,
            green,
            blue,
            fillAlpha,
        )
        val outlineBuffer = consumers.getBuffer(RenderType.lines())
        ShapeRenderer.renderLineBox(
            poseStack,
            outlineBuffer,
            box,
            red,
            green,
            blue,
            outlineAlpha,
        )
        poseStack.popPose()
    }
}
