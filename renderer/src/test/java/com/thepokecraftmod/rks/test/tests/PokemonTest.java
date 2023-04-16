package com.thepokecraftmod.rks.test.tests;

import com.thepokecraftmod.rks.animation.InfoBasedAnimator;
import com.thepokecraftmod.rks.assimp.AssimpModelLoader;
import com.thepokecraftmod.rks.model.config.animation.AnimationGroup;
import com.thepokecraftmod.rks.model.texture.TextureType;
import com.thepokecraftmod.rks.pipeline.Shader;
import com.thepokecraftmod.rks.pipeline.UniformBlockReference;
import com.thepokecraftmod.rks.storage.AnimatedObjectInstance;
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
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class PokemonTest {
    private static final Random RANDOM = new Random();
    private static final Window WINDOW = new Window("Pokemon Test", 1920, 1080, true, true);
    private static final long START_TIME = System.currentTimeMillis();
    private static final SharedUniformBlock SHARED = new SharedUniformBlock(WINDOW, 90);
    private static final RksRenderer RKS = new RksRenderer();

    public static void main(String[] args) {
        var shader = new Shader.Builder().shader(getResource("shaders/pokemon.vsh"), getResource("shaders/pokemon.fsh")).uniform(new UniformBlockReference("SharedInfo", 0)).uniform(new UniformBlockReference("InstanceInfo", 1)).texture(TextureType.ALBEDO).texture(TextureType.NORMALS).texture(TextureType.METALNESS).texture(TextureType.ROUGHNESS).texture(TextureType.AMBIENT_OCCLUSION).texture(TextureType.EMISSIVE).build();

        var locator = new ResourceCachedFileLocator("testModels/rayquaza");
        var model = AssimpModelLoader.load("model.gltf", locator, 0x40 | 0x200); // 0x40 = genNormals 0x200 = limit bone weights
        var object = ExampleModelLoader.loadAnimatedMeshes(model);
        for (var meshObject : object.objects) meshObject.setup(shader);
        var animator = new InfoBasedAnimator(model, locator);
        var material = new MaterialUploader(model, locator, s -> shader);
        material.upload();

        var possibleAnimations = List.of(
                "idle",
                "battle_idle",
                "faint",
                "jump",
                "fall",
                "stunned",
                "sleep",
                "eat",
                "attack1",
                "attack2",
                "damage1",
                "damage2",
                "ranged_attack1",
                "ranged_attack2",
                "spot_player",
                "happy",
                "angry",
                "turn_left",
                "turn_right",
                "refresh",
                "move",
                "move_fast"
        );
        for (int i = 0; i < 60; i++) {
            var instance = new AnimatedObjectInstance(220, new Matrix4f().rotateXYZ((float) Math.toRadians(120), 0, 0).translate(0, 0 + i * 0.1f, -0.1f), materialName -> uploadUniforms(materialName, material));
            RKS.add(object, instance);
            var randomAnimation1 = possibleAnimations.get(RANDOM.nextInt(0, possibleAnimations.size()));
            var randomAnimation2 = possibleAnimations.get(RANDOM.nextInt(0, possibleAnimations.size()));

            animator.animate(instance, AnimationGroup.FLYING, "eat", 3,
                    i1 -> i1.handleTransition(AnimationGroup.FLYING, randomAnimation1, 0,
                            i2 -> i2.handleTransition(AnimationGroup.FLYING, randomAnimation2, -1)));
        }

        var die = new AtomicBoolean();

        var thread = new Thread(() -> {
            while (!die.get())
                RKS.update((System.currentTimeMillis() - START_TIME) / 1000d);
        });
        thread.start();

        while (WINDOW.isOpen()) {
            WINDOW.pollEvents();
            SHARED.update();
            GL11C.glClearColor(0, 0, 0, 1.0f);
            GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
            RKS.render();
            WINDOW.swapBuffers();
        }

        die.set(true);
    }

    private static void uploadUniforms(String materialName, MaterialUploader uploader) {
        var shader = uploader.materials.get(materialName).shader;
        var color = 255.0f;
        var distance = 5;
        shader.uploadVec3f("camPos", new Vector3f(0.1f, 0f, -1));
        shader.uploadVec3fs("lightPositions", new Vector3f(-distance, distance, -distance), new Vector3f(distance, distance, -distance), new Vector3f(-distance, -distance, -distance), new Vector3f(distance, -distance, -distance));
        shader.uploadVec3fs("lightColors", new Vector3f(color, color, color), new Vector3f(color, color, color), new Vector3f(color, color, color), new Vector3f(color, color, color));
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
