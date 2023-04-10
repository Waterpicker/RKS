package com.thepokecraftmod.rks;

import com.thepokecraftmod.rks.storage.ObjectManager;
import org.lwjgl.opengl.GL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RKS {
    private static final Logger LOGGER = LoggerFactory.getLogger("RKS Render System");
    public final ObjectManager objectManager = new ObjectManager();

    public RKS() {
        var startLoad = System.currentTimeMillis();
        LOGGER.info("RKS Setup Time: " + (System.currentTimeMillis() - startLoad) + "ms");
    }

    public void render(double secondsPassed) {
        objectManager.update(secondsPassed);
        objectManager.render();
    }
}
