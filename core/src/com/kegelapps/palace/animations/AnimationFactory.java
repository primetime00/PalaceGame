package com.kegelapps.palace.animations;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.TweenCallback;
import com.kegelapps.palace.engine.Logic;

/**
 * Created by Ryan on 2/1/2016.
 */
public class AnimationFactory implements AnimationStatus {

    private static AnimationFactory instance;

    private int mPauseCount;

    public enum AnimationType {
        CARD,
        CAMERA
    }

    private AnimationFactory () {
        instance = this;
        mPauseCount = 0;
    }

    public static AnimationFactory get() {
        if (instance == null)
            instance = new AnimationFactory();
        return instance;
    }

    public AnimationBuilder createAnimationBuilder(AnimationType type) {
        AnimationBuilder builder = new AnimationBuilder(type) {
            @Override
            public void onBuild(Animation ani) {
                ani.setStatusListener(AnimationFactory.get());
            }
        };
        return builder;
    }

    private void pauseDecrement() {
        mPauseCount--;
        if (mPauseCount <= 0) {
            mPauseCount = 0;
            Logic.get().Pause(false);
        }
    }

    private void pauseIncrement() {
        mPauseCount++;
        Logic.get().Pause(true);
    }

    @Override
    public void onBegin(Animation animation) {
        if (animation.isPauseLogic())
            pauseIncrement();
    }

    @Override
    public void onEnd(Animation animation) {
        if (animation.isPauseLogic())
            pauseDecrement();
        animation.setStatusListener(null);
    }
}
