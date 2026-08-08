package com.moepus.byepregen.mixin;

import com.moepus.byepregen.Config;
import com.moepus.byepregen.ConfigParser;
import com.moepus.byepregen.PaletteContainer.ArenaPelette.ArenaSectionMaterializer;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = LevelChunk.class, remap = false)
public abstract class LevelChunkArenaMixin {
    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void byepregen$materializeArenaSections(CallbackInfo ci) {
        byepregen$materializeIfNeeded();
    }

    @Unique
    private void byepregen$materializeIfNeeded() {
        Config config = ConfigParser.getConfig();
        if (!config.enableArenaPalette) {
            return;
        }

        if (!config.enableServerRuntimeArenaPalette) {
            ArenaSectionMaterializer.materializeChunk((LevelChunk) (Object) this);
        }
    }
}
