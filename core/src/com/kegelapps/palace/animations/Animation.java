package com.kegelapps.palace.animations;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import com.kegelapps.palace.Director;

/**
 * Created by Ryan on 2/1/2016.
 */
public class Animation implements TweenCallback, AnimationBuilder.AnimationBuilderHelpers {
    protected AnimationFactory.AnimationType mType;
    protected boolean mPauseLogic = false;
    private AnimationStatus mStatusListener;
    protected BaseTween<Timeline> mTimeLineAnimation;
    protected BaseTween<Tween> mTweenAnimation;
    protected Animation mChild;
    protected String mDescription;
    protected Object mKillPreviousAnimation;

    public boolean isPauseLogic() {
        return mPauseLogic;
    }

    public Animation(boolean pauseLogic, AnimationStatus listener, BaseTween<Timeline> timeLineAnimation, Animation child, String description, AnimationFactory.AnimationType type, Object killPrevious) {
        mPauseLogic = pauseLogic;
        mStatusListener = listener;
        mTimeLineAnimation = timeLineAnimation;
        mChild = child;
        mDescription = description;
        mType = type;
        mKillPreviousAnimation = killPrevious;
        if (mTimeLineAnimation != null)
            mTimeLineAnimation.setCallback(this);
    }

    public Animation(Animation ani) {
        mPauseLogic = ani.mPauseLogic;
        mStatusListener = ani.mStatusListener;
        mTimeLineAnimation = ani.mTimeLineAnimation;
        mChild = ani.mChild;
        mType = ani.mType;
        mKillPreviousAnimation = ani.mKillPreviousAnimation;
    }


    public Animation setPause(boolean pause) {
        mPauseLogic = pause;
        return this;
    }

    public Animation setDescription(String desc) {
        mDescription = desc;
        return this;
    }

    public String getDescription() {
        return mDescription;
    }


    public AnimationStatus getStatusListener() {
        return mStatusListener;
    }

    public void setStatusListener(AnimationStatus mStatus) {
        this.mStatusListener = mStatus;
    }

    public void setNextAnimation(Animation ani) {
        mChild = ani;
    }

    public void Start() {
        if (mKillPreviousAnimation != null)
            Director.instance().getTweenManager().killTarget(mKillPreviousAnimation);

        if (mTimeLineAnimation != null) {
            mTimeLineAnimation.setCallback(this);
            mTimeLineAnimation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );
            System.out.print("------Starting animation: " + getDescription() + "\n");
            System.out.flush();
            mTimeLineAnimation.start(Director.instance().getTweenManager());
        }
    }

    @Override
    public void onEvent(int type, BaseTween<?> source) {
        if (type == TweenCallback.BEGIN) {
            if (mStatusListener != null)
                mStatusListener.onBegin(this);
            onBegin();
        }
        if (type == TweenCallback.END) {
            onEnd();
            if (mStatusListener != null)
                mStatusListener.onEnd(this);
        }
    }

    public void onBegin() {

    }

    public void onEnd() {

    }

    @Override
    public AnimationBuilder toBuilder() {
        AnimationBuilder builder = new AnimationBuilder(mType);
        builder.setDescription(mDescription);
        builder.setPause(mPauseLogic);
        builder.setNextAnimation(mChild);
        builder.setStatusListener(mStatusListener);
        return builder;
    }
}
