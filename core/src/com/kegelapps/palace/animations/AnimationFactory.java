package com.kegelapps.palace.animations;

import com.kegelapps.palace.Director;
import com.kegelapps.palace.Resettable;
import com.kegelapps.palace.engine.Logic;

/**
 * Created by Ryan on 2/1/2016.
 */
public class AnimationFactory implements AnimationStatus, Resettable {

    private static AnimationFactory instance;

    private int mPauseCount;

    public enum AnimationType {
        CARD,
        COIN,
        CAMERA,
        PAUSE
    }

    private AnimationFactory () {
        instance = this;
        mPauseCount = 0;
        Director.instance().addResetter(this);
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
                ani.addStatusListener(AnimationFactory.get());
            }
        };
        return builder;
    }

    public void pauseDecrement() {
        mPauseCount--;
        if (mPauseCount <= 0) {
            mPauseCount = 0;
            Logic.get().Pause(false);
        }
    }

    public void pauseIncrement() {
        mPauseCount++;
        Logic.get().Pause(true);
    }

    @Override
    public void onBegin(Animation animation) {
        if (animation.isPauseLogic()) {
            pauseIncrement();
        }
    }

    @Override
    public void onEnd(Animation animation) {
        if (animation.isPauseLogic()) {
            pauseDecrement();
        }
    }

    @Override
    public void Reset(boolean newGame) {
        mPauseCount = 0;
    }

}
