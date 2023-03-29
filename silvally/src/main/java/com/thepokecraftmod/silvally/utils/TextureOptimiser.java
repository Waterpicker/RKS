package com.thepokecraftmod.silvally.utils;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.IntelliJTheme;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

public class TextureOptimiser {

    public static void main(String[] args) {
        System.out.println("This tool will not try to optimise eyes or any non-png images. Please generate eyes yourself (in blender)");
        LafManager.setTheme(new IntelliJTheme());
        LafManager.install();

        var directory = Paths.get("F:\\NewAttempt\\ScarletViolet\\pokemon\\data\\pm0336\\pm0336_00_00");

        try (var files = Files.list(directory)) {
            var textures = files
                    .filter(path -> path.toString().endsWith(".png"))
                    .filter(path -> !path.toString().contains("lym"))
                    .filter(path -> !path.toString().contains("eye"))
                    .map(ModelTexture::new)
                    .toList();

            if (textures.stream().anyMatch(tex -> !tex.fileName.startsWith("pm")))
                System.err.println("Expected textures to be formatted in pm_####_##_##_MATERIAL_TYPE.png");

            for (var texture : textures) {
                if (texture.optimised) System.out.println(texture.fileName + " is already optimised");
                else {
                    var label = new AtomicReference<>((JLabel) null);
                    var frame = new JFrame("Texture Optimiser: " + texture.fileName);
                    var dimensions = Toolkit.getDefaultToolkit().getScreenSize();
                    frame.setSize(new Dimension(dimensions.width / 3, dimensions.height / 2));

                    var southPanel = new JPanel();
                    var save = new JButton("Save");
                    var sensitivity = new JSlider(JSlider.HORIZONTAL, 0, 40, 20);
                    southPanel.add(BorderLayout.WEST, new JLabel("Overlay Sensitivity"));
                    southPanel.add(BorderLayout.CENTER, sensitivity);
                    southPanel.add(BorderLayout.EAST, save);

                    frame.getContentPane().add(BorderLayout.SOUTH, southPanel);
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frame.setVisible(true);

                    var original = generateOverlay(texture, sensitivity, true);

                    frame.getContentPane().add(new JLabel(new ImageIcon(original.overlay())));
                    frame.pack();

                    sensitivity.addChangeListener(e -> {
                        if (label.get() != null) frame.getContentPane().remove(label.get());
                        label.set(new JLabel(new ImageIcon(generateOverlay(texture, sensitivity, true).overlay())));
                        frame.getContentPane().add(label.get());
                        frame.pack();
                    });

                    save.addActionListener(e -> {
                        try {
                            var output = Paths.get("out");
                            Files.createDirectories(output);
                            var images = generateOverlay(texture, sensitivity, false);
                            ImageIO.write(images.overlay(), "png", output.resolve(texture.fileName.replace(".png", "_overlay.png")).toFile());
                            ImageIO.write(images.halfMirror(), "png", output.resolve(texture.fileName).toFile());
                            frame.setVisible(false);
                        } catch (IOException ex) {
                            throw new RuntimeException("Failed to save images", ex);
                        }
                    });
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read files", e);
        }
    }

    private static OptimisedImages generateOverlay(ModelTexture texture, JSlider sensitivity, boolean writeWhitePixels) {
        // Step 1: Strip away the right side of the image
        var halfMirror = new BufferedImage(texture.image.getWidth() / 2, texture.image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        halfMirror.setData(texture.image.getData(new Rectangle(0, 0, texture.image.getWidth() / 2, texture.image.getHeight())));

        // Step 2: mirror it and compare this image
        var mirrored = mirror(halfMirror);
        var overlay = new BufferedImage(texture.image.getWidth(), texture.image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < texture.image.getWidth(); x++) {
            for (int y = 0; y < texture.image.getHeight(); y++) {
                int originalRgb = texture.image.getRGB(x, y);
                int mirroredRgb = mirrored.getRGB(x, y);

                if (originalRgb != mirroredRgb) {
                    int correctRed = 0xFF & (originalRgb >> 16);
                    int correctGreen = 0xFF & (originalRgb >> 8);
                    int correctBlue = 0xFF & (originalRgb);
                    int mirroredRed = 0xFF & (mirroredRgb >> 16);
                    int mirroredGreen = 0xFF & (mirroredRgb >> 8);
                    int mirroredBlue = 0xFF & (mirroredRgb);

                    if (
                            difference(correctRed, mirroredRed) > sensitivity.getValue() ||
                            difference(correctGreen, mirroredGreen) > sensitivity.getValue() ||
                            difference(correctBlue, mirroredBlue) > sensitivity.getValue()
                    ) overlay.setRGB(x, y, originalRgb);
                    else if(writeWhitePixels) overlay.setRGB(x, y, 0xFFFFFFFF);
                }
            }
        }

        return new OptimisedImages(texture.image, mirrored, halfMirror, overlay);
    }

    private static int difference(int a, int b) {
        return Math.max(a, b) - Math.min(a, b);
    }

    private static BufferedImage mirror(BufferedImage image) {
        var mirror = new BufferedImage(image.getWidth() * 2, image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int lx = 0, rx = image.getWidth() * 2 - 1; lx < image.getWidth(); lx++, rx--) {
                int p = image.getRGB(lx, y);
                mirror.setRGB(lx, y, p);
                mirror.setRGB(rx, y, p);
            }
        }

        return mirror;
    }

    private record OptimisedImages(
            BufferedImage original,
            BufferedImage mirrored,
            BufferedImage halfMirror,
            BufferedImage overlay
    ) {
    }
}
