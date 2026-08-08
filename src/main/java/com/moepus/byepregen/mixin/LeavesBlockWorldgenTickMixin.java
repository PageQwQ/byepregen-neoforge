package com.moepus.byepregen.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LeavesBlock.class,remap = false, priority = 500)
public abstract class LeavesBlockWorldgenTickMixin {
    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void c6c$skipStableWorldgenLeafTick(
            final BlockState state,
            final LevelReader level,
            final ScheduledTickAccess scheduledTickAccess,
            final BlockPos pos,
            final Direction direction,
            final BlockPos neighborPos,
            final BlockState neighborState,
            final RandomSource random,
            final CallbackInfoReturnable<BlockState> cir
    ) {
        if (!(level instanceof final WorldGenRegion worldGenRegion)) {
            return;
        }

        if (state.getValue(LeavesBlock.WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        cir.setReturnValue(state);
    }
}
