package com.kegelapps.palace.animations;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import com.kegelapps.palace.graphics.CardCamera;
import com.kegelapps.palace.graphics.CardView;
import com.kegelapps.palace.graphics.TableView;

/**
 * Created by Ryan on 2/1/2016.
 */
public class AnimationBuilder {

    private AnimationFactory.AnimationType mAnimationType;

    private boolean mPauseLogic = false;
    private AnimationStatus mStatusListener;
    private Animation mChild;
    private String mDescription;
    private Object mKillPreviousAnimation;
    private TweenCalculator mCalc;

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
        mKillPreviousAnimation = false;
    }


    public AnimationBuilder setPause(boolean pause) {
        mPauseLogic = pause;
        return this;
    }

    public AnimationBuilder setDescription(String desc) {
        mDescription = desc;
        return this;
    }

    public AnimationBuilder setStatusListener(AnimationStatus mStatus) {
        this.mStatusListener = mStatus;
        return this;
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

    public AnimationBuilder setTweenCalculator(TweenCalculator calc) {
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
                ani = new CardAnimation(mPauseLogic, mStatusListener, mCalc.calculate(this), mChild, mDescription, mAnimationType, mKillPreviousAnimation, mCard, mHandID, mTable);
                break;
            case CAMERA:
                ani = new CameraAnimation(mPauseLogic, mStatusListener, mCalc.calculate(this), mChild, mDescription, mAnimationType, mKillPreviousAnimation, mCamera, mCameraSide, mTable);
                break;
            default:
                break;
        }
        if (ani != null) {
            onBuild(ani);
        }
        return ani;
    }

    public AnimationBuilder fromBuilder(AnimationBuilder builder) {
        mPauseLogic = false;
        mStatusListener = builder.mStatusListener;
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
