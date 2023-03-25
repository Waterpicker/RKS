package com.thepokecraftmod.rks.model.texture;

import org.lwjgl.system.MemoryUtil;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class Texture implements Closeable {

    public final String reference;
    public final Map<String, Object> properties = new HashMap<>();
    public final ByteBuffer data;

    public Texture(String reference, ByteBuffer data) {
        this.reference = reference;
        this.data = data;
    }

    @Override
    public void close() {
        MemoryUtil.memFree(data);
    }
}
