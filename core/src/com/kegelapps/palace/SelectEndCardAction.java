package com.kegelapps.palace;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

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

    void Poll() {
        if (isDisposed())
            return;
        if (mFirstPoll)
            System.out.print("Player " + mHand.getID() + " Select your 3 end cards.\n");
        if (mHand.getType() == Hand.HandType.HUMAN) {
            if (touched) { //make a runnable?
                touched = false;
                mCardsToSelect--;
                Card c = mHand.getActiveCards().get(MathUtils.random(mHand.getActiveCards().size()-1));
                mHand.getEndCards().add(c);
                mHand.getActiveCards().remove(c);
                System.out.print("HUMAN SELECTS " + c + "\n");
            }
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
