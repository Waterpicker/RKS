package test;

import com.thepokecraftmod.rks.assimp.AssimpModelLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PokemonModelLoadingTest {
    private static final Map<String, byte[]> FILE_CACHE = new HashMap<>();

    public static void main(String[] args) {
        var startTime = System.currentTimeMillis();
        var file = AssimpModelLoader.load("C:\\Users\\hydos\\Desktop\\tmp\\model format testing\\model.gltf", PokemonModelLoadingTest::fileSystemResolver, 0);
        FILE_CACHE.clear();
        System.out.println((System.currentTimeMillis() - startTime) + "ms model loaded");
    }

    private static byte[] fileSystemResolver(String fileName) {
        return FILE_CACHE.computeIfAbsent(fileName, s -> {
            try {
                return Files.readAllBytes(Paths.get(s));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
