package com.thepokecraftmod.rks.test.load;

import com.thepokecraftmod.rks.draw.MeshDrawCommand;
import com.thepokecraftmod.rks.model.Model;
import com.thepokecraftmod.rks.scene.MeshObject;
import com.thepokecraftmod.rks.scene.MultiRenderObject;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class ExampleModelLoader {

    public static MultiRenderObject<MeshObject> loadMeshes(Model model) {
        var mro = new MultiRenderObject<MeshObject>();

        for (var mesh : model.meshes()) {
            var meshObject = new MeshObject(model.materials()[mesh.material()].name());

            var useShort = mesh.indices().size() < Short.MAX_VALUE;
            var indexBuffer = MemoryUtil.memAlloc(mesh.indices().size() * (useShort ? 2 : 4));
            mesh.indices().forEach(integer -> {
                if(useShort) indexBuffer.putShort((short) (int) integer);
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
            mro.add(meshObject, false);
        }

        return mro;
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
