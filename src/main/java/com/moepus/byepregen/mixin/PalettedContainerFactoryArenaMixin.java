package com.moepus.byepregen.mixin;

import com.moepus.byepregen.PaletteContainer.ArenaPelette.Codecs.StateCodec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PalettedContainerFactory.class, remap = false)
public abstract class PalettedContainerFactoryArenaMixin {
    @Inject(method = "create", at = @At("RETURN"), cancellable = true)
    private static void byepregen$wrapBlockStateCodec(
            RegistryAccess registryAccess, CallbackInfoReturnable<PalettedContainerFactory> cir) {
        PalettedContainerFactory factory = cir.getReturnValue();
        cir.setReturnValue(new PalettedContainerFactory(
                factory.blockStatesStrategy(),
                factory.defaultBlockState(),
                new StateCodec(factory.blockStatesContainerCodec()),
                factory.biomeStrategy(),
                factory.defaultBiome(),
                factory.biomeContainerCodec()
        ));
    }
}
