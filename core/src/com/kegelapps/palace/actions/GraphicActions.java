package com.kegelapps.palace.actions;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.Hand;
import com.kegelapps.palace.Logic;
import com.kegelapps.palace.graphics.CardView;
import com.kegelapps.palace.graphics.DeckView;
import com.kegelapps.palace.graphics.HandView;
import sun.rmi.runtime.Log;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
import static com.badlogic.gdx.math.Interpolation.*;


/**
 * Created by keg45397 on 12/15/2015.
 */
public class GraphicActions {

    public interface GraphicTrigger {
        void onActionStart();
        void onActionEnd();
    }

    GraphicTrigger mTrigger;
    Action mStartAction, mEndAction;

    public GraphicActions(GraphicTrigger trigger) {
        mTrigger = trigger;
        initStartStop();
    }

    public GraphicActions(boolean paused) {
        if (paused) {
            mTrigger = new GraphicTrigger() {
                @Override
                public void onActionStart() {
                    Logic.get().Pause(true);
                }

                @Override
                public void onActionEnd() {
                    Logic.get().Pause(false);
                }
            };
        }
        initStartStop();
    }


    private void initStartStop() {
        if (mTrigger != null) {
            mStartAction = run(new Runnable() {
                @Override
                public void run() {
                        mTrigger.onActionStart();
                }
            });
            mEndAction = run(new Runnable() {
                @Override
                public void run() {
                    mTrigger.onActionEnd();
                }
            });
        }
    }


    public Action DrawToActive(DeckView deck, final CardView card) {

        Action animation = parallel(moveTo(deck.getX()+deck.getWidth()+5, deck.getY(), 0.3f, swingIn),  sequence(delay(0.15f), run(new Runnable() {
                    @Override
                    public void run() {
                        card.setSide(CardView.Side.FRONT);
                    }
                })));
        if (mTrigger != null)
            return sequence(mStartAction, animation, mEndAction);
        else
            return animation;
    }

    public Action DealToHand(DeckView deck, Hand hand, CardView card) {
        Action animation;
        float cardSize = card.getMaxCardSize();
        int roundSize = MathUtils.round(cardSize) / 10;
        int variation = (MathUtils.random(roundSize) - (roundSize/2))*10;
        float duration = 0.7f;
        float rotation = (MathUtils.random(36) - 18)*10;
        switch (hand.getID()) {
            default:
            case 0: //bottom
                animation = parallel(moveTo(deck.getX()+variation, -cardSize, duration, exp5Out),
                        rotateBy(rotation, duration));
                break;
            case 1: //left
                animation = parallel(moveTo(-cardSize, deck.getY()+variation, duration, exp5Out),
                        rotateBy(rotation, duration));
                break;
            case 2: //top
                animation = parallel(moveTo(deck.getX()+variation, Director.instance().getScreenHeight()+cardSize, duration, exp5Out),
                        rotateBy(rotation, duration));
                break;
            case 3: //right
                animation = parallel(moveTo(Director.instance().getScreenWidth()+cardSize, deck.getY()+variation, duration, exp5Out),
                        rotateBy(rotation, duration));
                break;
        }
        if (mTrigger != null)
            return sequence(mStartAction, animation, mEndAction);
        return animation;
    }

    public Action LineUpHiddenCard(Polygon pos, int handId) {
        Action animation;
        float duration = 0.1f;
        switch (handId) {
            default:
            case 0: //bottom
                animation = parallel(moveTo(pos.getX(), pos.getY(), duration),
                        rotateTo(0.0f, duration));
                break;
            case 1: //left
                animation = parallel(moveTo(pos.getX(), pos.getY(), duration),
                        rotateTo(-90.0f, duration));
                break;
            case 2: //top
                animation = parallel(moveTo(pos.getX(), pos.getY(), duration),
                        rotateTo(180.0f, duration));
                break;
            case 3: //right
                animation = parallel(moveTo(pos.getX(), pos.getY(), duration),
                        rotateTo(90.0f, duration));
                break;
        }
        float varianceDelay = MathUtils.random(4)/10.0f;
        animation = sequence(delay(varianceDelay), animation);
        if (mTrigger != null)
            return sequence(mStartAction, animation, mEndAction);
        return animation;
    }


}