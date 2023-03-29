package com.thepokecraftmod.rks.test.tests;

import com.thepokecraftmod.rks.RKS;
import com.thepokecraftmod.rks.assimp.AssimpModelLoader;
import com.thepokecraftmod.rks.model.texture.TextureType;
import com.thepokecraftmod.rks.pipeline.Shader;
import com.thepokecraftmod.rks.pipeline.UniformBlockReference;
import com.thepokecraftmod.rks.storage.ObjectInstance;
import com.thepokecraftmod.rks.test.load.ExampleModelLoader;
import com.thepokecraftmod.rks.test.load.MaterialUploader;
import com.thepokecraftmod.rks.test.load.ResourceCachedFileLocator;
import com.thepokecraftmod.rks.test.util.SharedUniformBlock;
import com.thepokecraftmod.rks.test.util.Window;
import com.thepokecraftmod.rks.texture.Gpu2DTexture;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class UnlitTest {
    private static final Window WINDOW = new Window("Unlit Pokemon Test", 1920, 1080, false);
    private static final SharedUniformBlock SHARED = new SharedUniformBlock(WINDOW, 90);
    private static final RKS RKS = new RKS();

    public static void main(String[] args) throws IOException {
        var missing = Gpu2DTexture.create(UnlitTest.class.getResourceAsStream("/rks_missing.png").readAllBytes(), "missing.png");

        var shader = new Shader.Builder()
                .shader(getResource("shaders/unlit.vsh"), getResource("shaders/unlit.fsh"))
                .uniform(new UniformBlockReference("SharedInfo", 0))
                .uniform(new UniformBlockReference("InstanceInfo", 1))
                .texture(TextureType.DIFFUSE)
                .build();


        var model = AssimpModelLoader.load("testmodel/model.gltf", new ResourceCachedFileLocator(), 0);
        var object = ExampleModelLoader.loadMeshes(model);
        for (var meshObject : object.objects) meshObject.setup(shader);

        var material = new MaterialUploader(model, s -> shader);

        var instance = new ObjectInstance(new Matrix4f(), material::handle);
        RKS.objectManager.add(object, instance);

        while (WINDOW.isOpen()) {
            WINDOW.pollEvents();
            SHARED.update();
            instance.transformationMatrix.rotateXYZ(new Vector3f(0, 0.05f, 0));
            GL11C.glClearColor(0, 0.32156864f, 0, 1.0f);
            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
            RKS.render(0);
            WINDOW.swapBuffers();
        }

        model.close();
    }

    private static String getResource(String name) {
        try {
            return new String(UnlitTest.class.getResourceAsStream("/" + name).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
