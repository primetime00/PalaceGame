package com.kegelapps.palace.animations;

import aurelienribon.tweenengine.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.graphics.CardUtils;
import com.kegelapps.palace.graphics.CardView;
import com.kegelapps.palace.graphics.DeckView;
import com.kegelapps.palace.graphics.HandView;
import com.kegelapps.palace.tween.CardAccessor;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by Ryan on 12/21/2015.
 */
public class CardAnimation implements TweenCallback {

    private boolean mPauseLogic = false;
    public CardAnimation(boolean pauseLogic) {
        mPauseLogic = pauseLogic;
    }

    public CardAnimation() {
        mPauseLogic = false;
    }

    public BaseTween<Timeline> DrawToActive(DeckView deck, final CardView card) {

        Timeline animation = Timeline.createParallel();
        animation.setCallback(this);
        animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );
        animation.push(Tween.to(card, CardAccessor.POSITION_XY, 1.0f).target(deck.getX()+deck.getWidth()+5, deck.getY()).ease(TweenEquations.easeInOutExpo));
        animation.push(Tween.call(new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                card.setSide(CardView.Side.FRONT);
            }
        }).delay(0.8f));

        animation.start(Director.instance().getTweenManager());
        return animation;
    }

    public BaseTween<Timeline> DealToHand(DeckView deck, HandView hand, CardView card, float duration) {
        Timeline animation = Timeline.createParallel();
        animation.setCallback(this);
        animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );

        Rectangle r = hand.getActivePosition();
        Rectangle pg = hand.getHiddenPosition(0);
        float cardSize = card.getMaxCardSize();
        int roundSize = MathUtils.round(cardSize) / 10;
        int angleVariation = (MathUtils.random(roundSize) - (roundSize/2))*10;
        float powerVariation = MathUtils.random(r.getHeight()) - r.getHeight();
        powerVariation = 0;
        float rotation = (MathUtils.random(36) - 18)*10;
        switch (hand.getHand().getID()) {
            default:
            case 0: //bottom
                //animation.push(Tween.to(card, CardAccessor.POSITION_XY, duration).target(deck.getX()+variation, -cardSize).ease(TweenEquations.easeInOutExpo));
                animation.push(Tween.to(card, CardAccessor.POSITION_XY, duration).target(deck.getX()+angleVariation, pg.getY() + powerVariation).ease(TweenEquations.easeInOutExpo));
                animation.push(Tween.to(card, CardAccessor.ROTATION, duration).target(rotation));
                break;
            case 1: //left
                //animation.push(Tween.to(card, CardAccessor.POSITION_XY, duration).target(-cardSize, deck.getY()+angleVariation).ease(TweenEquations.easeInOutExpo));
                animation.push(Tween.to(card, CardAccessor.POSITION_XY, duration).target(pg.getX() + powerVariation, deck.getY()+angleVariation).ease(TweenEquations.easeInOutExpo));
                animation.push(Tween.to(card, CardAccessor.ROTATION, duration).target(rotation));
                break;
            case 2: //top
                animation.push(Tween.to(card, CardAccessor.POSITION_XY, duration).target(deck.getX()+angleVariation, pg.getY() + powerVariation).ease(TweenEquations.easeInOutExpo));
                //animation.push(Tween.to(card, CardAccessor.POSITION_XY, duration).target(deck.getX()+angleVariation, Director.instance().getScreenHeight()+cardSize).ease(TweenEquations.easeInOutExpo));
                animation.push(Tween.to(card, CardAccessor.ROTATION, duration).target(rotation));
                break;
            case 3: //right
                animation.push(Tween.to(card, CardAccessor.POSITION_XY, duration).target(pg.getX() + powerVariation, deck.getY()+angleVariation).ease(TweenEquations.easeInOutExpo));
                //animation.push(Tween.to(card, CardAccessor.POSITION_XY, duration).target(Director.instance().getScreenWidth()+cardSize, deck.getY()+angleVariation).ease(TweenEquations.easeInOutExpo));
                animation.push(Tween.to(card, CardAccessor.ROTATION, duration).target(rotation));
                break;
        }
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

        float duration = 0.1f;
        float rotDiff = (cardView.getOriginY() - cardView.getOriginX())/1;
        float sideRot = 90.0f;
        switch (id) {
            default:
            case 0: //bottom
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(rect.getX(), rect.getY()));
                animation.push(Tween.to(cardView, CardAccessor.ROTATION, duration).target(0.0f));
                break;
            case 1: //left
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(rect.getX()-(rotDiff*MathUtils.sinDeg(-sideRot)) , rect.getY()+(rotDiff*MathUtils.sinDeg(-sideRot))));
                animation.push(Tween.to(cardView, CardAccessor.ROTATION, duration).target(-sideRot));
                break;
            case 2: //top
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(rect.getX(), rect.getY()));
                animation.push(Tween.to(cardView, CardAccessor.ROTATION, duration).target(180.0f));
                break;
            case 3: //right
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(rect.getX()+(rotDiff*MathUtils.sinDeg(sideRot)) , rect.getY()-(rotDiff*MathUtils.sinDeg(sideRot))));
                animation.push(Tween.to(cardView, CardAccessor.ROTATION, duration).target(sideRot));
                break;
        }
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
            if (mPauseLogic)
                Logic.get().Pause(true);
        }
        if (type == TweenCallback.END) {
            if (mPauseLogic)
                Logic.get().Pause(false);
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

        Rectangle rect = handView.getActivePosition();
        int id = handView.getHand().getID();

        int cardWidth = CardUtils.getCardTextureWidth();

        //find out card1 X position:
        float width = cardWidth*index*handView.getCardOverlapPercent();
        float posX = rect.getX()+(width);
        float posY = rect.getY()+(width);

        float duration = 0.1f;
        float rotDiff = (cardView.getOriginY() - cardView.getOriginX())/1;
        float sideRot = 90.0f;
        switch (id) {
            default:
            case 0: //bottom
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(posX, rect.getY()));
                animation.push(Tween.to(cardView, CardAccessor.ROTATION, duration).target(0.0f));
                break;
            case 1: //left
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(rect.getX()-(rotDiff*MathUtils.sinDeg(-sideRot)) , posY+(rotDiff*MathUtils.sinDeg(-sideRot))));
                animation.push(Tween.to(cardView, CardAccessor.ROTATION, duration).target(-sideRot));
                break;
            case 2: //top
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(posX, rect.getY()));
                animation.push(Tween.to(cardView, CardAccessor.ROTATION, duration).target(180.0f));
                break;
            case 3: //right
                animation.push(Tween.to(cardView, CardAccessor.POSITION_XY, duration).target(rect.getX()+(rotDiff*MathUtils.sinDeg(sideRot)) , posY-(rotDiff*MathUtils.sinDeg(sideRot))));
                animation.push(Tween.to(cardView, CardAccessor.ROTATION, duration).target(sideRot));
                break;
        }
        if (handView.getHand().getType() == Hand.HandType.HUMAN)
            animation.push(Tween.call(mFlipCallback).delay(0.2f));

        animation.start(Director.instance().getTweenManager());
        return animation;
    }
}
