package com.moepus.byepregen.PaletteContainer;

import com.moepus.byepregen.PaletteContainer.ArenaPelette.ArenaBlockStatePalettedContainer;
import com.moepus.byepregen.PaletteContainer.ArenaPelette.Codecs.PayloadBuilder;
import com.moepus.byepregen.PaletteContainer.ArenaPelette.Codecs.SerializationScratch;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

/**
 * Builds the arena block-state compound tag. The pre-serialized payload is parsed into a real
 * {@link CompoundTag} (Tag is sealed in 26.1, so raw-payload tags can no longer be returned
 * from codecs); the payload format itself is unchanged.
 */
public final class ByteStreamTag {
    private static final int ROOT_HEADER_SIZE = 3;

    private ByteStreamTag() {
    }

    public static CompoundTag uniform(int rawId) {
        return materialize(PayloadBuilder.uniform(rawId));
    }

    public static CompoundTag packed(
            SerializationScratch scratch, ArenaBlockStatePalettedContainer container) {
        return materialize(PayloadBuilder.packed(scratch, container));
    }

    private static CompoundTag materialize(byte[] payload) {
        byte[] root = new byte[payload.length + ROOT_HEADER_SIZE];
        root[0] = Tag.TAG_COMPOUND;
        System.arraycopy(payload, 0, root, ROOT_HEADER_SIZE, payload.length);
        try {
            return NbtIo.read(new DataInputStream(new ByteArrayInputStream(root)));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to materialize arena block state payload", exception);
        }
    }
}
