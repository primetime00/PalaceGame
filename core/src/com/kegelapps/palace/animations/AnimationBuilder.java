package com.kegelapps.palace.animations;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import com.kegelapps.palace.graphics.CardCamera;
import com.kegelapps.palace.graphics.CardView;
import com.kegelapps.palace.graphics.TableView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryan on 2/1/2016.
 */
public class AnimationBuilder {

    private AnimationFactory.AnimationType mAnimationType;

    private boolean mPauseLogic = false;
    private List<AnimationStatus> mStatusListeners;
    private Runnable mAnimationAfterDelay;
    private Animation mChild;
    private String mDescription;
    private Object mKillPreviousAnimation;
    private float mStartDelay;
    private TweenProcessor mCalc;

    //Card specific animation
    private CardView mCard;
    private int mHandID;
    private TableView mTable;

    //Camera specific animation
    private CardCamera.CameraSide mCameraSide;
    private CardCamera mCamera;



    public CardView getCard() {
        return mCard;
    }

    public TableView getTable() {
        return mTable;
    }

    public int getHandID() {
        return mHandID;
    }

    public float getStartDelay() { return mStartDelay;}

    public AnimationBuilder setStartDelay(float delay, Runnable callback) {
        mStartDelay = delay;
        mAnimationAfterDelay = callback;
        return this;
    }

    public AnimationBuilder setStartDelay(float delay) {
        return setStartDelay(delay, null);
    }

    public Runnable getStartDelayCallback() {
        return mAnimationAfterDelay;
    }


    public interface AnimationBuilderHelpers {
        AnimationBuilder toBuilder();
    }

    public interface TweenCalculator {
        BaseTween<Timeline> calculate(AnimationBuilder builder);
    }

    public AnimationBuilder killPreviousAnimation(Object o) {
        mKillPreviousAnimation = o;
        return this;
    }


    private AnimationBuilder() {}

    public AnimationBuilder(AnimationFactory.AnimationType type) {
        mAnimationType = type;
        mStatusListeners = new ArrayList<>();
        mKillPreviousAnimation = false;
        mStartDelay = 0.0f;
    }


    public AnimationBuilder setPause(boolean pause) {
        mPauseLogic = pause;
        return this;
    }

    public AnimationBuilder setDescription(String desc) {
        mDescription = desc;
        return this;
    }

    public AnimationBuilder addStatusListener(AnimationStatus mStatus) {
        if (!mStatusListeners.contains(mStatus))
            mStatusListeners.add(mStatus);
        return this;
    }

    public List<AnimationStatus> getStatusListeners() {
        return mStatusListeners;
    }

    public AnimationBuilder setCard(CardView card) {
        mCard = card;
        return this;
    }


    public AnimationBuilder setHandID(int id) {
        mHandID = id;
        return this;
    }

    public AnimationBuilder setNextAnimation(Animation animation) {
        mChild = animation;
        return this;
    }

    public AnimationBuilder setTable(TableView table) {
        mTable = table;
        return this;
    }

    public AnimationBuilder setTweenCalculator(TweenProcessor calc) {
        mCalc = calc;
        return this;
    }

    public AnimationBuilder setCamera(CardCamera mCamera) {
        this.mCamera = mCamera;
        return this;
    }

    public AnimationBuilder setCameraSide(CardCamera.CameraSide mSide) {
        this.mCameraSide = mSide;
        return this;
    }

    public CardCamera getCamera() {
        return mCamera;
    }

    public CardCamera.CameraSide getCameraSide() {
        return mCameraSide;
    }

    public Animation build() {
        Animation ani = null;
        switch (mAnimationType) {
            case CARD:
                ani = new CardAnimation(mPauseLogic, mStatusListeners, mCalc.process(this), mChild, mDescription, mAnimationType, mKillPreviousAnimation, mCard, mHandID, mTable);
                break;
            case CAMERA:
                ani = new CameraAnimation(mPauseLogic, mStatusListeners, mCalc.process(this), mChild, mDescription, mAnimationType, mKillPreviousAnimation, mCamera, mCameraSide, mTable);
                break;
            default:
                break;
        }
        if (ani != null) {
            onBuild(ani);
        }

/*        mStatusListeners.clear();
        mAnimationAfterDelay = null;
        mChild = null;
        mKillPreviousAnimation = null;
        mCalc = null;

        mCard = null;
        mTable = null;

        mCamera = null;*/

        return ani;
    }

    public AnimationBuilder fromBuilder(AnimationBuilder builder) {
        mPauseLogic = false;
        mStatusListeners = builder.mStatusListeners;
        mCalc = builder.mCalc;
        mChild = builder.mChild;
        mDescription = builder.mDescription;
        mCard = builder.mCard;
        mHandID = builder.mHandID;
        mTable = builder.mTable;

        mCamera = builder.mCamera;
        mCameraSide = builder.mCameraSide;
        return this;
    }

    public void onBuild(Animation ani) {

    }
}
