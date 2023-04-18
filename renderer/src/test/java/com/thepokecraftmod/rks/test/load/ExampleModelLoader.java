package com.thepokecraftmod.rks.test.load;

import com.thepokecraftmod.rks.Pair;
import com.thepokecraftmod.rks.draw.MeshDrawCommand;
import com.thepokecraftmod.rks.model.Mesh;
import com.thepokecraftmod.rks.model.Model;
import com.thepokecraftmod.rks.model.animation.Skeleton;
import com.thepokecraftmod.rks.scene.MeshObject;
import com.thepokecraftmod.rks.scene.FullMesh;
import org.joml.Vector4f;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.*;

public class ExampleModelLoader {

    public static FullMesh loadMeshes(Model model) {
        var mro = new FullMesh();

        for (var mesh : model.meshes()) {
            var meshObject = new MeshObject(model.materialReferences()[mesh.material()]);

            var useShort = mesh.indices().size() < Short.MAX_VALUE;
            var indexBuffer = MemoryUtil.memAlloc(mesh.indices().size() * (useShort ? 2 : 4));
            mesh.indices().forEach(integer -> {
                if (useShort) indexBuffer.putShort((short) (int) integer);
                else indexBuffer.putInt(integer);
            });
            indexBuffer.flip();

            var vao = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(vao);

            // Positions
            var positionBuffer = MemoryUtil.memAllocFloat(mesh.positions().size() * 3);
            mesh.positions().forEach(vec3 -> {
                positionBuffer.put(vec3.x());
                positionBuffer.put(vec3.y());
                positionBuffer.put(vec3.z());
            });

            positionBuffer.flip();
            bindArrayBuffer(positionBuffer);
            vertexAttribPointer(0, 3);

            // UV's
            var uvBuffer = MemoryUtil.memAllocFloat(mesh.uvs().size() * 2);
            mesh.uvs().forEach(vec2 -> {
                uvBuffer.put(vec2.x());
                uvBuffer.put(vec2.y());
            });

            uvBuffer.flip();
            bindArrayBuffer(uvBuffer);
            vertexAttribPointer(1, 2);

            // Normals
            var normalBuffer = MemoryUtil.memAllocFloat(mesh.normals().size() * 3);
            mesh.normals().forEach(vec3 -> {
                normalBuffer.put(vec3.x());
                normalBuffer.put(vec3.y());
                normalBuffer.put(vec3.z());
            });

            normalBuffer.flip();
            bindArrayBuffer(normalBuffer);
            vertexAttribPointer(2, 3);

            var ebo = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, ebo);
            GL15.glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);

            meshObject.model.meshDrawCommands.add(new MeshDrawCommand(
                    vao,
                    GL11C.GL_TRIANGLES,
                    useShort ? GL11C.GL_UNSIGNED_SHORT : GL11C.GL_UNSIGNED_INT,
                    ebo,
                    mesh.indices().size()
            ));

            MemoryUtil.memFree(indexBuffer);
            MemoryUtil.memFree(positionBuffer);
            MemoryUtil.memFree(uvBuffer);
            mro.add(meshObject);
        }

        return mro;
    }

    public static FullMesh loadAnimatedMeshes(Model model) {
        var fullMesh = new FullMesh();

        for (var mesh : model.meshes()) {
            if (mesh.bones().size() == 0) throw new RuntimeException("Mesh has no bones");
            var meshObject = new MeshObject(model.materialReferences()[mesh.material()]);

            var useShort = mesh.indices().size() < Short.MAX_VALUE;
            var indexBuffer = MemoryUtil.memAlloc(mesh.indices().size() * (useShort ? 2 : 4));
            mesh.indices().forEach(integer -> {
                if (useShort) indexBuffer.putShort((short) (int) integer);
                else indexBuffer.putInt(integer);
            });
            indexBuffer.flip();

            var vao = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(vao);

            // Positions
            var positionBuffer = MemoryUtil.memAllocFloat(mesh.positions().size() * 3);
            mesh.positions().forEach(vec3 -> {
                positionBuffer.put(vec3.x());
                positionBuffer.put(vec3.y());
                positionBuffer.put(vec3.z());
            });

            positionBuffer.flip();
            bindArrayBuffer(positionBuffer);
            vertexAttribPointer(0, 3);

            // UV's
            var uvBuffer = MemoryUtil.memAllocFloat(mesh.uvs().size() * 2);
            mesh.uvs().forEach(vec2 -> {
                uvBuffer.put(vec2.x());
                uvBuffer.put(vec2.y());
            });

            uvBuffer.flip();
            bindArrayBuffer(uvBuffer);
            vertexAttribPointer(1, 2);

            // Normals
            var normalBuffer = MemoryUtil.memAllocFloat(mesh.normals().size() * 3);
            mesh.normals().forEach(vec3 -> {
                normalBuffer.put(vec3.x());
                normalBuffer.put(vec3.y());
                normalBuffer.put(vec3.z());
            });

            normalBuffer.flip();
            bindArrayBuffer(normalBuffer);
            vertexAttribPointer(2, 3);

            var jointWeightData = generateJointWeightData(mesh, model.skeleton());

            // Joints
            var jointBuffer = MemoryUtil.memAllocFloat(jointWeightData.a().size() * 4);
            jointWeightData.a().forEach(vec4 -> {
                jointBuffer.put(vec4.x());
                jointBuffer.put(vec4.y());
                jointBuffer.put(vec4.z());
                jointBuffer.put(vec4.w());
            });

            jointBuffer.flip();
            bindArrayBuffer(jointBuffer);
            vertexAttribPointer(3, 4);

            // Weights
            var weightBuffer = MemoryUtil.memAllocFloat(jointWeightData.b().size() * 4);
            jointWeightData.b().forEach(vec4 -> {
                weightBuffer.put(vec4.x());
                weightBuffer.put(vec4.y());
                weightBuffer.put(vec4.z());
                weightBuffer.put(vec4.w());
            });

            weightBuffer.flip();
            bindArrayBuffer(weightBuffer);
            vertexAttribPointer(4, 4);

            var ebo = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, ebo);
            GL15.glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);

            meshObject.model.meshDrawCommands.add(new MeshDrawCommand(
                    vao,
                    GL11C.GL_TRIANGLES,
                    useShort ? GL11C.GL_UNSIGNED_SHORT : GL11C.GL_UNSIGNED_INT,
                    ebo,
                    mesh.indices().size()
            ));

            MemoryUtil.memFree(indexBuffer);
            MemoryUtil.memFree(positionBuffer);
            MemoryUtil.memFree(uvBuffer);
            MemoryUtil.memFree(normalBuffer);
            MemoryUtil.memFree(jointBuffer);
            MemoryUtil.memFree(weightBuffer);
            fullMesh.add(meshObject);
        }

        return fullMesh;
    }

    private static Pair<List<Vector4f>, List<Vector4f>> generateJointWeightData(Mesh mesh, Skeleton skeleton) {
        var dataSize = 4 * 2;
        var data = new float[mesh.positions().size() * dataSize];
        var bone_index_map0 = new HashMap<Integer, Integer>();
        var bone_index_map1 = new HashMap<Integer, Integer>();

        for (var boneId = 0; boneId < mesh.bones().size(); boneId++) {
            var bone = Objects.requireNonNull(mesh.bones().get(boneId));
            var joinedBoneId = skeleton.getId(bone); // gets the global bone id instead of using the meshes bone id

            for (int weightId = 0; weightId < bone.weights.length; weightId++) {
                var weight = bone.weights[weightId];
                int vertId = weight.vertexId;
                int pVertex = vertId * dataSize; // pointer to where a vertex starts in the array.

                if (!bone_index_map0.containsKey(vertId)) {
                    data[(pVertex)] = joinedBoneId;
                    data[(pVertex) + 2] = weight.weight;
                    bone_index_map0.put(vertId, 0);
                } else if (bone_index_map0.get(vertId) == 0) {
                    data[(pVertex) + 1] = joinedBoneId;
                    data[(pVertex) + 3] = weight.weight;
                    bone_index_map0.put(vertId, 1);
                } else if (!bone_index_map1.containsKey(vertId)) {
                    data[(pVertex) + 4] = joinedBoneId;
                    data[(pVertex) + 6] = weight.weight;
                    bone_index_map1.put(vertId, 0);
                } else if (bone_index_map1.get(vertId) == 0) {
                    data[(pVertex) + 5] = joinedBoneId;
                    data[(pVertex) + 7] = weight.weight;
                    bone_index_map1.put(vertId, 1);
                } else {
                    throw new RuntimeException("Max 4 bones per vertex");
                }
            }
        }

        var joints = new ArrayList<Vector4f>();
        var weights = new ArrayList<Vector4f>();

        for (int i = 0; i < data.length; i += dataSize) {
            joints.add(new Vector4f(data[i], data[i + 1], data[i + 4], data[i + 5]));
            weights.add(new Vector4f(data[i + 2], data[i + 3], data[i + 6], data[i + 7]));
        }

        return new Pair<>(joints, weights);
    }

    private static void vertexAttribPointer(int binding, int numComponents) {
        GL20.glEnableVertexAttribArray(binding);
        GL20.glVertexAttribPointer(
                binding,
                numComponents,
                GL11.GL_FLOAT,
                false,
                0,
                0);
    }

    private static void bindArrayBuffer(FloatBuffer data) {
        var glBufferView = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
    }
}
