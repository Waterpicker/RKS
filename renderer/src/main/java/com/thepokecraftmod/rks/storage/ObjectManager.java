package com.thepokecraftmod.rks.storage;

import com.thepokecraftmod.rks.animation.AnimationController;
import com.thepokecraftmod.rks.scene.RenderObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectManager {
    private final AnimationController animationController = new AnimationController();
    private final Map<RenderObject, List<ObjectInstance>> objects = new HashMap<>();

    public void update(double secondsPassed) {
        for (var objects : objects.values()) {
            if (objects.size() > 0) {
                for (var objectInstance : objects) {
                    if (objectInstance instanceof AnimatedObjectInstance animatedInstance) {
                        if (animatedInstance.mainAnimation != null) {
                            if (!animationController.playingInstances.contains((animatedInstance.mainAnimation))) {
                                animationController.playingInstances.add(animatedInstance.mainAnimation);
                            }
                        }

                        if (animatedInstance.facialAnimation != null) {
                            if (!animationController.playingInstances.contains((animatedInstance.facialAnimation))) {
                                animationController.playingInstances.add(animatedInstance.facialAnimation);
                            }
                        }
                    }
                }
            }
        }

        animationController.render(secondsPassed);
    }

    public void render() {
        for (var entry : objects.entrySet()) {
            var object = entry.getKey();

            if (!object.hidden) {
                object.update();
                object.render(entry.getValue());
            }
        }
    }

    public <T extends ObjectInstance> T add(@NotNull RenderObject object, @NotNull T instance) {
        instance.link(object);
        objects.putIfAbsent(object, new ArrayList<>());
        objects.get(object).add(instance);
        return instance;
    }
}
