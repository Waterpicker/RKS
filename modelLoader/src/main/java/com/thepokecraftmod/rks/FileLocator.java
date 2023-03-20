package com.thepokecraftmod.rks;

/**
 * Method to find and locate files based on the name of the file.
 */
@FunctionalInterface
public interface FileLocator {

    byte[] getFile(String name);
}
