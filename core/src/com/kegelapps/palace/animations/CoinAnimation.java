package com.kegelapps.palace.animations;

import aurelienribon.tweenengine.*;
import com.badlogic.gdx.math.Vector2;
import com.kegelapps.palace.graphics.CardView;
import com.kegelapps.palace.graphics.CoinView;
import com.kegelapps.palace.graphics.HandView;
import com.kegelapps.palace.graphics.TableView;
import com.kegelapps.palace.tween.ActorAccessor;

import java.util.List;

/**
 * Created by keg45397 on 2/24/2016.
 */
public class CoinAnimation extends Animation {

    private CoinView mCoin;
    private int mHandID;
    private TableView mTable;

    public CoinAnimation(boolean pauseLogic, List<AnimationStatus> listeners, BaseTween<Timeline> timeLineAnimation, Animation child, String description, AnimationFactory.AnimationType type, Object killObj, CoinView mCoin, int mHandID, TableView mTable) {
        super(pauseLogic, listeners, timeLineAnimation, child, description, type, killObj);
        this.mCoin = mCoin;
        this.mHandID = mHandID;
        this.mTable = mTable;
        this.mType = type;
    }

    static public class FlyInCoin extends TweenProcessor {

        protected float mDuration;
        protected boolean mRotation;

        public FlyInCoin(float duration, boolean rotate) {
            mDuration = duration;
            mRotation = rotate;
        }

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CoinView mCoin = builder.getCoin();
            final int maxRotateDegree = 60;
            final int rotateSections = 5;
            TableView mTable = builder.getTable();
            int mHandID = builder.getHandID();
            HandView hand = mTable.getHand(mHandID);
            if (hand == null)
                throw new RuntimeException("FlyInCoin requires a valid HandView.");
            Vector2 pos = hand.getHiddenPosition(1).getCenter(new Vector2());
            int overRotate = (int)(Math.random() * maxRotateDegree/rotateSections)+1; //1 - 12
            pos.y -= mCoin.getHeight()/2.0f;
            pos.x -= mCoin.getWidth()/2.0f;
            float rotation = 720 + (overRotate*rotateSections) -  maxRotateDegree/2;

            mAnimation.beginParallel();
            TweenEquation eq = TweenEquations.easeInOutQuart;
            float duration = mDuration;
            mAnimation.push(Tween.to(mCoin, ActorAccessor.POSITION_XY, duration).target(pos.x, pos.y).ease(eq));
            if (mRotation) {
                mAnimation.push(Tween.to(mCoin, ActorAccessor.ROTATION, duration).target(rotation));
            }
            return mAnimation.end();
        }
    }



}
