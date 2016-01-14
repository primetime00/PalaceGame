package com.kegelapps.palace;

import com.badlogic.gdx.math.MathUtils;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;

/**
 * Created by keg45397 on 12/8/2015.
 */
public class SelectEndCardAction extends Action {

    private String mInputName = this.getClass().getName();
    private int mCardsToSelect;
    private Hand mHand;

    private boolean touched;
    private boolean mFirstPoll;

    public SelectEndCardAction(Hand hand, OnAction listener) {
        super();
        assert (listener == null);
        mInputName+=hand.getID();
        mFirstPoll = true;
        mCardsToSelect = 3;
        mHand = hand;
        mActionListener = listener;
        if (mHand.getType() == Hand.HandType.HUMAN)
            SetupInput();
    }

    private void SetupInput() {
        touched = false;
        Input.get().addInputLogicAdapter(mInputName, new Input.InputLogicAdapter() {
            @Override
            public void onTouched() {
                touched = true;
            }
        });
    }

    public void Poll() {
        if (isDisposed())
            return;
        if (mFirstPoll)
            System.out.print("Player " + mHand.getID() + " Select your 3 end cards.\n");
        if (mHand.getType() == Hand.HandType.HUMAN) {
            for (Card c : mHand.getPlayCards()) { //make a runnable?
                mCardsToSelect--;
                Card activeCard = mHand.getActiveCards().get(mHand.getActiveCards().indexOf(c));
                mHand.getEndCards().add(activeCard);
                mHand.getActiveCards().remove(activeCard);
                System.out.print("HUMAN SELECTS " + c + "\n");
            }
            mHand.getPlayCards().clear();
            for (Card c : mHand.getDiscardCards()) { //make a runnable?
                mCardsToSelect++;
                Card activeCard = mHand.getEndCards().get(mHand.getEndCards().indexOf(c));
                mHand.getEndCards().remove(activeCard);
                mHand.getActiveCards().add(activeCard);
                System.out.print("HUMAN DESELECTS " + c + "\n");
            }
            mHand.getDiscardCards().clear();
        }
        else {
            mCardsToSelect--;
            Card c = mHand.getActiveCards().get(MathUtils.random(mHand.getActiveCards().size()-1));
            mHand.getEndCards().add(c);
            mHand.getActiveCards().remove(c);
        }
        if (mCardsToSelect == 0) {
            mActionListener.onActionComplete();
            dispose();
        }
        mFirstPoll = false;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (mHand.getType() == Hand.HandType.HUMAN)
            Input.get().removeInputLogicAdapter(mInputName);
    }
}
