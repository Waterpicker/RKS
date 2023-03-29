package com.thepokecraftmod.silvally.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public class ModelTexture {

    public final Path path;
    public final BufferedImage image;
    public final boolean optimised;
    public final String extension;
    public final String type;
    public final String fileName;

    public ModelTexture(Path texture) {
        try {
            this.path = texture;
            this.image = ImageIO.read(path.toFile());
            this.optimised = path.toString().contains("overlay") || image.getWidth() != image.getHeight(); // 2 Methods of optimisation. Overlay and Image Splitting.
            this.fileName = texture.getFileName().toString();
            this.extension = fileName.substring(fileName.lastIndexOf(".") + 1);
            this.type = fileName.substring(fileName.lastIndexOf("_") + 1, fileName.indexOf("."));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "ModelTexture(\"" + fileName + "\")";
    }
}
