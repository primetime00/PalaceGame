package com.kegelapps.palace.animations;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
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

    private CardView mCard;
    private int mHandID;
    private TableView mTable;

    private TweenCalculator mCalc;

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

    public Animation build() {
        switch (mAnimationType) {
            case CARD:
                CardAnimation ani = new CardAnimation(mPauseLogic, mStatusListener, mCalc.calculate(this), mChild, mDescription, mAnimationType, mKillPreviousAnimation, mCard, mHandID, mTable);
                onBuild(ani);
                return ani;
                //return new CardAnimation(mPauseLogic, mStatusListener, mCalc.calculate(this), mChild, mDescription, mAnimationType, mKillPreviousAnimation, mCard, mHandID, mTable);
            default:
                break;
        }
        return null;
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
        return this;
    }

    public void onBuild(Animation ani) {

    }
}
