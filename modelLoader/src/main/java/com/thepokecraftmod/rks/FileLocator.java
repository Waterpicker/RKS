package com.thepokecraftmod.rks;

import java.nio.ByteBuffer;

/**
 * Method to find and locate files based on the name of the file.
 */
@FunctionalInterface
public interface FileLocator {

    byte[] getFile(String name);

    /**
     * Expects a Native Byte Buffer
     */
    default ByteBuffer readImage(String name) {
        return null;
    }
}
