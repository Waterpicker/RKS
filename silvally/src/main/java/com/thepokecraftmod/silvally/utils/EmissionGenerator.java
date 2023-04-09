package com.thepokecraftmod.silvally.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;

public class EmissionGenerator {

    public static void main(String[] args) throws IOException {
        var filePath = Paths.get(args[0]);
        var image = ImageIO.read(filePath.toFile());

        var emissionMap = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                var lymRgb = image.getRGB(x, y);
                int blue = 0xFF & lymRgb;

                if (blue > 0) emissionMap.setRGB(x, y, 0xFF00FF00);
            }
        }

        ImageIO.write(emissionMap, "png", Paths.get("out.png").toFile());
    }
}
