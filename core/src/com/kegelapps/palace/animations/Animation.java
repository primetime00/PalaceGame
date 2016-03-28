package com.kegelapps.palace.animations;

import aurelienribon.tweenengine.*;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.graphics.CardView;
import com.kegelapps.palace.graphics.TableView;
import com.kegelapps.palace.tween.ActorAccessor;

import java.util.List;

/**
 * Created by Ryan on 2/1/2016.
 */
public class Animation implements TweenCallback, AnimationBuilder.AnimationBuilderHelpers {
    protected AnimationFactory.AnimationType mType;
    protected boolean mPauseLogic = false;
    private List<AnimationStatus> mStatusListeners;
    protected BaseTween<Timeline> mTimeLineAnimation;
    protected BaseTween<Tween> mTweenAnimation;
    protected Animation mChild;
    protected String mDescription;
    protected Object mKillPreviousAnimation;

    public boolean isPauseLogic() {
        return mPauseLogic;
    }

    public Animation(boolean pauseLogic, List<AnimationStatus> listeners, BaseTween<Timeline> timeLineAnimation, Animation child, String description, AnimationFactory.AnimationType type, Object killPrevious) {
        mPauseLogic = pauseLogic;
        mStatusListeners = listeners;
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
        mStatusListeners = ani.mStatusListeners;
        mTimeLineAnimation = ani.mTimeLineAnimation;
        mChild = ani.mChild;
        mType = ani.mType;
        mKillPreviousAnimation = ani.mKillPreviousAnimation;
    }

    public BaseTween<Timeline> getTimeLineAnimation() { return mTimeLineAnimation;}

    public static class AnimationStatusListener implements AnimationStatus {

        @Override
        public void onBegin(Animation animation) {

        }

        @Override
        public void onEnd(Animation animation) {

        }
    }


    public List<AnimationStatus> getStatusListeners() {
        return mStatusListeners;
    }

    public void addStatusListener(AnimationStatus mStatus) {
        if (!mStatusListeners.contains(mStatus))
            mStatusListeners.add(mStatus);
    }

    public void Start() {
        if (mKillPreviousAnimation != null && (mKillPreviousAnimation instanceof Actor || mKillPreviousAnimation instanceof Camera)) {
            Director.instance().getTweenManager().killTarget(mKillPreviousAnimation);

        }

        if (mTimeLineAnimation != null) {
            mTimeLineAnimation.setCallback(this);
            mTimeLineAnimation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );
            if (Logic.get().isSimulate()) {
                onEvent(TweenCallback.BEGIN, null);
                onEvent(TweenCallback.END, null);
                return;
            }
            mTimeLineAnimation.start(Director.instance().getTweenManager());
            if (mChild != null) {
                mChild.getTimeLineAnimation().pause();
                mChild.Start();
            }
        }
    }

    @Override
    public void onEvent(int type, BaseTween<?> source) {
        if (type == TweenCallback.BEGIN) {
            if (mStatusListeners != null && !mStatusListeners.isEmpty()) {
                for (AnimationStatus s : mStatusListeners) {
                    s.onBegin(this);
                }
            }
            onBegin();
        }
        if (type == TweenCallback.END) {
            onEnd();
            if (mStatusListeners != null && !mStatusListeners.isEmpty()) {
                for (AnimationStatus s : mStatusListeners) {
                    s.onEnd(this);
                }
                mStatusListeners.clear();
            }
            if (mChild != null)
                mChild.getTimeLineAnimation().resume();
                //mChild.Start();
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
        builder.getStatusListeners().addAll(mStatusListeners);
        return builder;
    }

    static public class PauseAnimation extends TweenProcessor {

        protected float mDuration, mFlipDuration;

        public PauseAnimation(float time) {
            mDuration = time;
        }

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            mAnimation.pushPause(mDuration);
            return mAnimation;
        }
    }

}
