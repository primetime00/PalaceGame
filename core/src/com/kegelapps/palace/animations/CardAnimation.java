package com.kegelapps.palace.animations;

import aurelienribon.tweenengine.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.graphics.*;
import com.kegelapps.palace.graphics.utils.CardUtils;
import com.kegelapps.palace.graphics.utils.HandUtils;
import com.kegelapps.palace.tween.CardAccessor;

import java.util.List;

/**
 * Created by Ryan on 12/21/2015.
 */
public class CardAnimation extends Animation {


    private CardView mCard;
    private int mHandID;
    private TableView mTable;

    public CardAnimation(boolean pauseLogic, List<AnimationStatus> listeners, BaseTween<Timeline> timeLineAnimation, Animation child, String description, AnimationFactory.AnimationType type, Object killObj, CardView mCard, int mHandID, TableView mTable) {
        super(pauseLogic, listeners, timeLineAnimation, child, description, type, killObj);
        this.mCard = mCard;
        this.mHandID = mHandID;
        this.mTable = mTable;
        this.mType = type;
    }

    public CardAnimation(CardAnimation ani) {
        super(ani.mPauseLogic, ani.getStatusListeners(), ani.mTimeLineAnimation, ani.mChild, ani.mDescription, ani.mType, ani.mKillPreviousAnimation);
        mCard = ani.mCard;
        mTable = ani.mTable;
        mHandID = ani.mHandID;
    }

    static public class DrawToActive extends TweenProcessor {

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CardView mCard = builder.getCard();
            TableView mTable = builder.getTable();
            mCard.setSide(CardView.Side.BACK);
            Vector2 vec = mTable.getPlayView().GetNextCardPosition();
            mAnimation.push(Tween.set(mCard, CardAccessor.POSITION_XY).target(mTable.getDeck().getX(), mTable.getDeck().getY()));
            mAnimation.beginParallel();
            mAnimation.push(Tween.to(mCard, CardAccessor.POSITION_XY, 1.0f).target(vec.x, vec.y).ease(TweenEquations.easeInOutExpo));
            mAnimation.push(Tween.call(new TweenCallback() {
                @Override
                public void onEvent(int type, BaseTween<?> source) {
                    mCard.setSide(CardView.Side.FRONT);
                }
            }).delay(0.8f));
            mAnimation.end();
            return mAnimation;
        }
    }

    static public class DealToHand extends TweenProcessor {

        protected boolean mRotation = true;

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CardView mCard = builder.getCard();
            TableView mTable = builder.getTable();
            int mHandID = builder.getHandID();
            mCard.setPosition(builder.getTable().getDeck().getX(), builder.getTable().getDeck().getY());
            mAnimation.push(Tween.set(mCard, CardAccessor.POSITION_XY).target(mTable.getDeck().getX(), mTable.getDeck().getY()));
            HandView hand = mTable.getHands().get(mHandID);

            Rectangle activeRect = hand.getActivePosition();
            Rectangle hiddenRect = hand.getHiddenPosition(0);
            float cardSize = mCard.getMaxCardSize();
            int roundSize = MathUtils.round(cardSize) / 10;
            int angleVariation = (MathUtils.random(roundSize) - (roundSize/2))*10;
            float powerVariation = (hiddenRect.getHeight() - MathUtils.random(hiddenRect.getHeight()))/2.0f;
            float rotation;
            float duration = 0.5f;


            mAnimation.beginParallel();

            HandUtils.HandSide side = HandUtils.IDtoSide(hand.getHand().getID(), mTable);

            if (mRotation) {
                rotation = (MathUtils.random(36) - 18)*10;
            } else {
                rotation = (side == HandUtils.HandSide.SIDE_TOP || side == HandUtils.HandSide.SIDE_BOTTOM) ? 0.0f : 90.0f;
            }

            float x = 0;
            float y = 0;
            if (side == HandUtils.HandSide.SIDE_BOTTOM || side == HandUtils.HandSide.SIDE_TOP) {
                x = mTable.getDeck().getX() + angleVariation;
                y = hiddenRect.getY() + (powerVariation * (side == HandUtils.HandSide.SIDE_TOP ? 1 : -1));
            } else {
                x = hiddenRect.getX() + (powerVariation * (side == HandUtils.HandSide.SIDE_RIGHT ? 1 : -1));
                y = mTable.getDeck().getY()+angleVariation;
            }
            mAnimation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(x, y).ease(TweenEquations.easeInOutExpo));
            mAnimation.push(Tween.to(mCard, CardAccessor.ROTATION, duration).target(rotation));

            mAnimation.end();
            mAnimation.setUserData(hand);
            return mAnimation;
        }
    }

    static public class LineUpHiddenCards extends TweenProcessor {

        private int mPosition;

        public LineUpHiddenCards(int pos) {
            mPosition = pos;
        }

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CardView mCard = builder.getCard();
            TableView mTable = builder.getTable();
            int mHandID = builder.getHandID();
            float varianceDelay = MathUtils.random(10) / 10.0f;
            mAnimation.pushPause(varianceDelay);
            mAnimation.beginParallel();

            Vector3 pos;

            float duration = 0.1f;
            int handPos = mPosition;
            if (handPos < 0) {
                return null;
            }
            Rectangle rect = mTable.getHands().get(mHandID).getHiddenPosition(handPos);
            pos = HandUtils.LineUpHiddenCard(mCard, mTable, mHandID, rect);
            mAnimation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(pos.x, pos.y));
            mAnimation.push(Tween.to(mCard, CardAccessor.ROTATION, duration).target(pos.z));
            return mAnimation.end();
        }
    }

    static public class SelectEndCard extends TweenProcessor {

        private int mPosition;
        public SelectEndCard(int pos) {
            this.mPosition = pos;
        }

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CardView mCard = builder.getCard();
            TableView mTable = builder.getTable();
            int mHandID = builder.getHandID();
            if (mPosition < 0) {
                throw new RuntimeException("Position is out of range!");
            }
            mAnimation.beginParallel();
            Rectangle rect = mTable.getHands().get(mHandID).getHiddenPosition(mPosition);
            float ov = CardUtils.getCardWidth() * mTable.getHands().get(mHandID).getEndCardOverlapPercent();
            float x = rect.x;
            float y = rect.y;


            float duration = 0.2f;
            float rotDiff = (mCard.getOriginY() - mCard.getOriginX())/1;
            float sideRot = 90.0f;
            switch (mHandID) {
                default:
                case 0: //bottom
                    x += ov;
                    y += ov;
                    break;
                case 1: //left
                    x+= (-(rotDiff*MathUtils.sinDeg(-sideRot))+ov);
                    y+= (rotDiff*MathUtils.sinDeg(-sideRot))+ov;
                    break;
                case 2: //top
                    x += -ov;
                    y += -ov;
                    break;
                case 3: //right
                    x += (rotDiff*MathUtils.sinDeg(sideRot))-ov;
                    y += -((rotDiff*MathUtils.sinDeg(sideRot))-ov);
                    break;
            }
            mAnimation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(x,y));
            mAnimation.push(Tween.call(new TweenCallback() {
                @Override
                public void onEvent(int type, BaseTween<?> source) {
                    mCard.setSide(CardView.Side.FRONT);
                }
            }).delay(0.1f));
            return mAnimation.end();
        }
    }

    static public class LineUpActiveCard extends TweenProcessor {

        private int mIndex;
        public LineUpActiveCard(int index) {
            this.mIndex = index;
        }

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CardView mCard = builder.getCard();
            TableView mTable = builder.getTable();
            int mHandID = builder.getHandID();
            mAnimation.beginParallel();
            TweenCallback mFlipCallback = new TweenCallback() {
                @Override
                public void onEvent(int type, BaseTween<?> source) {
                    mCard.setSide(CardView.Side.FRONT);
                }
            };
            Vector3 pos;

            pos = HandUtils.LineUpActiveCard(mIndex, mCard, mTable, mHandID, mTable.getHands().get(mHandID).getActivePosition(), mTable.getHands().get(mHandID).getCardOverlapPercent());

            float duration = 0.1f;
            mAnimation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(pos.x, pos.y));
            mAnimation.push(Tween.to(mCard, CardAccessor.ROTATION, duration).target(pos.z));

            if (mTable.getHands().get(mHandID).getHand().getType() == Hand.HandType.HUMAN)
                mAnimation.push(Tween.call(mFlipCallback).delay(0.2f));
            return mAnimation.end();
        }
    }

    static public class PlayFailedCard extends TweenProcessor {

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CardView mCard = builder.getCard();
            TableView mTable = builder.getTable();
            int mHandID = builder.getHandID();
            float pos_x = mCard.getX();
            float pos_y = mCard.getY();
            float x = mTable.getPlayView().getX();
            float y = mTable.getPlayView().getY();
            mAnimation.beginParallel();
            TweenEquation eq = TweenEquations.easeInOutQuart;
            float duration = 0.3f;
            float div = 3.0f;
            float rotDiff = (mCard.getOriginY() - mCard.getOriginX())/1;
            float sideRot = 90.0f;
            mAnimation.push(Tween.set(mCard, CardAccessor.POSITION_XY).target(pos_x, pos_y));
            switch (mHandID) {
                default:
                case 0: //bottom
                    y = y/div;
                    break;
                case 1: //left
                    x += -(((rotDiff*MathUtils.sinDeg(-sideRot)))/div);
                    y +=  (rotDiff*MathUtils.sinDeg(-sideRot));
                    pos_x += -(rotDiff*MathUtils.sinDeg(-sideRot));
                    pos_y += (rotDiff*MathUtils.sinDeg(-sideRot));
                    break;
                case 2: //top
                    y = y/div;
                    break;
                case 3: //right
                    x += ((rotDiff*MathUtils.sinDeg(sideRot))/div);
                    y += -(rotDiff*MathUtils.sinDeg(sideRot));
                    pos_x += (rotDiff*MathUtils.sinDeg(sideRot));
                    pos_y += -(rotDiff*MathUtils.sinDeg(sideRot));
                    break;
            }
            mAnimation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(x,y).ease(eq)).end();
            mAnimation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(pos_x,pos_y).ease(eq));
            return mAnimation;
        }
    }

    static public class PlaySuccessCard extends TweenProcessor {

        protected float mDuration, mFlipDuration;

        public PlaySuccessCard() {
            mDuration = 0.3f;
            mFlipDuration = mDuration / 3.0f;
        }

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CardView mCard = builder.getCard();
            TableView mTable = builder.getTable();
            int mHandID = builder.getHandID();
            float pos_x = mCard.getX();
            float pos_y = mCard.getY();
            Vector2 vec = mTable.getPlayView().GetNextCardPosition();
            float x = vec.x;
            float y = vec.y;

            mAnimation.beginParallel();
            TweenEquation eq = TweenEquations.easeInOutQuart;
            float duration = mDuration;
            mAnimation.push(Tween.set(mCard, CardAccessor.POSITION_XY).target(pos_x, pos_y));
            mAnimation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(x, y).ease(eq));
            mAnimation.push(Tween.to(mCard, CardAccessor.ROTATION, duration).target(0.0f));
            mAnimation.push(Tween.call(new TweenCallback() {
                @Override
                public void onEvent(int type, BaseTween<?> source) {
                    mCard.setSide(CardView.Side.FRONT);
                }
            }).delay(mFlipDuration)).end();
            return mAnimation;
        }
    }

    static public class PlaySuccessBurnCard extends PlaySuccessCard {

        public PlaySuccessBurnCard() {
            mDuration = 1.0f;
            mFlipDuration = 0.3f / 3.0f;
        }
    }

    static public class SelectPendingCard extends TweenProcessor {

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CardView mCard = builder.getCard();
            int mHandID = builder.getHandID();
            float x = mCard.getX();
            float y = mCard.getY();
            float raise = CardUtils.getCardTextureHeight() * 0.15f;
            float duration = 0.5f;
            TweenEquation eq = TweenEquations.easeInOutQuart;
            float rotDiff = (mCard.getOriginY() - mCard.getOriginX()) / 1;
            float sideRot = 90.0f;
            mAnimation.push(Tween.set(mCard, CardAccessor.POSITION_XY).target(x, y));

            switch (mHandID) {
                default:
                case 0: //bottom
                    y += raise;
                    break;
                case 1: //left
                    x += -((rotDiff * MathUtils.sinDeg(-sideRot))+raise);
                    y += (rotDiff * MathUtils.sinDeg(-sideRot));
                    break;
                case 2: //top
                    y -= raise;
                    break;
                case 3: //right
                    x += (rotDiff * MathUtils.sinDeg(sideRot)) - raise;
                    y += -(rotDiff * MathUtils.sinDeg(sideRot));
                    break;
            }
            mAnimation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(x, y).ease(eq));
            //should we bounce!?
            AnimationBuilder nextBuilder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD).fromBuilder(builder);
            nextBuilder.setTweenCalculator(new BounceCard());
            builder.setNextAnimation(nextBuilder.build());
            return mAnimation;
        }
    }

    static public class BounceCard extends TweenProcessor {

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CardView mCard = builder.getCard();
            int mHandID = builder.getHandID();
            float fall = CardUtils.getCardTextureHeight() * 0.10f;
            float duration = 0.5f;
            TweenEquation eq = TweenEquations.easeInCubic;
            float x, y;

            switch (mHandID) {
                default:
                case 0: //bottom
                    x = 0; y = -fall;
                    break;
                case 1: //left
                    x = -fall; y = 0;
                    break;
                case 2: //top
                    x = 0; y = fall;
                    break;
                case 3: //right
                    x = fall; y = 0;
                    break;
            }
            mAnimation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).targetRelative(x, y).ease(eq));
            mAnimation.repeatYoyo(Tween.INFINITY, 0);
            return mAnimation;
        }
    }

    static public class BurnCard extends TweenProcessor {

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CardView mCard = builder.getCard();
            if (builder.getCamera() == null)
                throw new RuntimeException("This animation needs the camera!");
            float angle = (float)(22.5 * (int)((Math.random() * 15.0f)));
            float h = builder.getCamera().viewportHeight;
            float w = builder.getCamera().viewportWidth;
            float length = (float)Math.hypot(w, h);
            float x = (float) (length * Math.cos(Math.toRadians(angle)));
            float y = (float) (length * Math.sin(Math.toRadians(angle)));
            mAnimation.pushPause(0.5f);
            mAnimation.push(Tween.to(mCard, CardAccessor.POSITION_XY, 1.5f).target(x, y).ease(TweenEquations.easeInOutQuad));
            return mAnimation;
        }
    }

    static public class DrawEndTurnCard extends DealToHand {
        public DrawEndTurnCard() {
            mRotation = false;
        }
    }

    @Override
    public AnimationBuilder toBuilder() {
        return super.toBuilder().setCard(mCard).setTable(mTable).setHandID(mHandID);
    }

}
