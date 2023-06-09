package com.thepokecraftmod.rks.test.tests;

import com.thepokecraftmod.rks.assimp.AssimpModelLoader;
import com.thepokecraftmod.rks.model.texture.TextureType;
import com.thepokecraftmod.rks.pipeline.Shader;
import com.thepokecraftmod.rks.pipeline.UniformBlockReference;
import com.thepokecraftmod.rks.storage.ObjectInstance;
import com.thepokecraftmod.rks.storage.RksRenderer;
import com.thepokecraftmod.rks.test.load.ExampleModelLoader;
import com.thepokecraftmod.rks.test.load.MaterialUploader;
import com.thepokecraftmod.rks.test.load.ResourceCachedFileLocator;
import com.thepokecraftmod.rks.test.util.SharedUniformBlock;
import com.thepokecraftmod.rks.test.util.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class UnlitTest {
    private static final Window WINDOW = new Window("Unlit Pokemon Test", 1920, 1080, false, true);
    private static final SharedUniformBlock SHARED = new SharedUniformBlock(WINDOW, 90);
    private static final RksRenderer RKS = new RksRenderer();

    public static void main(String[] args) {
        var shader = new Shader.Builder()
                .shader(getResource("shaders/unlit.vsh"), getResource("shaders/unlit.fsh"))
                .uniform(new UniformBlockReference("SharedInfo", 0))
                .uniform(new UniformBlockReference("InstanceInfo", 1))
                .texture(TextureType.ALBEDO)
                .build();


        var locator = new ResourceCachedFileLocator("testModels/seviper");
        var model = AssimpModelLoader.load("model.gltf", locator, 0);
        var object = ExampleModelLoader.loadMeshes(model);
        for (var meshObject : object.objects) meshObject.setup(shader);

        var material = new MaterialUploader(model, locator, s -> shader);

        var instance = new ObjectInstance(new Matrix4f().translation(0, -0.8f, -0.3f), material::handle);
        RKS.add(object, instance);

        while (WINDOW.isOpen()) {
            WINDOW.pollEvents();
            SHARED.update();
            instance.transformationMatrix.rotateXYZ(new Vector3f(0, 0.02f, 0));
            GL11C.glClearColor(1, 1, 1, 1.0f);
            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
            RKS.update(0);
            RKS.render();
            WINDOW.swapBuffers();
        }
    }

    private static String getResource(String name) {
        try {
            return new String(Objects.requireNonNull(UnlitTest.class.getResourceAsStream("/" + name), "Couldn't find resource " + name).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
