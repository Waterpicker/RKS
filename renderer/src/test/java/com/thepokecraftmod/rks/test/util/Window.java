package com.thepokecraftmod.rks.test.util;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryUtil;

import java.util.Objects;

public class Window {
    public final String title;
    public final int width;
    public final int height;
    public long handle;

    public Window(String title, int width, int height, boolean testingPerformance) {
        this.title = title;
        this.width = width;
        this.height = height;
        try {
            System.loadLibrary("renderdoc");
        } catch (Exception ignored) {
            System.err.println("RenderDoc not loaded in tests. This is generally unwanted behaviour");
        }
        initGlfw(testingPerformance);
    }

    private void initGlfw(boolean testingPerformance) {
        Configuration.DEBUG.set(true);
        Configuration.DEBUG_LOADER.set(true);

        if (!GLFW.glfwInit()) {
            int errorCode = GLFW.glfwGetError(null);
            throw new RuntimeException("Failed to Initialize GLFW. Error Code: " + errorCode);
        }

        GLFW.glfwSetErrorCallback(this::onGlfwError);
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);

        // Setup Window
        this.handle = GLFW.glfwCreateWindow(this.width, this.height, title, MemoryUtil.NULL, MemoryUtil.NULL);

        if (this.handle == 0)
            throw new RuntimeException("Failed to create GLFW Window! If you updated your drivers recently, try restarting.");

        var videoMode = Objects.requireNonNull(GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()), "VideoMode was null");
        GLFW.glfwSetWindowPos(this.handle, (videoMode.width() - this.width) / 2, (videoMode.height() - this.height) / 2);
        GLFW.glfwMakeContextCurrent(this.handle);

        if (GL.createCapabilities(true).OpenGL45) {
            GL45C.glDebugMessageCallback(this::onGlError, MemoryUtil.NULL);
            GL45C.glEnable(GL45C.GL_DEBUG_OUTPUT);
        }

        if (testingPerformance) GLFW.glfwSwapInterval(0);
        GLFW.glfwShowWindow(this.handle);
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(this.handle);
    }

    public boolean isOpen() {
        return !GLFW.glfwWindowShouldClose(this.handle);
    }

    public void pollEvents() {
        GLFW.glfwPollEvents();
    }

    public void destroy() {
        GLFW.glfwDestroyWindow(this.handle);
    }

    private void onGlError(int glSource, int glType, int id, int severity, int length, long pMessage, long userParam) {
        var source = switch (glSource) {
            case GL43C.GL_DEBUG_SOURCE_API -> "api";
            case GL43C.GL_DEBUG_SOURCE_WINDOW_SYSTEM -> "window system";
            case GL43C.GL_SHADER_COMPILER -> "shader compiler";
            case GL43C.GL_DEBUG_SOURCE_THIRD_PARTY -> "3rd party";
            case GL43C.GL_DEBUG_SOURCE_APPLICATION -> "application";
            case GL43C.GL_DEBUG_SOURCE_OTHER -> "'other'";
            default -> throw new IllegalStateException("Unexpected value: " + glSource);
        };

        var type = switch (glType) {
            case GL43C.GL_DEBUG_TYPE_ERROR -> "error";
            case GL43C.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR -> "deprecated behaviour";
            case GL43C.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR -> "undefined behaviour";
            case GL43C.GL_DEBUG_TYPE_PORTABILITY -> "portability";
            case GL43C.GL_DEBUG_TYPE_PERFORMANCE -> "performance";
            case GL43C.GL_DEBUG_TYPE_MARKER -> "marker";
            case GL43C.GL_DEBUG_TYPE_OTHER -> "'other'";
            default -> throw new IllegalStateException("Unexpected value: " + glType);
        };

        System.out.println("[OpenGL " + source + " " + type + "] Message: " + MemoryUtil.memUTF8(pMessage));
    }

    private void onGlfwError(int error, long pDescription) {
        String description = MemoryUtil.memUTF8(pDescription);
        System.err.printf("An Error has Occurred! (%d%n) Description: %s%n", error, description);
    }
}
