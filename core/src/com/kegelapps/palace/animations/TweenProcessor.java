package com.kegelapps.palace.animations;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;

/**
 * Created by ryan on 2/5/16.
 */
public class TweenProcessor implements AnimationBuilder.TweenCalculator {

    protected Timeline mAnimation;

    private BaseTween<Timeline> process(AnimationBuilder builder) {
        mAnimation = Timeline.createSequence();
        if (builder.getStartDelay() > 0.0f) {
            mAnimation.pushPause(builder.getStartDelay());
        }
        return calculate(builder);
    }

    @Override
    public BaseTween<Timeline> calculate(AnimationBuilder builder) {
        return null;
    }
}
