package com.thepokecraftmod.silvally.utils;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        var running = args[0];
        var newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        switch (running) {
            case "emissionGenerator" -> EmissionGenerator.main(newArgs);
            case "textureOptimiser" -> TextureOptimiser.main(newArgs);
        }
    }
}
