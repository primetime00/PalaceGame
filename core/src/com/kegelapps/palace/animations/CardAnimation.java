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
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by Ryan on 12/21/2015.
 */
public class CardAnimation implements TweenCallback {

    private boolean mPauseLogic = false;
    private String mDescription;
    private CardView mCard;
    private int mHandID;
    private AnimationType mType;

    public enum AnimationType {
        SELECT_CARDS,
        BOUNCE_CARDS,
        UNKNOWN
    }

    public CardAnimation(boolean pauseLogic) {
        init(pauseLogic, "");
    }

    public CardAnimation(boolean pauseLogic, String desc) {
        init(pauseLogic, desc);
    }

    private void init(boolean pauseLogic, String desc) {
        mCard = null;
        mHandID = -1;
        mType = AnimationType.UNKNOWN;
        mPauseLogic = pauseLogic;
        if (Logic.get().isFastDeal())
            mPauseLogic = false;
        mDescription = desc;
    }


    public CardAnimation() {
        mPauseLogic = false;
    }

    public AnimationType getType() {
        return mType;
    }

    public BaseTween<Timeline> DrawToActive(DeckView deck, InPlayView play, final CardView card) {
        card.setSide(CardView.Side.BACK);
        Vector2 vec = play.GetNextCardPosition();
        Timeline animation = Timeline.createSequence();
        animation.setCallback(this);
        animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );

        animation.push(Tween.set(card, CardAccessor.POSITION_XY).target(deck.getX(), deck.getY()));
        animation.beginParallel();
        animation.push(Tween.to(card, CardAccessor.POSITION_XY, 1.0f).target(vec.x, vec.y).ease(TweenEquations.easeInOutExpo));
        animation.push(Tween.call(new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                card.setSide(CardView.Side.FRONT);
            }
        }).delay(0.8f));
        animation.end();
        animation.start(Director.instance().getTweenManager());
        return animation;
    }

    public BaseTween<Timeline> DealToHand(DeckView deck, HandView hand, CardView card, float duration) {
        Timeline animation = Timeline.createSequence();
        animation.setCallback(this);
        animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );

        animation.push(Tween.set(card, CardAccessor.POSITION_XY).target(deck.getX(), deck.getY()));

        Rectangle r = hand.getActivePosition();
        Rectangle pg = hand.getHiddenPosition(0);
        float cardSize = card.getMaxCardSize();
        int roundSize = MathUtils.round(cardSize) / 10;
        int angleVariation = (MathUtils.random(roundSize) - (roundSize/2))*10;
        float powerVariation = MathUtils.random(r.getHeight()) - r.getHeight();
        powerVariation = 0;
        float rotation = (MathUtils.random(36) - 18)*10;

        animation.beginParallel();
        switch (hand.getHand().getID()) {
            default:
            case 0: //bottom
                animation.push(Tween.to(card, CardAccessor.POSITION_XY, duration).target(deck.getX()+angleVariation, pg.getY() + powerVariation).ease(TweenEquations.easeInOutExpo));
                animation.push(Tween.to(card, CardAccessor.ROTATION, duration).target(rotation));
                break;
            case 1: //left
                animation.push(Tween.to(card, CardAccessor.POSITION_XY, duration).target(pg.getX() + powerVariation, deck.getY()+angleVariation).ease(TweenEquations.easeInOutExpo));
                animation.push(Tween.to(card, CardAccessor.ROTATION, duration).target(rotation));
                break;
            case 2: //top
                animation.push(Tween.to(card, CardAccessor.POSITION_XY, duration).target(deck.getX()+angleVariation, pg.getY() + powerVariation).ease(TweenEquations.easeInOutExpo));
                animation.push(Tween.to(card, CardAccessor.ROTATION, duration).target(rotation));
                break;
            case 3: //right
                animation.push(Tween.to(card, CardAccessor.POSITION_XY, duration).target(pg.getX() + powerVariation, deck.getY()+angleVariation).ease(TweenEquations.easeInOutExpo));
                animation.push(Tween.to(card, CardAccessor.ROTATION, duration).target(rotation));
                break;
        }
        animation.end();
        animation.setUserData(hand);
        animation.start(Director.instance().getTweenManager());
        return animation;
    }

    public BaseTween<Timeline> LineUpHiddenCards(Rectangle rect, int id, CardView cardView) {
        Timeline animation = Timeline.createParallel();
        animation.setCallback(this);
        animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );
        float varianceDelay = MathUtils.random(10)/10.0f;
        animation.delay(varianceDelay);

        Vector3 pos;

        float duration = 0.1f;
        switch (id) {
            default:
            case 0: //bottom
                pos = HandUtils.LineUpHiddenCard(cardView, HandUtils.HandSide.SIDE_BOTTOM, rect);
                break;
            case 1: //left
                pos = HandUtils.LineUpHiddenCard(cardView, HandUtils.HandSide.SIDE_LEFT, rect);
                break;
            case 2: //top
                pos = HandUtils.LineUpHiddenCard(cardView, HandUtils.HandSide.SIDE_TOP, rect);
                break;
            case 3: //right
                pos = HandUtils.LineUpHiddenCard(cardView, HandUtils.HandSide.SIDE_RIGHT, rect);
                break;
        }
        animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(pos.x, pos.y));
        animation.push(Tween.to(cardView, CardAccessor.ROTATION, duration).target(pos.z));

        animation.start(Director.instance().getTweenManager());
        return animation;
    }

    public BaseTween<Timeline> MoveCard(float x, float y, int id, CardView cardView) {
        Timeline animation = Timeline.createParallel();
        animation.setCallback(this);
        animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );

        float duration = 0.1f;
        float rotDiff = (cardView.getOriginY() - cardView.getOriginX())/1;
        float sideRot = 90.0f;
        switch (id) {
            default:
            case 0: //bottom
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(x, y));
                break;
            case 1: //left
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(x-(rotDiff*MathUtils.sinDeg(-sideRot)) , y+(rotDiff*MathUtils.sinDeg(-sideRot))));
                break;
            case 2: //top
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(x,y));
                break;
            case 3: //right
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(x+(rotDiff*MathUtils.sinDeg(sideRot)) , y-(rotDiff*MathUtils.sinDeg(sideRot))));
                break;
        }
        animation.start(Director.instance().getTweenManager());
        return animation;
    }

    public BaseTween<Timeline> SelectEndCard(float x, float y, int id, final CardView cardView) {
        Timeline animation = Timeline.createParallel();
        animation.setCallback(this);
        animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );

        float duration = 0.2f;
        float rotDiff = (cardView.getOriginY() - cardView.getOriginX())/1;
        float sideRot = 90.0f;
        switch (id) {
            default:
            case 0: //bottom
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(x, y));
                break;
            case 1: //left
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(x-(rotDiff*MathUtils.sinDeg(-sideRot)) , y+(rotDiff*MathUtils.sinDeg(-sideRot))));
                break;
            case 2: //top
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(x,y));
                break;
            case 3: //right
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(x+(rotDiff*MathUtils.sinDeg(sideRot)) , y-(rotDiff*MathUtils.sinDeg(sideRot))));
                break;
        }
        animation.push(Tween.call(new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                cardView.setSide(CardView.Side.FRONT);
            }
        }).delay(0.1f));
        animation.start(Director.instance().getTweenManager());
        return animation;
    }

    @Override
    public void onEvent(int type, BaseTween<?> source) {
        if (type == TweenCallback.BEGIN) {
            if (mPauseLogic) {
                Logic.get().Pause(true);
                System.out.print("Pause\n");
            }
        }
        if (type == TweenCallback.END) {
            if (mPauseLogic) {
                Logic.get().Pause(false);
                System.out.print("UnPause\n");
            }
            switch (mType) {
                case SELECT_CARDS:
                    new CardAnimation(false).BounceCard(mHandID, mCard);
                    break;
                default:
                    break;
            }
        }
    }

    //public BaseTween<Timeline>  LineUpActiveCard(Rectangle rect, int id, final CardView cardView, int size, int index) {
    public BaseTween<Timeline>  LineUpActiveCard (HandView handView, final CardView cardView, int index) {
        Timeline animation = Timeline.createParallel();
        animation.setCallback(this);
        animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );
        TweenCallback mFlipCallback = new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                cardView.setSide(CardView.Side.FRONT);
            }
        };

        Vector3 pos;
        switch (handView.getHand().getID()) {
            default:
            case 0: pos = HandUtils.LineUpActiveCard(index, cardView, HandUtils.HandSide.SIDE_BOTTOM, handView.getActivePosition(), handView.getCardOverlapPercent());
                break;
            case 1: pos = HandUtils.LineUpActiveCard(index, cardView, HandUtils.HandSide.SIDE_LEFT, handView.getActivePosition(), handView.getCardOverlapPercent());
                break;
            case 2: pos = HandUtils.LineUpActiveCard(index, cardView, HandUtils.HandSide.SIDE_TOP, handView.getActivePosition(), handView.getCardOverlapPercent());
                break;
            case 3: pos = HandUtils.LineUpActiveCard(index, cardView, HandUtils.HandSide.SIDE_RIGHT, handView.getActivePosition(), handView.getCardOverlapPercent());
                break;
        }
        float duration = 0.1f;
        animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(pos.x, pos.y));
        animation.push(Tween.to(cardView, CardAccessor.ROTATION, duration).target(pos.z));

        if (handView.getHand().getType() == Hand.HandType.HUMAN)
            animation.push(Tween.call(mFlipCallback).delay(0.2f));

        animation.start(Director.instance().getTweenManager());
        return animation;
    }

    public BaseTween<Timeline> PlayFailedCard(InPlayView play, int id, CardView cardView) {
        float pos_x = cardView.getX();
        float pos_y = cardView.getY();
        float x = play.getX();
        float y = play.getY();
        Timeline animation = Timeline.createSequence();
        animation.beginParallel();
        animation.setCallback(this);
        animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );
        TweenEquation eq = TweenEquations.easeInOutQuart;
        float duration = 0.3f;
        float div = 3.0f;
        float rotDiff = (cardView.getOriginY() - cardView.getOriginX())/1;
        float sideRot = 90.0f;
        animation.push(Tween.set(cardView, CardAccessor.POSITION_XY).target(pos_x, pos_y));
        switch (id) {
            default:
            case 0: //bottom
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(x, y/div).ease(eq)).end();
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(pos_x, pos_y).ease(eq));
                break;
            case 1: //left
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target( (x-(rotDiff*MathUtils.sinDeg(-sideRot)))/div , y+(rotDiff*MathUtils.sinDeg(-sideRot))).ease(eq)).end();
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target( (pos_x-(rotDiff*MathUtils.sinDeg(-sideRot))) , pos_y+(rotDiff*MathUtils.sinDeg(-sideRot))).ease(eq));
                break;
            case 2: //top
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(x,y/div).ease(eq)).end();
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(pos_x,pos_y).ease(eq));
                break;
            case 3: //right
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target((x+(rotDiff*MathUtils.sinDeg(sideRot))/div) , y-(rotDiff*MathUtils.sinDeg(sideRot))).ease(eq)).end();
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target((pos_x+(rotDiff*MathUtils.sinDeg(sideRot))) , pos_y-(rotDiff*MathUtils.sinDeg(sideRot))).ease(eq));
                break;
        }
        animation.start(Director.instance().getTweenManager());
        return animation;
    }

    public BaseTween<Timeline> PlaySuccessCard(InPlayView play, int id, final CardView cardView) {
        float pos_x = cardView.getX();
        float pos_y = cardView.getY();
        Vector2 vec = play.GetNextCardPosition();
        float x = vec.x;
        float y = vec.y;

        Timeline animation = Timeline.createSequence();
        animation.beginParallel();
        animation.setCallback(this);
        animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );
        TweenEquation eq = TweenEquations.easeInOutQuart;
        float duration = 0.3f;
        float rotDiff = (cardView.getOriginY() - cardView.getOriginX())/1;
        float sideRot = 90.0f;
        animation.push(Tween.set(cardView, CardAccessor.POSITION_XY).target(pos_x, pos_y));
        switch (id) {
            default:
            case 0: //bottom
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(x, y).ease(eq));
                break;
            case 1: //left
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(x, y).ease(eq));
                //animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target( (x-(rotDiff*MathUtils.sinDeg(-sideRot))) , y+(rotDiff*MathUtils.sinDeg(-sideRot))).ease(eq)).end();
                animation.push(Tween.to(cardView, CardAccessor.ROTATION, duration).target(0.0f));
                break;
            case 2: //top
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(x,y).ease(eq));
                animation.push(Tween.to(cardView, CardAccessor.ROTATION, duration).target(0.0f));
                break;
            case 3: //right
                //animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target((x+(rotDiff*MathUtils.sinDeg(sideRot))) , y-(rotDiff*MathUtils.sinDeg(sideRot))).ease(eq));
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(x,y).ease(eq));
                animation.push(Tween.to(cardView, CardAccessor.ROTATION, duration).target(0.0f));
                break;
        }
        animation.push(Tween.call(new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                cardView.setSide(CardView.Side.FRONT);
            }
        }).delay(duration/3.0f)).end();
        animation.start(Director.instance().getTweenManager());
        return animation;
    }

    public BaseTween<Timeline> SelectPendingCard(int id, CardView cardView) {
        float x = cardView.getX();
        float y = cardView.getY();
        Timeline animation = Timeline.createSequence(); //first we lift the cards up a tad
        float raise = CardUtils.getCardTextureHeight() * 0.15f;
        float duration = 0.5f;
        mType = AnimationType.SELECT_CARDS;
        mCard = cardView;
        mHandID = id;
        animation.setCallback(this);
        animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );
        TweenEquation eq = TweenEquations.easeInOutQuart;
        float rotDiff = (cardView.getOriginY() - cardView.getOriginX())/1;
        float sideRot = 90.0f;
        animation.push(Tween.set(cardView, CardAccessor.POSITION_XY).target(x, y));

        switch (id) {
            default:
            case 0: //bottom
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(x, y+raise).ease(eq)); //move it up!
                break;
            case 1: //left
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target( (x-(rotDiff*MathUtils.sinDeg(-sideRot)))+raise , y+(rotDiff*MathUtils.sinDeg(-sideRot))).ease(eq));
                break;
            case 2: //top
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(x,y-raise).ease(eq));
                break;
            case 3: //right
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target((x+(rotDiff*MathUtils.sinDeg(sideRot))-raise) , y-(rotDiff*MathUtils.sinDeg(sideRot))).ease(eq));
                break;
        }
        animation.start(Director.instance().getTweenManager());
        return animation;
    }

    public BaseTween<Timeline> BounceCard(int id, CardView cardView) {
        float x = cardView.getX();
        float y = cardView.getY();
        Timeline animation = Timeline.createSequence(); //first we lift the cards up a tad
        float fall = CardUtils.getCardTextureHeight() * 0.10f;
        float duration = 0.5f;
        mType = AnimationType.BOUNCE_CARDS;
        mCard = cardView;
        mHandID = id;
        TweenEquation eq = TweenEquations.easeInCubic;
        animation.setCallback(this);
        animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );
        animation.push(Tween.set(cardView, CardAccessor.POSITION_XY).target(x, y));

        switch (id) {
            default:
            case 0: //bottom
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(x, y-fall).ease(eq));
                break;
            case 1: //left
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).targetRelative( 0-fall , 0).ease(eq));
                break;
            case 2: //top
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).targetRelative(0,0+fall).ease(eq));
                break;
            case 3: //right
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).targetRelative(0+fall , 0).ease(eq));
                break;
        }
        animation.repeatYoyo(Tween.INFINITY, 0);
        animation.start(Director.instance().getTweenManager());
        return animation;
    }

}
