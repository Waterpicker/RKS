package com.thepokecraftmod.rks.rendering;

import com.thepokecraftmod.rks.loading.ModelLoader;
import com.thepokecraftmod.rks.storage.ObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RKS {
    private static final Logger LOGGER = LoggerFactory.getLogger("RKS Render System");
    public static boolean DEBUG_THREADS = false;
    public final ObjectManager objectManager = new ObjectManager();
    private final ModelLoader loader;
    private static final Queue<Runnable> TASKS = new ConcurrentLinkedQueue<>();

    public RKS() {
        var startLoad = System.currentTimeMillis();
        this.loader = new ModelLoader();
        LOGGER.info("RareCandy Startup took " + (System.currentTimeMillis() - startLoad) + "ms");
    }

    public void render(boolean clearInstances, double secondsPassed) {
        var task = TASKS.poll();
        while (task != null) {
            task.run();
            task = TASKS.poll();
        }

        objectManager.update(secondsPassed);
        objectManager.render();

        if (clearInstances) {
            this.objectManager.clearObjects();
        }
    }

    public void close() {
        this.loader.close();
    }

    public ModelLoader getLoader() {
        return loader;
    }

    public static void runLater(Runnable r) {
        TASKS.add(r);
    }
}
