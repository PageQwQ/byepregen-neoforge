package com.moepus.byepregen.mixin;

import com.moepus.byepregen.PaletteContainer.BlockStateRawIdAccess;
import com.moepus.byepregen.PaletteContainer.PaletteRawIdAccess;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.Strategy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;

@Mixin(value = PalettedContainer.class, remap = false)
public abstract class PalettedContainerMixin<T> implements BlockStateRawIdAccess {
    @Shadow
    public volatile PalettedContainer.Data<T> data;

    @Shadow
    @Final
    public Strategy strategy;

    @Override
    public int getRawId(int x, int y, int z) {
        PalettedContainer.Data<T> data = this.data;
        int localId = data.storage().get(this.strategy.getIndex(x, y, z));
        Palette<T> palette = data.palette();
        if (palette instanceof PaletteRawIdAccess rawIdAccess) {
            return rawIdAccess.byepregen$rawIdForLocalId(localId);
        }
        throw new UnsupportedOperationException("Missing raw-id access for palette " + palette.getClass().getName());
    }
}
