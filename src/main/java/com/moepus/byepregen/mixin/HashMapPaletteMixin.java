package com.moepus.byepregen.mixin;

import com.moepus.byepregen.PaletteContainer.PaletteRawIdAccess;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.HashMapPalette;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = HashMapPalette.class, remap = false)
public abstract class HashMapPaletteMixin<T> implements PaletteRawIdAccess {
    @Shadow
    @Final
    private CrudeIncrementalIntIdentityHashBiMap<T> values;

    @Override
    public int byepregen$rawIdForLocalId(int localId) {
        T value = this.values.byId(localId);
        return value instanceof BlockState state ? Block.BLOCK_STATE_REGISTRY.getId(state) : -1;
    }
}
