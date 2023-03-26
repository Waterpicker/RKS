package com.thepokecraftmod.rks.assimp;

import com.google.common.collect.ImmutableMap;
import com.thepokecraftmod.rks.FileLocator;
import com.thepokecraftmod.rks.model.Mesh;
import com.thepokecraftmod.rks.model.Model;
import com.thepokecraftmod.rks.model.animation.Joint;
import com.thepokecraftmod.rks.model.extra.ModelConfig;
import com.thepokecraftmod.rks.model.material.Material;
import com.thepokecraftmod.rks.model.material.ShadingMethod;
import com.thepokecraftmod.rks.model.texture.Texture;
import com.thepokecraftmod.rks.model.texture.TextureType;
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
        var config = readConfig(fullPath, locator);
        var materials = readMaterialData(scene, locator);
        var meshes = readMeshData(scene);
        var root = Joint.create(scene.mRootNode());
        return new Model(materials, meshes, root, config);
    }

    private static ModelConfig readConfig(String fullPath, FileLocator locator) {
        fullPath = fullPath.replace("\\", "/");
        var extrasPath = fullPath.substring(0, fullPath.lastIndexOf("/")) + "/model.config.json";
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

    private static Material[] readMaterialData(AIScene scene, FileLocator locator) {
        var materials = new Material[scene.mNumMaterials()];

        for (int i = 0; i < scene.mNumMaterials(); i++) {
            var aiMat = AIMaterial.create(scene.mMaterials().get(i));

            var textureTypeMap = new ImmutableMap.Builder<TextureType, Optional<Texture>>()
                    .put(TextureType.DIFFUSE, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_DIFFUSE, locator)))
                    .put(TextureType.SPECULAR, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_SPECULAR, locator)))
                    .put(TextureType.AMBIENT, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_AMBIENT, locator)))
                    .put(TextureType.EMISSIVE, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_EMISSIVE, locator)))
                    .put(TextureType.HEIGHT, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_HEIGHT, locator)))
                    .put(TextureType.NORMALS, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_NORMALS, locator)))
                    .put(TextureType.SHININESS, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_SHININESS, locator)))
                    .put(TextureType.OPACITY, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_OPACITY, locator)))
                    .put(TextureType.DISPLACEMENT, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_DISPLACEMENT, locator)))
                    .put(TextureType.LIGHTMAP, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_LIGHTMAP, locator)))
                    .put(TextureType.REFLECTION, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_REFLECTION, locator)))
                    .put(TextureType.BASE_COLOR, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_BASE_COLOR, locator)))
                    .put(TextureType.NORMAL_CAMERA, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_NORMAL_CAMERA, locator)))
                    .put(TextureType.EMISSION_COLOR, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_EMISSION_COLOR, locator)))
                    .put(TextureType.METALNESS, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_METALNESS, locator)))
                    .put(TextureType.DIFFUSE_ROUGHNESS, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_DIFFUSE_ROUGHNESS, locator)))
                    .put(TextureType.AMBIENT_OCCLUSION, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_AMBIENT_OCCLUSION, locator)))
                    .put(TextureType.SHEEN, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_SHEEN, locator)))
                    .put(TextureType.CLEARCOAT, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_CLEARCOAT, locator)))
                    .put(TextureType.TRANSMISSION, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_TRANSMISSION, locator)))
                    .put(TextureType.UNKNOWN, Optional.ofNullable(assimpGetTextureFile(aiMat, Assimp.aiTextureType_UNKNOWN, locator)))
                    .build();

            var materialPropertyMap = new HashMap<String, Object>();
            var currentTex = "";

            for (int j = 0; j < aiMat.mNumProperties(); j++) {
                var property = AIMaterialProperty.create(aiMat.mProperties().get(j));
                var name = property.mKey().dataString();
                var data = property.mData();

                if (name.contains("$tex")) {
                    if (name.equals(Assimp._AI_MATKEY_TEXTURE_BASE))
                        currentTex = AIString.create(MemoryUtil.memAddress(data)).dataString();
                    else {
                        var availableTextures = textureTypeMap.values().stream()
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .toList();

                        for (var texture : availableTextures) {
                            if (texture.reference.equals(currentTex)) readProperty(property, texture.properties);
                        }
                    }
                } else readProperty(property, materialPropertyMap);
            }

            var name = (String) materialPropertyMap.get(Assimp.AI_MATKEY_NAME);
            materials[i] = new Material(name, textureTypeMap, (ShadingMethod) materialPropertyMap.get(Assimp.AI_MATKEY_SHADING_MODEL), materialPropertyMap);
        }

        return materials;
    }

    private static Texture assimpGetTextureFile(AIMaterial material, int textureType, FileLocator locator) {
        if (Assimp.aiGetMaterialTextureCount(material, textureType) <= 0)
            return null;

        try (var stack = MemoryStack.stackPush()) {
            var path = AIString.malloc(stack);
            var _nullI = new int[]{0};
            var _nullF = new float[]{0};
            Assimp.aiGetMaterialTexture(material, textureType, 0, path, _nullI, _nullI, _nullF, _nullI, _nullI, _nullI);
            var file = path.dataString();
            file = file.replace("\\", "/");
            file = file.replace("//", "");
            var info = locator.readImage(file);
            return new Texture(file, info.nativeBuffer(), info.width(), info.height());

        }
    }

    private static void readProperty(AIMaterialProperty property, Map<String, Object> propertyMap) {
        var name = property.mKey().dataString();
        var type = property.mType();
        var data = property.mData();

        var value = (Object) switch (type) {
            case 0x01 -> data.getFloat();
            case 0x02 -> data.getDouble();
            case 0x03 -> AIString.create(MemoryUtil.memAddress(data)).dataString();
            case 0x04 -> data.getInt();
            case 0x05 -> switch (name) {
                case Assimp.AI_MATKEY_TWOSIDED -> data.get() == 1;
                case Assimp.AI_MATKEY_SHADING_MODEL -> switch (data.getInt()) {
                    case Assimp.aiShadingMode_Flat -> ShadingMethod.FLAT;
                    case Assimp.aiShadingMode_Gouraud -> ShadingMethod.GOURAUD;
                    case Assimp.aiShadingMode_Phong -> ShadingMethod.PHONG;
                    case Assimp.aiShadingMode_Blinn -> ShadingMethod.BLINN;
                    case Assimp.aiShadingMode_Toon -> ShadingMethod.TOON;
                    case Assimp.aiShadingMode_OrenNayar -> ShadingMethod.OREN_NAYAR;
                    case Assimp.aiShadingMode_Minnaert -> ShadingMethod.MINNAERT;
                    case Assimp.aiShadingMode_CookTorrance -> ShadingMethod.COOK_TORRANCE;
                    case Assimp.aiShadingMode_NoShading -> ShadingMethod.NO_SHADING;
                    case Assimp.aiShadingMode_Fresnel -> ShadingMethod.FRESNEL;
                    case Assimp.aiShadingMode_PBR_BRDF -> ShadingMethod.PBR_BRDF;
                    default -> throw new IllegalStateException("Unexpected value: " + data.getInt());
                };
                default -> {
                    if (data.limit() == 4) yield data.getInt();
                    else yield data;
                }
            };
            default -> throw new IllegalStateException("Unexpected parameter type: " + type);
        };

        propertyMap.put(name, value);
    }
}
