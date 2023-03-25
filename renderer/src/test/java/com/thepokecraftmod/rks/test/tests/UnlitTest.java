package com.thepokecraftmod.rks.test.tests;

import com.thepokecraftmod.rks.assimp.AssimpModelLoader;
import com.thepokecraftmod.rks.storage.ObjectInstance;
import com.thepokecraftmod.rks.RKS;
import com.thepokecraftmod.rks.scene.MultiRenderObject;
import com.thepokecraftmod.rks.test.util.ResourceCachedFileLocator;
import com.thepokecraftmod.rks.test.util.Window;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;

public class UnlitTest {
    private static final Window WINDOW = new Window("Unlit Pokemon Test", 1920, 1080, false);
    private static final RKS RKS = new RKS();

    public static void main(String[] args) {
        var model = AssimpModelLoader.load("testmodel/model.gltf", new ResourceCachedFileLocator(), 0);
        var object = new MultiRenderObject<>();

        RKS.objectManager.add(object, new ObjectInstance(new Matrix4f(), (type, slot) -> {}));

        while (WINDOW.isOpen()) {
            WINDOW.pollEvents();
            RKS.render(0);
            GL11C.glClearColor(0, 0.32156864f, 0, 1.0f);
            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
            WINDOW.swapBuffers();
        }

        model.close();
    }
}
