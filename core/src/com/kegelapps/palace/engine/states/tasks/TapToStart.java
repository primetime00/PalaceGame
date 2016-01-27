package com.kegelapps.palace.engine.states.tasks;

import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.StateListener;

/**
 * Created by keg45397 on 1/15/2016.
 */
public class TapToStart extends State {

    private Table mTable;
    private boolean mTapped;

    public TapToStart(State parent, Table table, StateListener listener) {
        super(parent);
        mTable = table;
        mStateListener = listener;
        mTapped = false;
    }

    @Override
    protected void FirstRun() {
        mTapped = false;
    }

    @Override
    protected boolean Run() {
        if (mTapped) {
            if (mStateListener != null)
                mStateListener.onContinueState();
            return true;
        }

        for (int i=0; i<mTable.getHands().size(); ++i) {
            Hand mHand = mTable.getHands().get(i);
            if (mHand.getType() == Hand.HandType.HUMAN) {
                for (Card c : mHand.GetDiscardCards()) { //make a runnable?
                    Card activeCard = mHand.getEndCards().get(mHand.getEndCards().indexOf(c));
                    mHand.RemoveEndCard(activeCard);
                    System.out.print("HUMAN DESELECTS " + c + "\n");
                    if (mStateListener != null)
                        mStateListener.onBackState();
                    mHand.GetDiscardCards().remove(activeCard);
                    return true;
                }
                mHand.GetDiscardCards().clear();
            }
        }
        return false;
    }

    @Override
    public void UserSignal() {
        mTapped = true;
    }

    @Override
    public Names getStateName() {
        return Names.TAP_DECK_START;
    }
}
