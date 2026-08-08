package com.moepus.byepregen.mixin.yalight;

import com.llamalad7.mixinextras.sugar.Local;
import com.moepus.byepregen.yalight.YAChunkLightAccess;
import com.moepus.byepregen.yalight.YAChunkLightData;
import com.moepus.byepregen.yalight.YANibbleArray;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SerializableChunkData.class)
public abstract class ChunkSerializerYALightMixin {
    @Redirect(
            method = "copyOf",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/lighting/LayerLightEventListener;getDataLayerData(Lnet/minecraft/core/SectionPos;)Lnet/minecraft/world/level/chunk/DataLayer;",
                    ordinal = 0
            )
    )
    private static DataLayer byepregen$writeYABlockLight(
            LayerLightEventListener listener,
            SectionPos sectionPos,
            @Local(argsOnly = true) ChunkAccess chunk
    ) {
        return byepregen$visibleLayer(chunk, LightLayer.BLOCK, sectionPos.y());
    }

    @Redirect(
            method = "copyOf",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/lighting/LayerLightEventListener;getDataLayerData(Lnet/minecraft/core/SectionPos;)Lnet/minecraft/world/level/chunk/DataLayer;",
                    ordinal = 1
            )
    )
    private static DataLayer byepregen$writeYASkyLight(
            LayerLightEventListener listener,
            SectionPos sectionPos,
            @Local(argsOnly = true) ChunkAccess chunk
    ) {
        return byepregen$visibleLayer(chunk, LightLayer.SKY, sectionPos.y());
    }

    @Unique
    @Nullable
    private static DataLayer byepregen$visibleLayer(ChunkAccess chunk, LightLayer layer, int sectionY) {
        YAChunkLightData data = ((YAChunkLightAccess)chunk).byepregen$yaLightData(layer, false);
        YANibbleArray nibble = data == null ? null : data.getVisibleSection(sectionY);
        return nibble == null ? null : nibble.toVanilla();
    }

    @Unique
    private static final ThreadLocal<it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<DataLayer>> BYEPREGEN_BLOCK_LIGHT_CAPTURE =
            ThreadLocal.withInitial(it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap::new);

    @Unique
    private static final ThreadLocal<it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<DataLayer>> BYEPREGEN_SKY_LIGHT_CAPTURE =
            ThreadLocal.withInitial(it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap::new);

    @Redirect(
            method = "read",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/lighting/LevelLightEngine;queueSectionData(Lnet/minecraft/world/level/LightLayer;Lnet/minecraft/core/SectionPos;Lnet/minecraft/world/level/chunk/DataLayer;)V",
                    ordinal = 0
            )
    )
    private static void byepregen$readYABlockLight(
            LevelLightEngine lightEngine,
            LightLayer layer,
            SectionPos sectionPos,
            DataLayer dataLayer
    ) {
        if (dataLayer != null) {
            BYEPREGEN_BLOCK_LIGHT_CAPTURE.get().put(sectionPos.y(), dataLayer);
        }
    }

    @Redirect(
            method = "read",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/lighting/LevelLightEngine;queueSectionData(Lnet/minecraft/world/level/LightLayer;Lnet/minecraft/core/SectionPos;Lnet/minecraft/world/level/chunk/DataLayer;)V",
                    ordinal = 1
            )
    )
    private static void byepregen$readYASkyLight(
            LevelLightEngine lightEngine,
            LightLayer layer,
            SectionPos sectionPos,
            DataLayer dataLayer
    ) {
        if (dataLayer != null) {
            BYEPREGEN_SKY_LIGHT_CAPTURE.get().put(sectionPos.y(), dataLayer);
        }
    }

    @Inject(
            method = "read",
            at = @At("RETURN")
    )
    private static void byepregen$finishYALightLoad(
            ServerLevel level,
            net.minecraft.world.entity.ai.village.poi.PoiManager poiManager,
            net.minecraft.world.level.chunk.storage.RegionStorageInfo regionStorageInfo,
            ChunkPos chunkPos,
            CallbackInfoReturnable<ProtoChunk> cir
    ) {
        ChunkAccess chunk = cir.getReturnValue();
        if (chunk instanceof YAChunkLightAccess access) {
            byepregen$installCaptured(access, LightLayer.BLOCK, BYEPREGEN_BLOCK_LIGHT_CAPTURE.get());
            byepregen$installCaptured(access, LightLayer.SKY, BYEPREGEN_SKY_LIGHT_CAPTURE.get());
        }
        BYEPREGEN_BLOCK_LIGHT_CAPTURE.get().clear();
        BYEPREGEN_SKY_LIGHT_CAPTURE.get().clear();
    }

    @Unique
    private static void byepregen$installCaptured(
            YAChunkLightAccess access,
            LightLayer layer,
            it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<DataLayer> captured) {
        if (captured.isEmpty()) {
            return;
        }
        YAChunkLightData data = access.byepregen$yaLightData(layer);
        for (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<DataLayer> entry : captured.int2ObjectEntrySet()) {
            data.loadInitialSection(entry.getIntKey(), YANibbleArray.fromVanilla(entry.getValue()));
        }
        data.finishInitialLoad();
    }
}
