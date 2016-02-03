package com.kegelapps.palace.animations;

import aurelienribon.tweenengine.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.graphics.*;
import com.kegelapps.palace.tween.CardAccessor;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.removeAction;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by Ryan on 12/21/2015.
 */
public class CardAnimation extends Animation {


    private CardView mCard;
    private int mHandID;
    private TableView mTable;

    public CardAnimation(boolean pauseLogic, AnimationStatus listener, BaseTween<Timeline> timeLineAnimation, Animation child, String description, AnimationFactory.AnimationType type, Object killObj, CardView mCard, int mHandID, TableView mTable) {
        super(pauseLogic, listener, timeLineAnimation, child, description, type, killObj);
        this.mCard = mCard;
        this.mHandID = mHandID;
        this.mTable = mTable;
        this.mType = type;
    }

    public CardAnimation(CardAnimation ani) {
        super(ani.mPauseLogic, ani.getStatusListener(), ani.mTimeLineAnimation, ani.mChild, ani.mDescription, ani.mType, ani.mKillPreviousAnimation);
        mCard = ani.mCard;
        mTable = ani.mTable;
        mHandID = ani.mHandID;
    }

    static public class DrawToActive implements AnimationBuilder.TweenCalculator {

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CardView mCard = builder.getCard();
            TableView mTable = builder.getTable();
            mCard.setSide(CardView.Side.BACK);
            Vector2 vec = mTable.getPlayView().GetNextCardPosition();
            Timeline animation = Timeline.createSequence();
            //animation.setCallback(builder);
            animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );

            animation.push(Tween.set(mCard, CardAccessor.POSITION_XY).target(mTable.getDeck().getX(), mTable.getDeck().getY()));
            animation.beginParallel();
            animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, 1.0f).target(vec.x, vec.y).ease(TweenEquations.easeInOutExpo));
            animation.push(Tween.call(new TweenCallback() {
                @Override
                public void onEvent(int type, BaseTween<?> source) {
                    mCard.setSide(CardView.Side.FRONT);
                }
            }).delay(0.8f));
            animation.end();
            return animation;
        }
    }

    static public class DealToHand implements AnimationBuilder.TweenCalculator {

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder ani) {
            final CardView mCard = ani.getCard();
            TableView mTable = ani.getTable();
            int mHandID = ani.getHandID();
            Timeline animation = Timeline.createSequence();
            //animation.setCallback(this);
            animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );

            animation.push(Tween.set(mCard, CardAccessor.POSITION_XY).target(mTable.getDeck().getX(), mTable.getDeck().getY()));
            HandView hand = mTable.getHands().get(mHandID);

            Rectangle activeRect = hand.getActivePosition();
            Rectangle hiddenRect = hand.getHiddenPosition(0);
            float cardSize = mCard.getMaxCardSize();
            int roundSize = MathUtils.round(cardSize) / 10;
            int angleVariation = (MathUtils.random(roundSize) - (roundSize/2))*10;
            float powerVariation = MathUtils.random(activeRect.getHeight()) - activeRect.getHeight();
            powerVariation = 0;
            float rotation = (MathUtils.random(36) - 18)*10;
            float duration = 0.5f;

            animation.beginParallel();
            switch (hand.getHand().getID()) {
                default:
                case 0: //bottom
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(mTable.getDeck().getX()+angleVariation, hiddenRect.getY() + powerVariation).ease(TweenEquations.easeInOutExpo));
                    animation.push(Tween.to(mCard, CardAccessor.ROTATION, duration).target(rotation));
                    break;
                case 1: //left
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(hiddenRect.getX() + powerVariation, mTable.getDeck().getY()+angleVariation).ease(TweenEquations.easeInOutExpo));
                    animation.push(Tween.to(mCard, CardAccessor.ROTATION, duration).target(rotation));
                    break;
                case 2: //top
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(mTable.getDeck().getX()+angleVariation, hiddenRect.getY() + powerVariation).ease(TweenEquations.easeInOutExpo));
                    animation.push(Tween.to(mCard, CardAccessor.ROTATION, duration).target(rotation));
                    break;
                case 3: //right
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(hiddenRect.getX() + powerVariation, mTable.getDeck().getY()+angleVariation).ease(TweenEquations.easeInOutExpo));
                    animation.push(Tween.to(mCard, CardAccessor.ROTATION, duration).target(rotation));
                    break;
            }
            animation.end();
            animation.setUserData(hand);
            return animation;
        }
    }

    static public class LineUpHiddenCards implements AnimationBuilder.TweenCalculator {

        private int mPosition;

        public LineUpHiddenCards(int pos) {
            mPosition = pos;
        }

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CardView mCard = builder.getCard();
            TableView mTable = builder.getTable();
            int mHandID = builder.getHandID();
            Timeline animation = Timeline.createParallel();
            animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END);
            float varianceDelay = MathUtils.random(10) / 10.0f;
            animation.delay(varianceDelay);

            Vector3 pos;

            float duration = 0.1f;
            int handPos = mPosition;
            if (handPos < 0) {
                return null;
            }
            Rectangle rect = mTable.getHands().get(mHandID).getHiddenPosition(handPos);
            pos = HandUtils.LineUpHiddenCard(mCard, mTable, mHandID, rect);
            animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(pos.x, pos.y));
            animation.push(Tween.to(mCard, CardAccessor.ROTATION, duration).target(pos.z));
            return animation;
        }
    }

    static public class SelectEndCard implements AnimationBuilder.TweenCalculator {

        private int mPosition;
        public SelectEndCard(int pos) {
            this.mPosition = pos;
        }

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CardView mCard = builder.getCard();
            TableView mTable = builder.getTable();
            int mHandID = builder.getHandID();
            Timeline animation = Timeline.createParallel();
            animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );
            if (mPosition < 0) {
                return null;
            }
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
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(x+ov, y+ov));
                    break;
                case 1: //left
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(x-(rotDiff*MathUtils.sinDeg(-sideRot))+ov , y+(rotDiff*MathUtils.sinDeg(-sideRot))+ov));
                    break;
                case 2: //top
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(x-ov,y-ov));
                    break;
                case 3: //right
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(x+(rotDiff*MathUtils.sinDeg(sideRot))-ov , y-(rotDiff*MathUtils.sinDeg(sideRot))-ov));
                    break;
            }
            animation.push(Tween.call(new TweenCallback() {
                @Override
                public void onEvent(int type, BaseTween<?> source) {
                    mCard.setSide(CardView.Side.FRONT);
                }
            }).delay(0.1f));
            return animation;
        }
    }

    static public class LineUpActiveCard implements AnimationBuilder.TweenCalculator {

        private int mIndex;
        public LineUpActiveCard(int index) {
            this.mIndex = index;
        }

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CardView mCard = builder.getCard();
            TableView mTable = builder.getTable();
            int mHandID = builder.getHandID();
            Timeline animation = Timeline.createParallel();
            animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END);
            TweenCallback mFlipCallback = new TweenCallback() {
                @Override
                public void onEvent(int type, BaseTween<?> source) {
                    mCard.setSide(CardView.Side.FRONT);
                }
            };
            Vector3 pos;

            pos = HandUtils.LineUpActiveCard(mIndex, mCard, mTable, mHandID, mTable.getHands().get(mHandID).getActivePosition(), mTable.getHands().get(mHandID).getCardOverlapPercent());

            float duration = 0.1f;
            animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(pos.x, pos.y));
            animation.push(Tween.to(mCard, CardAccessor.ROTATION, duration).target(pos.z));

            if (mTable.getHands().get(mHandID).getHand().getType() == Hand.HandType.HUMAN)
                animation.push(Tween.call(mFlipCallback).delay(0.2f));
            return animation;
        }
    }

    static public class PlayFailedCard implements AnimationBuilder.TweenCalculator {

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CardView mCard = builder.getCard();
            TableView mTable = builder.getTable();
            int mHandID = builder.getHandID();
            float pos_x = mCard.getX();
            float pos_y = mCard.getY();
            float x = mTable.getPlayView().getX();
            float y = mTable.getPlayView().getY();
            Timeline animation = Timeline.createSequence();
            animation.beginParallel();
            animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );
            TweenEquation eq = TweenEquations.easeInOutQuart;
            float duration = 0.3f;
            float div = 3.0f;
            float rotDiff = (mCard.getOriginY() - mCard.getOriginX())/1;
            float sideRot = 90.0f;
            animation.push(Tween.set(mCard, CardAccessor.POSITION_XY).target(pos_x, pos_y));
            switch (mHandID) {
                default:
                case 0: //bottom
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(x, y/div).ease(eq)).end();
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(pos_x, pos_y).ease(eq));
                    break;
                case 1: //left
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target( (x-(rotDiff*MathUtils.sinDeg(-sideRot)))/div , y+(rotDiff*MathUtils.sinDeg(-sideRot))).ease(eq)).end();
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target( (pos_x-(rotDiff*MathUtils.sinDeg(-sideRot))) , pos_y+(rotDiff*MathUtils.sinDeg(-sideRot))).ease(eq));
                    break;
                case 2: //top
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(x,y/div).ease(eq)).end();
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(pos_x,pos_y).ease(eq));
                    break;
                case 3: //right
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target((x+(rotDiff*MathUtils.sinDeg(sideRot))/div) , y-(rotDiff*MathUtils.sinDeg(sideRot))).ease(eq)).end();
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target((pos_x+(rotDiff*MathUtils.sinDeg(sideRot))) , pos_y-(rotDiff*MathUtils.sinDeg(sideRot))).ease(eq));
                    break;
            }
            return animation;
        }
    }

    static public class PlaySuccessCard implements AnimationBuilder.TweenCalculator {

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

            Timeline animation = Timeline.createSequence();
            animation.beginParallel();
            animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END);
            TweenEquation eq = TweenEquations.easeInOutQuart;
            float duration = 0.3f;
            animation.push(Tween.set(mCard, CardAccessor.POSITION_XY).target(pos_x, pos_y));
            switch (mHandID) {
                default:
                case 0: //bottom
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(x, y).ease(eq));
                    break;
                case 1: //left
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(x, y).ease(eq));
                    //animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target( (x-(rotDiff*MathUtils.sinDeg(-sideRot))) , y+(rotDiff*MathUtils.sinDeg(-sideRot))).ease(eq)).end();
                    animation.push(Tween.to(mCard, CardAccessor.ROTATION, duration).target(0.0f));
                    break;
                case 2: //top
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(x, y).ease(eq));
                    animation.push(Tween.to(mCard, CardAccessor.ROTATION, duration).target(0.0f));
                    break;
                case 3: //right
                    //animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target((x+(rotDiff*MathUtils.sinDeg(sideRot))) , y-(rotDiff*MathUtils.sinDeg(sideRot))).ease(eq));
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(x, y).ease(eq));
                    animation.push(Tween.to(mCard, CardAccessor.ROTATION, duration).target(0.0f));
                    break;
            }
            animation.push(Tween.call(new TweenCallback() {
                @Override
                public void onEvent(int type, BaseTween<?> source) {
                    mCard.setSide(CardView.Side.FRONT);
                }
            }).delay(duration / 3.0f)).end();
            return animation;
        }
    }

    static public class SelectPendingCard implements AnimationBuilder.TweenCalculator {

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CardView mCard = builder.getCard();
            TableView mTable = builder.getTable();
            int mHandID = builder.getHandID();
            float x = mCard.getX();
            float y = mCard.getY();
            Timeline animation = Timeline.createSequence(); //first we lift the cards up a tad
            float raise = CardUtils.getCardTextureHeight() * 0.15f;
            float duration = 0.5f;
            animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END);
            TweenEquation eq = TweenEquations.easeInOutQuart;
            float rotDiff = (mCard.getOriginY() - mCard.getOriginX()) / 1;
            float sideRot = 90.0f;
            animation.push(Tween.set(mCard, CardAccessor.POSITION_XY).target(x, y));

            switch (mHandID) {
                default:
                case 0: //bottom
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(x, y + raise).ease(eq)); //move it up!
                    break;
                case 1: //left
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target((x - (rotDiff * MathUtils.sinDeg(-sideRot))) + raise, y + (rotDiff * MathUtils.sinDeg(-sideRot))).ease(eq));
                    break;
                case 2: //top
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target(x, y - raise).ease(eq));
                    break;
                case 3: //right
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).target((x + (rotDiff * MathUtils.sinDeg(sideRot)) - raise), y - (rotDiff * MathUtils.sinDeg(sideRot))).ease(eq));
                    break;
            }
            //should we bounce!?
            AnimationBuilder nextBuilder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD).fromBuilder(builder);
            nextBuilder.setTweenCalculator(new BounceCard());
            builder.setNextAnimation(nextBuilder.build());
            return animation;
        }
    }

    static public class BounceCard implements AnimationBuilder.TweenCalculator {

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CardView mCard = builder.getCard();
            TableView mTable = builder.getTable();
            int mHandID = builder.getHandID();
            Timeline animation = Timeline.createSequence(); //first we lift the cards up a tad
            float fall = CardUtils.getCardTextureHeight() * 0.10f;
            float duration = 0.5f;
            TweenEquation eq = TweenEquations.easeInCubic;
            animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END);
            //animation.push(Tween.set(mCard, CardAccessor.POSITION_XY).target(mCard.getX(), mCard.getY()));

            switch (mHandID) {
                default:
                case 0: //bottom
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).targetRelative(0, -fall).ease(eq));
                    break;
                case 1: //left
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).targetRelative(-fall, 0).ease(eq));
                    break;
                case 2: //top
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).targetRelative(0, fall).ease(eq));
                    break;
                case 3: //right
                    animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, duration).targetRelative(fall, 0).ease(eq));
                    break;
            }
            animation.repeatYoyo(Tween.INFINITY, 0);
            return animation;
        }
    }

    static public class BurnCard implements AnimationBuilder.TweenCalculator {

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            final CardView mCard = builder.getCard();
            if (builder.getCamera() == null)
                throw new RuntimeException("This animation needs the camera!");
            TableView mTable = builder.getTable();
            //todo uniform number from 0-359
            float angle = (float)(22.5 * (int)((Math.random() * 15.0f)));
            float h = builder.getCamera().viewportHeight;
            float w = builder.getCamera().viewportWidth;
            float length = (float)Math.hypot(w, h);
            float x = (float) (length * Math.cos(Math.toRadians(angle)));
            float y = (float) (length * Math.sin(Math.toRadians(angle)));
            Timeline animation = Timeline.createSequence();
            animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );

            animation.push(Tween.to(mCard, CardAccessor.POSITION_XY, 1.5f).target(x, y).ease(TweenEquations.easeInOutQuad));
            return animation;
        }
    }

    @Override
    public AnimationBuilder toBuilder() {
        return super.toBuilder().setCard(mCard).setTable(mTable).setHandID(mHandID);
    }

}
