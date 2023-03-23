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
        var file = AssimpModelLoader.load("C:\\Users\\allegra\\Documents\\Github\\hYdos\\RKS\\modelLoader\\src\\test\\resources\\testmodel\\model.gltf", PokemonModelLoadingTest::fileSystemResolver, 0);
        System.out.println("model loaded in " + (System.currentTimeMillis() - startTime) + "ms");
        FILE_CACHE.clear();
    }

    private static byte[] fileSystemResolver(String fileName) {
        return FILE_CACHE.computeIfAbsent(fileName, s -> {
            try {
                var cleanString = s.replace("\\", "/").replace("//", "/");
                return Files.readAllBytes(Paths.get(cleanString));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
