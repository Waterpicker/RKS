package com.thepokecraftmod.rks.test.load;

import com.thebombzen.jxlatte.JXLDecoder;
import com.thebombzen.jxlatte.JXLOptions;
import com.thepokecraftmod.rks.FileLocator;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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

    public BufferedImage read(byte[] imageBytes) throws IOException {
        var decoder = new JXLDecoder(new ByteArrayInputStream(imageBytes), new JXLOptions());
        var image = decoder.decode();
        var bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        var buffer = image.getBuffer();

        for(int x = 0; x < image.getWidth(); ++x) {
            for(int y = 0; y < image.getHeight(); ++y) {
                int r = (int)Math.min(Math.max(buffer[0][y][x] * 255.0F, 0.0F), 255.0F);
                int g = (int)Math.min(Math.max(buffer[1][y][x] * 255.0F, 0.0F), 255.0F);
                int b = (int)Math.min(Math.max(buffer[2][y][x] * 255.0F, 0.0F), 255.0F);
                int a = image.getAlphaIndex() == -1 ? 255 : (int)(buffer[3][y][x] * 255.0F);
                int argb = (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | b & 255;
                bufferedImage.getRaster().setDataElements(0, 0, 1, 1, new int[]{argb});
            }
        }

        return bufferedImage;
    }

    @Override
    public BufferedImage readImage(String name) {
        try {
            var parent = getParent(name);
            var cleanString = parent + name.replace("\\", "/").replace("//", "/");
            var is = Objects.requireNonNull(ResourceCachedFileLocator.class.getResourceAsStream(cleanString), "Texture InputStream is null");
            var image = cleanString.endsWith(".jxl") ? read(is.readAllBytes()) : ImageIO.read(is);
            int height = image.getHeight();
            int width = image.getWidth();
            var needMirror = height / width == 2;

            if (needMirror) {
                var mirror = new BufferedImage(width * 2, height, BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < height; y++) {
                    for (int lx = 0, rx = width * 2 - 1; lx < width; lx++, rx--) {
                        int p = image.getRGB(lx, y);
                        mirror.setRGB(lx, y, p);
                        mirror.setRGB(rx, y, p);
                    }
                }

                image = mirror;
            }

            return image;
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
