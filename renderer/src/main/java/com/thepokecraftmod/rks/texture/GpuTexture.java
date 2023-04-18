package com.thepokecraftmod.rks.texture;

import com.thepokecraftmod.rks.model.config.TextureFilter;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryUtil;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.io.Closeable;
import java.nio.ByteBuffer;

public interface GpuTexture extends Closeable {

    int getId();

    void bind(int slot);

    String getName();

    @Override
    default void close() {
        GL13C.glDeleteTextures(getId());
    }

    class Direct implements GpuTexture {

        private final String name;
        private final int id;

        public Direct(ByteBuffer rgbaBytes, int width, int height, TextureFilter filter, String name) {
            this.name = name;
            this.id = GL11C.glGenTextures();

            GL13C.glActiveTexture(GL13C.GL_TEXTURE0);
            GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, id);
            GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, width, height, 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, rgbaBytes);

            GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL20C.GL_MIRRORED_REPEAT);
            GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);
            GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, filter.glId);
            GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, filter.glId);
            MemoryUtil.memFree(rgbaBytes);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public void bind(int slot) {
            GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + slot);
            GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, id);
        }
    }

    class Reference implements GpuTexture {

        public final ByteBuffer readyData;
        public final int width;
        public final int height;
        private final TextureFilter filter;
        public final String name;

        public Reference(ByteBuffer readyData, int width, int height, TextureFilter filter, String name) {
            this.readyData = readyData;
            this.width = width;
            this.height = height;
            this.filter = filter;
            this.name = name;
        }

        public Reference(BufferedImage image, TextureFilter filter, String name) {
            var buffer = image.getData().getDataBuffer();

            if (buffer instanceof DataBufferFloat intBuffer) {
                var rawData = intBuffer.getData();
                this.readyData = MemoryUtil.memAlloc(rawData.length * 4);
                this.width = image.getWidth();
                this.height = image.getHeight();

                for (var hdrChannel : rawData) {
                    var channelValue = hdrToRgb(hdrChannel);
                    readyData.put((byte) channelValue);
                }

                readyData.flip();
                this.filter = filter;
                this.name = name;
            } else if (buffer instanceof DataBufferInt floatBuffer) {
                var rawData = floatBuffer.getData();
                this.readyData = MemoryUtil.memAlloc(rawData.length * 4);
                this.width = image.getWidth();
                this.height = image.getHeight();

                for (var pixel : rawData) {
                    readyData.put((byte) ((pixel >> 16) & 0xFF));
                    readyData.put((byte) ((pixel >> 8) & 0xFF));
                    readyData.put((byte) (pixel & 0xFF));
                    readyData.put((byte) ((pixel >> 24) & 0xFF));
                }

                readyData.flip();
                this.filter = filter;
                this.name = name;
            } else throw new RuntimeException("Unknown Data Type: " + buffer.getClass().getName());
        }

        private static int hdrToRgb(float hdr) {
            return (int) Math.min(Math.max(Math.pow(hdr, 1.0/2.2) * 255, 0), 255);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getId() {
            throw new RuntimeException("Tried getting OpenGL id of reference texture");
        }

        @Override
        public void bind(int slot) {
            throw new RuntimeException("Tried binding reference texture");
        }

        public Direct upload() {
            return new Direct(readyData, width, height, filter, name);
        }
    }
}
