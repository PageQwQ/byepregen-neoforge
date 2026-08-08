package com.moepus.byepregen.mixin.yalight;

import com.moepus.byepregen.yalight.YALightEngineHolder;
import java.util.BitSet;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerYALightMixin {
    @Shadow
    private ClientLevel level;

    @Redirect(
            method = "handleLevelChunkWithLight",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;queueLightUpdate(Ljava/lang/Runnable;)V"
            )
    )
    private void byepregen$handleLevelChunkWithLight(
            ClientLevel level,
            Runnable lightUpdate,
            ClientboundLevelChunkWithLightPacket packet
    ) {
        byepregen$feedLightData(level, packet.getLightData(), packet.getX(), packet.getZ());
    }

    @Redirect(
            method = "handleLightUpdatePacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;queueLightUpdate(Ljava/lang/Runnable;)V"
            )
    )
    private void byepregen$handleLightUpdatePacket(
            ClientLevel level,
            Runnable lightUpdate,
            ClientboundLightUpdatePacket packet
    ) {
        byepregen$feedLightData(level, packet.getLightData(), packet.getX(), packet.getZ());
    }

    @Unique
    private void byepregen$feedLightData(ClientLevel level, ClientboundLightUpdatePacketData data, int chunkX, int chunkZ) {
        LevelLightEngine lightEngine = level.getChunkSource().getLightEngine();
        YALightEngineHolder holder = (YALightEngineHolder)lightEngine;
        byepregen$feedLayer(holder, level, LightLayer.SKY, chunkX, chunkZ,
                data.getSkyYMask(), data.getEmptySkyYMask(), data.getSkyUpdates());
        byepregen$feedLayer(holder, level, LightLayer.BLOCK, chunkX, chunkZ,
                data.getBlockYMask(), data.getEmptyBlockYMask(), data.getBlockUpdates());
    }

    @Unique
    private void byepregen$feedLayer(
            YALightEngineHolder holder,
            ClientLevel level,
            LightLayer layer,
            int chunkX,
            int chunkZ,
            BitSet yMask,
            BitSet emptyYMask,
            List<byte[]> updates
    ) {
        LevelLightEngine lightEngine = level.getChunkSource().getLightEngine();
        int updateIndex = 0;
        for (int i = 0; i < lightEngine.getLightSectionCount(); ++i) {
            int sectionY = lightEngine.getMinLightSection() + i;
            boolean hasUpdate = yMask.get(i);
            boolean isEmpty = emptyYMask.get(i);
            if (!hasUpdate && !isEmpty) {
                continue;
            }

            SectionPos sectionPos = SectionPos.of(chunkX, sectionY, chunkZ);
            if (hasUpdate) {
                holder.byepregen$getYALightEngine().queueOwnedSectionBytes(layer, sectionPos, updates.get(updateIndex++));
            } else {
                holder.byepregen$getYALightEngine().queueZeroSectionData(layer, sectionPos);
            }
            level.setSectionDirtyWithNeighbors(chunkX, sectionY, chunkZ);
        }
    }

    @Redirect(
            method = "handleForgetLevelChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;queueLightRemoval(Lnet/minecraft/network/protocol/game/ClientboundForgetLevelChunkPacket;)V"
            )
    )
    private void byepregen$clearYALightData(ClientPacketListener instance, ClientboundForgetLevelChunkPacket packet) {
        LevelLightEngine lightEngine = this.level.getChunkSource().getLightEngine();
        ((YALightEngineHolder)lightEngine).byepregen$getYALightEngine().clearChunk(packet.pos());
    }
}
