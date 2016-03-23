package com.kegelapps.palace.input;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.graphics.CardView;
import com.kegelapps.palace.graphics.HandView;
import com.kegelapps.palace.graphics.TableView;

/**
 * Created by Ryan on 2/19/2016.
 */
public class CardGestureListener extends ActorGestureListener {
    private HandView mHand;

    public CardGestureListener(HandView hand) {
        super(20, 0.4f, 0.6f, 0.15f);
        mHand = hand;
    }

    @Override
    public void fling(InputEvent event, float velocityX, float velocityY, int button) {
        super.fling(event, velocityX, velocityY, button);
        if (event.getTarget() instanceof CardView) {
            Card c = ((CardView)event.getTarget()).getCard();
            if (velocityY > 300.0f) {
                if (mHand.getHand().GetActiveCards().contains(c))
                    Logic.get().PlayerSelectCard(mHand.getHand(), c);
                else if (mHand.getHand().GetEndCards().contains(c))
                    Logic.get().PlayerSelectEndCard(mHand.getHand(), c);
                else if (mHand.getHand().GetHiddenCards().contains(c))
                    Logic.get().PlayerSelectHiddenCard(mHand.getHand(), c);
            }
            else if (mHand.getHand().GetEndCards().contains(c) && velocityY < -300.0f) {
                Logic.get().PlayerUnselectCard(mHand.getHand(), c);
            }
        }
    }

    @Override
    public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
        super.pan(event, x, y, deltaX, deltaY);
        if (Math.abs(deltaX) >6.0f) {
            mHand.PanCamera(deltaX);
        }
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        super.touchUp(event, x, y, pointer, button);
        if ( !(event.getTarget() instanceof CardView) )
            return;
        CardView cv = (CardView) event.getTarget();
        if (!mHand.getHand().GetPlayCards().GetAllCards().contains(cv.getCard())) {
            Logic.get().PlayerUnSelectAllCards(mHand.getHand());
        }
    }

    @Override
    public boolean longPress(Actor actor, float x, float y) {
        boolean res = super.longPress(actor, x, y);
        Actor cardView = null;
        if (mHand.getActivePosition().contains(x, y)) {
            cardView = mHand.hit(x, y, true);
            if (cardView == null || !(cardView instanceof CardView))
                return false;
            Card c = ((CardView)cardView).getCard();
            if (mHand.getHand().GetActiveCards().contains(c)) {
                Logic.get().PlayerSelectAllCards(mHand.getHand(), c);
            }
        }
        return res;
    }

}
