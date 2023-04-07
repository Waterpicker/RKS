package com.thepokecraftmod.rks.test.tests;

import com.thepokecraftmod.rks.RKS;
import com.thepokecraftmod.rks.animation.AnimationInstance;
import com.thepokecraftmod.rks.assimp.AssimpModelLoader;
import com.thepokecraftmod.rks.model.Model;
import com.thepokecraftmod.rks.model.animation.Animation;
import com.thepokecraftmod.rks.model.animation.Skeleton;
import com.thepokecraftmod.rks.model.texture.TextureType;
import com.thepokecraftmod.rks.pipeline.Shader;
import com.thepokecraftmod.rks.pipeline.UniformBlockReference;
import com.thepokecraftmod.rks.storage.AnimatedObjectInstance;
import com.thepokecraftmod.rks.test.load.ExampleModelLoader;
import com.thepokecraftmod.rks.test.load.MaterialUploader;
import com.thepokecraftmod.rks.test.load.ResourceCachedFileLocator;
import com.thepokecraftmod.rks.test.util.SharedUniformBlock;
import com.thepokecraftmod.rks.test.util.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11C;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class PokemonTest {
    private static final Window WINDOW = new Window("Pokemon Test", 1920, 1080, false, true);
    private static final long START_TIME = System.currentTimeMillis();
    private static final SharedUniformBlock SHARED = new SharedUniformBlock(WINDOW, 90);
    private static final RKS RKS = new RKS();

    public static void main(String[] args) {
        var shader = new Shader.Builder()
                .shader(getResource("shaders/pokemon.vsh"), getResource("shaders/pokemon.fsh"))
                .uniform(new UniformBlockReference("SharedInfo", 0))
                .uniform(new UniformBlockReference("InstanceInfo", 1))
                .texture(TextureType.DIFFUSE)
                .texture(TextureType.NORMALS)
                .texture(TextureType.METALNESS)
                .texture(TextureType.ROUGHNESS)
                .texture(TextureType.AMBIENT_OCCLUSION)
                .build();


        var locator = new ResourceCachedFileLocator();
        var model = AssimpModelLoader.load("testmodel/model.gltf", locator, 0x40 | 0x200); // 0x40 = genNormals 0x200 = limit bone weights
        var object = ExampleModelLoader.loadAnimatedMeshes(model);
        for (var meshObject : object.objects) meshObject.setup(shader, loadAnimations(model, locator, "testmodel"));

        var material = new MaterialUploader(model, locator, s -> shader);

        var instance = new AnimatedObjectInstance(220, new Matrix4f().translation(0, -0.8f, -2f), materialName -> uploadUniforms(materialName, material));
        RKS.objectManager.add(object, instance);

        instance.currentAnimation = new AnimationInstance(object.objects.get(0).animations.get("idle"));

        while (WINDOW.isOpen()) {
            WINDOW.pollEvents();
            SHARED.update();
            instance.transformationMatrix.identity().rotateXYZ(new Vector3f(0, WINDOW.getCursorX() / 100, 0));
            instance.transformationMatrix.rotateXYZ(new Vector3f(0, 0.02f, 0));
            GL11C.glClearColor(0, 0, 0, 1.0f);
            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
            RKS.render((System.currentTimeMillis() - START_TIME) / 1000d);
            WINDOW.swapBuffers();
        }
    }

    private static Map<String, Animation> loadAnimations(Model model, ResourceCachedFileLocator locator, String path) {
        var skeleton = new Skeleton(model.root());
        var pAnimation = ByteBuffer.wrap(locator.getFile(path + "/pm0336_00_00_00000_defaultwait01_loop.tranm"));
        var trAnimation = com.thepokecraftmod.rks.model.animation.tranm.Animation.getRootAsAnimation(pAnimation);
        var animation = new Animation("idle", trAnimation, skeleton);
        return Map.of("idle", animation);
    }

    private static void uploadUniforms(String materialName, MaterialUploader uploader) {
        var shader = uploader.materials.get(materialName).shader;
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
            return new String(Objects.requireNonNull(PokemonTest.class.getResourceAsStream("/" + name), "Couldn't find resource " + name).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
