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

public class BRDFTest {
    private static final Window WINDOW = new Window("BRDF Pokemon Test", 1920, 1080, false, false);
    private static final SharedUniformBlock SHARED = new SharedUniformBlock(WINDOW, 90);
    private static final RksRenderer RKS = new RksRenderer();

    public static void main(String[] args) {
        var shader = new Shader.Builder()
                .shader(getResource("shaders/brdf.vsh"), getResource("shaders/brdf.fsh"))
                .uniform(new UniformBlockReference("SharedInfo", 0))
                .uniform(new UniformBlockReference("InstanceInfo", 1))
                .texture(TextureType.ALBEDO)
                .texture(TextureType.NORMALS)
                .texture(TextureType.METALNESS)
                .texture(TextureType.ROUGHNESS)
                .texture(TextureType.AMBIENT_OCCLUSION)
                .build();


        var locator = new ResourceCachedFileLocator("testModels/seviper");
        var model = AssimpModelLoader.load("model.gltf", locator, 0x40);// genNormals
        var object = ExampleModelLoader.loadMeshes(model);
        for (var meshObject : object.objects) meshObject.setup(shader);

        var material = new MaterialUploader(model, locator, s -> shader);
        material.upload();

        var instance = new ObjectInstance(new Matrix4f().translation(0, -0.8f, -0.3f), materialName -> uploadUniforms(materialName, material));
        RKS.add(object, instance);

        while (WINDOW.isOpen()) {
            WINDOW.pollEvents();
            SHARED.update();
            instance.transformationMatrix.identity().rotateXYZ(new Vector3f(0, WINDOW.getCursorX() / 100, 0));
            instance.transformationMatrix.rotateXYZ(new Vector3f(0, 0.02f, 0));
            GL11C.glClearColor(0, 0, 0, 1.0f);
            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
            RKS.render();
            RKS.update(0);
            WINDOW.swapBuffers();
        }
    }

    private static void uploadUniforms(String materialName, MaterialUploader uploader) {
        var shader = uploader.defaultMaterials.get(materialName).shader;
        var lightStrength = 20000.0f;
        shader.uploadVec3f("camPos", new Vector3f(0f, 0f, -1));
        shader.uploadVec3fs(
                "lightPositions",
                new Vector3f(-100.0f, 100.0f, 100.0f),
                new Vector3f(100.0f, 100.0f, 100.0f),
                new Vector3f(-100.0f, -100.0f, 100.0f),
                new Vector3f(100.0f, -100.0f, 100.0f)
        );
        shader.uploadVec3fs(
                "lightColors",
                new Vector3f(lightStrength, lightStrength, lightStrength),
                new Vector3f(lightStrength, lightStrength, lightStrength),
                new Vector3f(lightStrength, lightStrength, lightStrength),
                new Vector3f(lightStrength, lightStrength, lightStrength)
        );
        uploader.handle(materialName);
    }

    private static String getResource(String name) {
        try {
            return new String(Objects.requireNonNull(BRDFTest.class.getResourceAsStream("/" + name), "Couldn't find resource " + name).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
