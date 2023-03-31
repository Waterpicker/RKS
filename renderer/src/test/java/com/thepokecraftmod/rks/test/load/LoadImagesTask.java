package com.thepokecraftmod.rks.test.load;

import com.thebombzen.jxlatte.imageio.JXLImageReader;
import com.thebombzen.jxlatte.imageio.JXLImageReaderSpi;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public class LoadImagesTask extends RecursiveTask<List<BufferedImage>> {

    private final List<byte[]> images;

    public LoadImagesTask(List<byte[]> images) {
        this.images = images;
    }

    @Override
    protected List<BufferedImage> compute() {
        var result = new ArrayList<BufferedImage>();
        var queuedTasks = new ArrayList<LoadImagesTask>();

        if (images.size() > 1) {
            for (var image : images) {
                var task = new LoadImagesTask(List.of(image));
                task.fork();
                queuedTasks.add(task);
            }
        } else result.add(read(images.get(0)));

        for (var queuedTask : queuedTasks) result.addAll(queuedTask.join());
        return result;
    }

    private BufferedImage read(byte[] image) {
        try {
            var reader = new JXLImageReader(null, image);
            return reader.read(0, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
