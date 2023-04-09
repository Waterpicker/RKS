package com.thepokecraftmod.rks.test.tests;

import com.thepokecraftmod.rks.RKS;
import com.thepokecraftmod.rks.animation.AnimationInstance;
import com.thepokecraftmod.rks.assimp.AssimpModelLoader;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

public class PokemonTest {
    private static final Window WINDOW = new Window("Pokemon Test", 1920, 1080, true, true);
    private static final long START_TIME = System.currentTimeMillis();
    private static final SharedUniformBlock SHARED = new SharedUniformBlock(WINDOW, 90);
    private static final RKS RKS = new RKS();
    private static double lastFrameTime;

    public static void main(String[] args) {
        var shader = new Shader.Builder()
                .shader(getResource("shaders/pokemon.vsh"), getResource("shaders/pokemon.fsh"))
                .uniform(new UniformBlockReference("SharedInfo", 0))
                .uniform(new UniformBlockReference("InstanceInfo", 1))
                .texture(TextureType.ALBEDO)
                .texture(TextureType.NORMALS)
                .texture(TextureType.METALNESS)
                .texture(TextureType.ROUGHNESS)
                .texture(TextureType.AMBIENT_OCCLUSION)
                .texture(TextureType.EMISSIVE)
                .build();


        var locator = new ResourceCachedFileLocator();
        var model = AssimpModelLoader.load("testModels/fallback/model.gltf", locator, 0x40 | 0x200); // 0x40 = genNormals 0x200 = limit bone weights
        var object = ExampleModelLoader.loadAnimatedMeshes(model);
        for (var meshObject : object.objects) meshObject.setup(shader, loadAnimations(locator, "testModels/fallback", model.skeleton()));

        var material = new MaterialUploader(model, locator, s -> shader);

        var instance = new AnimatedObjectInstance(220, new Matrix4f().translation(0, -1.5f, 2f).rotateX(-90), materialName -> uploadUniforms(materialName, material));
        RKS.objectManager.add(object, instance);

        instance.currentAnimation = new AnimationInstance(object.objects.get(0).animations.get("idle"));

        while (WINDOW.isOpen()) {
            WINDOW.pollEvents();
            SHARED.update();
            instance.transformationMatrix.rotateZ((float) ((GLFW.glfwGetTime() - lastFrameTime) * 10f));
            GL11C.glClearColor(0, 0, 0, 1.0f);
            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
            RKS.render((System.currentTimeMillis() - START_TIME) / 1000d);
            WINDOW.swapBuffers();
            lastFrameTime = GLFW.glfwGetTime();
        }
    }

    private static Map<String, Animation> loadAnimations(ResourceCachedFileLocator locator, String path, Skeleton skeleton) {
        try {
            var pAnimation = ByteBuffer.wrap(Files.readAllBytes(Paths.get("F:/NewAttempt/ScarletViolet/pokemon/data/pm9999/pm9999_00_00/pm9999_00_00_20000_defaultwait01_loop.gfbanm")));
            var trAnimation = com.thepokecraftmod.rks.model.animation.tranm.Animation.getRootAsAnimation(pAnimation);
            var animation = new Animation("idle", trAnimation, skeleton);
            return Map.of("idle", animation);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void uploadUniforms(String materialName, MaterialUploader uploader) {
        var shader = uploader.materials.get(materialName).shader;
        var color = 255.0f;
        var distance = 5;
        shader.uploadVec3f("camPos", new Vector3f(0.1f, 0f, -1));
        shader.uploadVec3fs(
                "lightPositions",
                new Vector3f(-distance, distance, -distance),
                new Vector3f(distance, distance, -distance),
                new Vector3f(-distance, -distance, -distance),
                new Vector3f(distance, -distance, -distance)
        );
        shader.uploadVec3fs(
                "lightColors",
                new Vector3f(color, color, color),
                new Vector3f(color, color, color),
                new Vector3f(color, color, color),
                new Vector3f(color, color, color)
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
