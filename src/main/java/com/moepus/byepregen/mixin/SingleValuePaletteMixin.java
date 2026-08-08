package com.moepus.byepregen.mixin;

import com.moepus.byepregen.PaletteContainer.PaletteRawIdAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.SingleValuePalette;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = SingleValuePalette.class, remap = false)
public abstract class SingleValuePaletteMixin<T> implements PaletteRawIdAccess {
    @Shadow
    private T value;

    @Override
    public int byepregen$rawIdForLocalId(int localId) {
        T value = this.value;
        return localId == 0 && value instanceof BlockState state
                ? Block.BLOCK_STATE_REGISTRY.getId(state)
                : -1;
    }
}