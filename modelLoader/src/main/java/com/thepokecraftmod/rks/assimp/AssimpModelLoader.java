package com.thepokecraftmod.rks.assimp;

import com.thepokecraftmod.rks.FileLocator;
import com.thepokecraftmod.rks.model.Mesh;
import com.thepokecraftmod.rks.model.Model;
import com.thepokecraftmod.rks.model.animation.Joint;
import com.thepokecraftmod.rks.model.extra.ModelConfig;
import com.thepokecraftmod.rks.model.material.Material;
import com.thepokecraftmod.rks.model.material.ShadingMethod;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.util.*;

public class AssimpModelLoader {

    public static Model load(String name, FileLocator locator, int extraFlags) {
        var fileIo = AIFileIO.create()
                .OpenProc((pFileIO, pFileName, openMode) -> {
                    var fileName = MemoryUtil.memUTF8(pFileName);
                    var bytes = locator.getFile(fileName);
                    var data = BufferUtils.createByteBuffer(bytes.length);
                    data.put(bytes);
                    data.flip();

                    return AIFile.create()
                            .ReadProc((pFile, pBuffer, size, count) -> {
                                var max = Math.min(data.remaining() / size, count);
                                MemoryUtil.memCopy(MemoryUtil.memAddress(data), pBuffer, max * size);
                                data.position((int) (data.position() + max * size));
                                return max;
                            })
                            .SeekProc((pFile, offset, origin) -> {
                                switch (origin) {
                                    case Assimp.aiOrigin_CUR -> data.position(data.position() + (int) offset);
                                    case Assimp.aiOrigin_SET -> data.position((int) offset);
                                    case Assimp.aiOrigin_END -> data.position(data.limit() + (int) offset);
                                }

                                return 0;
                            })
                            .FileSizeProc(pFile -> data.limit())
                            .address();
                })
                .CloseProc((pFileIO, pFile) -> {
                    var aiFile = AIFile.create(pFile);
                    aiFile.ReadProc().free();
                    aiFile.SeekProc().free();
                    aiFile.FileSizeProc().free();
                });

        var scene = Assimp.aiImportFileEx(name, Assimp.aiProcess_Triangulate | Assimp.aiProcess_JoinIdenticalVertices | Assimp.aiProcess_ImproveCacheLocality | extraFlags, fileIo);
        if (scene == null) throw new RuntimeException(Assimp.aiGetErrorString());
        var result = readScene(scene, name, locator);
        Assimp.aiReleaseImport(scene);
        return result;
    }

    private static Model readScene(AIScene scene, String fullPath, FileLocator locator) {
        var rootPath = fullPath.replace("\\", "/").substring(0, fullPath.lastIndexOf("/"));
        var config = readConfig(rootPath, locator);
        var materials = readMaterialData(scene);
        var meshes = readMeshData(scene);
        var root = Joint.create(scene.mRootNode());
        return new Model(rootPath, materials, meshes, root, config);
    }

    private static ModelConfig readConfig(String fullPath, FileLocator locator) {
        var extrasPath = fullPath + "/model.config.json";
        var json = new String(locator.getFile(extrasPath));
        return ModelConfig.GSON.fromJson(json, ModelConfig.class);
    }

    private static Mesh[] readMeshData(AIScene scene) {
        var meshes = new Mesh[scene.mNumMeshes()];

        for (int i = 0; i < scene.mNumMeshes(); i++) {
            var mesh = AIMesh.create(scene.mMeshes().get(i));
            var name = mesh.mName().dataString();
            var material = mesh.mMaterialIndex();
            var indices = new ArrayList<Integer>();
            var positions = new ArrayList<Vector3f>();
            var uvs = new ArrayList<Vector2f>();
            var normals = new ArrayList<Vector3f>();

            // Indices
            var aiFaces = mesh.mFaces();
            for (int j = 0; j < mesh.mNumFaces(); j++) {
                var aiFace = aiFaces.get(j);
                indices.add(aiFace.mIndices().get(0));
                indices.add(aiFace.mIndices().get(1));
                indices.add(aiFace.mIndices().get(2));
            }

            // Positions
            var aiVert = mesh.mVertices();
            for (int j = 0; j < mesh.mNumVertices(); j++)
                positions.add(new Vector3f(aiVert.get(j).x(), aiVert.get(j).y(), aiVert.get(j).z()));

            // UV's
            var aiUV = mesh.mTextureCoords(0);
            if (aiUV != null) {
                while (aiUV.remaining() > 0) {
                    var uv = aiUV.get();
                    uvs.add(new Vector2f(uv.x(), uv.y()));
                }
            }

            // Normals
            var aiNormals = mesh.mNormals();
            if (aiNormals != null) {
                for (int j = 0; j < mesh.mNumFaces(); j++)
                    normals.add(new Vector3f(aiNormals.get(j).x(), aiNormals.get(j).y(), aiNormals.get(j).z()));
            }

            meshes[i] = new Mesh(name, material, indices, positions, uvs, normals);
        }

        return meshes;
    }

    private static String[] readMaterialData(AIScene scene) {
        var materials = new String[scene.mNumMaterials()];

        for (int i = 0; i < scene.mNumMaterials(); i++) {
            var aiMat = AIMaterial.create(scene.mMaterials().get(i));

            for (int j = 0; j < aiMat.mNumProperties(); j++) {
                var property = AIMaterialProperty.create(aiMat.mProperties().get(j));
                var name = property.mKey().dataString();
                var data = property.mData();

                if (name.equals(Assimp.AI_MATKEY_NAME)) {
                    var matName = AIString.create(MemoryUtil.memAddress(data)).dataString();
                    materials[i] = matName;
                }
            }
        }

        return materials;
    }
}
