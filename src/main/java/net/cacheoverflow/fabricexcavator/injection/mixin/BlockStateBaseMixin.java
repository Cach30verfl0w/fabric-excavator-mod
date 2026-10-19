// SPDX-FileCopyrightText: 2026 Cedric Hammes
// SPDX-License-Identifier: MIT

package net.cacheoverflow.fabricexcavator.injection.mixin;

import net.cacheoverflow.fabricexcavator.ExcavationRegions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Inject(method = "getDestroyProgress", at = @At("RETURN"), cancellable = true, require = 1)
    private void fabricExcavator$useHardestBlock(
        Player player,
        BlockGetter level,
        BlockPos pos,
        CallbackInfoReturnable<Float> callback
    ) {
        Float adjustedProgress = ExcavationRegions.adjustedDestroyProgress(player, level, pos);
        if (adjustedProgress != null) {
            callback.setReturnValue(adjustedProgress);
        }
    }
}
