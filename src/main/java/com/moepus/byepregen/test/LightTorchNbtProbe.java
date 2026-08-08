package com.moepus.byepregen.test;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import net.minecraft.server.level.ServerLevel;

record LightTorchNbtProbe(int source, int adjacent, int distant, int nonZero) {
    static LightTorchNbtProbe capture(ServerLevel level, ChunkAccess chunk, BlockPos source) {
        CompoundTag root = SerializableChunkData.copyOf(level, chunk).write();
        return fromRoot(root, source);
    }

    boolean hasExpectedTorch() {
        return this.source == 14 && this.adjacent == 13 && this.distant == 12;
    }

    private static LightTorchNbtProbe fromRoot(CompoundTag root, BlockPos source) {
        ListTag sections = root.getListOrEmpty("sections");
        int sectionY = source.getY() >> 4;
        for (int i = 0; i < sections.size(); ++i) {
            CompoundTag section = sections.getCompoundOrEmpty(i);
            if (section.getByteOr("Y", (byte)0) == (byte)sectionY) {
                return fromBytes(section.getByteArray("BlockLight").orElse(new byte[0]), source);
            }
        }
        return new LightTorchNbtProbe(0, 0, 0, 0);
    }

    private static LightTorchNbtProbe fromBytes(byte[] data, BlockPos source) {
        int localY = source.getY() & 15;
        int sourceIndex = localY << 8 | (source.getZ() & 15) << 4 | source.getX() & 15;
        int adjacentIndex = localY << 8 | (source.getZ() & 15) << 4 | source.east().getX() & 15;
        int distantIndex = localY << 8 | (source.getZ() & 15) << 4 | source.east(2).getX() & 15;
        int nonZero = 0;
        for (int index = 0; index < 4096; ++index) {
            nonZero += nibble(data, index) == 0 ? 0 : 1;
        }
        return new LightTorchNbtProbe(
                nibble(data, sourceIndex), nibble(data, adjacentIndex), nibble(data, distantIndex), nonZero);
    }

    private static int nibble(byte[] data, int index) {
        if (data.length != 2048) {
            return 0;
        }
        int packed = data[index >>> 1] & 255;
        return packed >>> ((index & 1) << 2) & 15;
    }
}
