package com.thepokecraftmod.rks.test.util;

import com.thepokecraftmod.rks.FileLocator;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ResourceCachedFileLocator implements FileLocator {

    private final Map<String, byte[]> fileCache = new HashMap<>();
    private String root;

    @Override
    public byte[] getFile(String name) {
        return fileCache.computeIfAbsent(name, s -> {
            try {
                var parent = getParent(name);
                var cleanString = parent + s.replace("\\", "/").replace("//", "/");
                var is = ResourceCachedFileLocator.class.getResourceAsStream(cleanString);
                return Objects.requireNonNull(is, "Unable to find resource " + cleanString).readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public ByteBuffer readImage(String name) {
        try {
            var parent = getParent(name);
            var cleanString = parent + name.replace("\\", "/").replace("//", "/");
            var is = ResourceCachedFileLocator.class.getResourceAsStream(cleanString.replace(".png", ".webp"));
            var image = ImageIO.read(Objects.requireNonNull(is));
            int height = image.getHeight();
            int width = image.getWidth();

            if (height / width == 2) {
                var mirror = new BufferedImage(width * 2, height, BufferedImage.TYPE_INT_ARGB);

                for (int y = 0; y < height; y++) {
                    for (int lx = 0, rx = width * 2 - 1; lx < width; lx++, rx--) {
                        int p = mirror.getRGB(lx, y);
                        mirror.setRGB(lx, y, p);
                        mirror.setRGB(rx, y, p);
                    }
                }

                image = mirror;
            }

            var rawData = ((DataBufferInt) image.getData().getDataBuffer()).getData();
            var data = MemoryUtil.memAlloc(rawData.length * 4);
            for (var pixel : rawData) {
                data.put((byte) ((pixel >> 16) & 0xFF));
                data.put((byte) ((pixel >> 8) & 0xFF));
                data.put((byte) (pixel & 0xFF));
                data.put((byte) ((pixel >> 24) & 0xFF));
            }

            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getParent(String fileName) {
        if (fileName.endsWith(".gltf") && fileName.contains("/")) {
            this.root = fileName.substring(0, fileName.lastIndexOf("/"));
            return "/";
        } else if (!fileName.contains(root)) return "/" + root + "/";
        else return "/";
    }
}
