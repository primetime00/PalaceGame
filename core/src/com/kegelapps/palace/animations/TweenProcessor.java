package com.kegelapps.palace.animations;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;

/**
 * Created by ryan on 2/5/16.
 */
abstract public class TweenProcessor implements AnimationBuilder.TweenCalculator {

    protected Timeline mAnimation;
    protected float mStartDelay;
    protected Runnable mStartDelayCallback;

    public BaseTween<Timeline> process(final AnimationBuilder builder) {
        mAnimation = Timeline.createSequence();
        mStartDelay = builder.getStartDelay();
        mStartDelayCallback = builder.getStartDelayCallback();
        if (mStartDelay > 0.0f) {
            mAnimation.pushPause(mStartDelay);
        }
        if (mStartDelayCallback != null) {
            mAnimation.push(Tween.call(new TweenCallback() {
                @Override
                public void onEvent(int type, BaseTween<?> source) {
                    mStartDelayCallback.run();
                }
            }));
        }
        mAnimation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END);
        return calculate(builder);
    }
}
