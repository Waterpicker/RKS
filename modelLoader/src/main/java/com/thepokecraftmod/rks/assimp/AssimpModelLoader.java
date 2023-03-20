package com.thepokecraftmod.rks.assimp;

import com.thepokecraftmod.rks.FileLocator;
import com.thepokecraftmod.rks.model.PokemonModel;
import org.lwjgl.assimp.AIFile;
import org.lwjgl.assimp.AIFileIO;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.system.MemoryUtil;

public class AssimpModelLoader {

    public static PokemonModel load(String name, FileLocator locator, int extraFlags) {
        var fileIo = AIFileIO.create()
                .OpenProc((pFileIO, pFileName, openMode) -> {
                    var fileName = MemoryUtil.memUTF8(pFileName);
                    var bytes = locator.getFile(fileName);
                    var data = MemoryUtil.memAlloc(bytes.length);
                    data.put(bytes);
                    data.flip();

                    return AIFile.create()
                            .ReadProc((pFile, pBuffer, size, count) -> {
                                long max = Math.min(data.remaining(), size * count);
                                MemoryUtil.memCopy(MemoryUtil.memAddress(data) + data.position(), pBuffer, max);
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
                    try (var aiFile = AIFile.create(pFile)) {
                        aiFile.ReadProc().free();
                        aiFile.SeekProc().free();
                        aiFile.FileSizeProc().free();
                    }
                });

        var scene = Assimp.aiImportFileEx(name, Assimp.aiProcess_Triangulate | Assimp.aiProcess_JoinIdenticalVertices | extraFlags, fileIo);
        var result = readScene(scene);
        Assimp.aiReleaseImport(scene);
        return result;
    }

    private static PokemonModel readScene(AIScene scene) {
        return null;
    }
}
