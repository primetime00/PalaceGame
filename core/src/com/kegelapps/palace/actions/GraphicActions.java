package com.kegelapps.palace.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.kegelapps.palace.Logic;
import com.kegelapps.palace.graphics.CardView;
import com.kegelapps.palace.graphics.DeckView;
import sun.rmi.runtime.Log;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
import static com.badlogic.gdx.math.Interpolation.*;


/**
 * Created by keg45397 on 12/15/2015.
 */
public class GraphicActions {

    interface GraphicTrigger {
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
}
