package com.thepokecraftmod.rks.animation;

import com.thepokecraftmod.rks.FileLocator;
import com.thepokecraftmod.rks.model.Model;
import com.thepokecraftmod.rks.model.animation.Animation;
import com.thepokecraftmod.rks.model.config.animation.AnimationGroup;
import com.thepokecraftmod.rks.model.config.animation.AnimationInfo;
import com.thepokecraftmod.rks.storage.AnimatedObjectInstance;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class InfoBasedAnimator {

    private final Map<String, Animation> animationCache = new HashMap<>();
    private final Map<AnimationGroup, Map<String, AnimationInfo>> info;

    public InfoBasedAnimator(Model model, FileLocator locator) {
        this.info = model.config().animations;

        info.values().stream()
                .flatMap(infoMap -> infoMap.entrySet().stream())
                .forEach(entry -> {
                    if (!animationCache.containsKey(entry.getKey())) {
                        var possibleAnims = entry.getValue().getAllPossibleAnimations();

                        for (var animation : possibleAnims) {
                            var pAnimation = ByteBuffer.wrap(locator.getFile(animation));
                            var trAnimation = com.thepokecraftmod.rks.model.animation.tranm.Animation.getRootAsAnimation(pAnimation);
                            animationCache.put(animation, new Animation(animation, trAnimation, model.skeleton()));
                        }
                    }
                });
    }

    public void animate(AnimatedObjectInstance instance, AnimationGroup group, String action, int loops, Consumer<ControlledInstance> afterAnimation) {
        if (!info.containsKey(group)) throw new RuntimeException("Missing animation group " + group);
        var animations = info.get(group);
        if (!animations.containsKey(action))
            throw new RuntimeException("Missing animation " + group + " in group " + group);
        var animationInfo = animations.get(action);
        var startAnimation = animationInfo.hasStart() ? animationCache.get(animationInfo.getStartAnimation()) : animationCache.get(animationInfo.getMainAnimation());

        if (instance.mainAnimation instanceof ControlledInstance controlledInstance) {
            if (instance != controlledInstance.instance) throw new RuntimeException("Instance Mismatch");
            controlledInstance.handleTransition(group, action, loops);
        } else {
            var newInstance = new ControlledInstance(startAnimation, instance, this, animationInfo, loops, afterAnimation);
            instance.changeMainAnimation(newInstance);
        }
    }

    public static class ControlledInstance extends AnimationInstance {

        private final AnimatedObjectInstance instance;
        private final InfoBasedAnimator animator;
        private final AnimationInfo info;
        private final int loops;
        private final Consumer<ControlledInstance> afterAnimation;
        private int i = 0;

        public ControlledInstance(Animation startAnim, AnimatedObjectInstance instance, InfoBasedAnimator animator, AnimationInfo info, int loops, Consumer<ControlledInstance> afterAnimation) {
            super(startAnim);
            this.instance = instance;
            this.animator = animator;
            this.info = info;
            this.loops = loops;
            this.afterAnimation = afterAnimation;
        }

        @Override
        public void onLoop() {
            var cache = animator.animationCache;
            var startAnimation = cache.get(info.getStartAnimation());
            var loopAnimations = info.getAllMainAnimations().stream().map(cache::get).toList();
            var endAnimation = cache.get(info.getEndAnimation());

            if (animation.equals(endAnimation)) afterAnimation.accept(this);
            else if (loopAnimations.contains(animation)) {
                i++;
                if (loops != -1 && i >= loops) {
                    if (info.hasEnd() && loopAnimations.contains(animation)) newAnimation(endAnimation);
                    else afterAnimation.accept(this);
                } else newAnimation(cache.get(info.getMainAnimation()));
            } else if (info.hasStart() && animation.equals(startAnimation))
                newAnimation(cache.get(info.getMainAnimation()));
        }

        public void newAnimation(Animation animation) {
            this.animation = animation;
            this.startTime = lastRealTime;
            this.timeAtUnpause = lastRealTime;
            this.timeAtPause = 0;
            this.currentTime = animation.getAnimationTime(0);
        }

        public void handleTransition(AnimationGroup group, String action, int loops) {
            handleTransition(group, action, loops, null);
        }

        public void handleTransition(AnimationGroup group, String action, int loops, Consumer<ControlledInstance> afterAnimation) {
            destroy();
            instance.mainAnimation = null;
            animator.animate(instance, group, action, loops, afterAnimation);
        }
    }
}
