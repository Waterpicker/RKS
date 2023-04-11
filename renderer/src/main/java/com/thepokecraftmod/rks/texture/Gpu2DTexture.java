package com.thepokecraftmod.rks.texture;

import com.thepokecraftmod.rks.model.extra.TextureFilter;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;

public class Gpu2DTexture {

    public final String name;
    public final int id;

    public Gpu2DTexture(ByteBuffer rgbaBytes, int width, int height, TextureFilter filter, String name) {
        this.name = name;
        this.id = GL11C.glGenTextures();

        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
        GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, width, height, 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, rgbaBytes);

        GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL20C.GL_MIRRORED_REPEAT);
        GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);
        GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, filter.glId);
        GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, filter.glId);
    }

    public static Gpu2DTexture create(BufferedImage image, TextureFilter filter, String name) {
        var rawData = ((DataBufferInt) image.getData().getDataBuffer()).getData();
        var data = MemoryUtil.memAlloc(rawData.length * 4);
        for (var pixel : rawData) {
            data.put((byte) ((pixel >> 16) & 0xFF));
            data.put((byte) ((pixel >> 8) & 0xFF));
            data.put((byte) (pixel & 0xFF));
            data.put((byte) ((pixel >> 24) & 0xFF));
        }

        data.flip();
        return new Gpu2DTexture(data, image.getWidth(), image.getHeight(), filter, name);
    }

    public static Gpu2DTexture create(byte[] bytes, TextureFilter filter, String name) {
        try (var stack = MemoryStack.stackPush()) {
            var fileBytes = Gpu3DTexture.readResource(bytes);
            var width = stack.mallocInt(1);
            var height = stack.mallocInt(1);
            var channels = stack.mallocInt(1);
            var rgbaBytes = STBImage.stbi_load_from_memory(fileBytes, width, height, channels, 4);
            if (rgbaBytes == null)
                throw new RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason());

            return new Gpu2DTexture(rgbaBytes, width.get(0), height.get(0), filter, name);
        }
    }

    public void bind(int slot) {
        GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + slot);
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, id);
    }
}
