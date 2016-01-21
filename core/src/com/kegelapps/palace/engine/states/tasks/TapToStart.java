package com.kegelapps.palace.engine.states.tasks;

import com.badlogic.gdx.math.MathUtils;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;

/**
 * Created by keg45397 on 1/15/2016.
 */
public class TapToStart extends State {

    private Table mTable;

    private OnStateListener mStateListener;

    public TapToStart(State parent, Table table, OnStateListener listener) {
        super(parent);
        mTable = table;
        mStateListener = listener;
    }


    @Override
    public boolean Run() {
        super.Run();

        Hand mHand;

        for (int i=0; i<mTable.getHands().size(); ++i) {
            mHand = mTable.getHands().get(i);
            if (mHand.getType() == Hand.HandType.HUMAN) {
                for (Card c : mHand.getDiscardCards()) { //make a runnable?
                    Card activeCard = mHand.getEndCards().get(mHand.getEndCards().indexOf(c));
                    mHand.RemoveEndCard(activeCard);
                    System.out.print("HUMAN DESELECTS " + c + "\n");
                    if (mStateListener != null)
                        mStateListener.onBackState();
                    mHand.getDiscardCards().remove(activeCard);
                    return true;
                }
                mHand.getDiscardCards().clear();
            }
        }
        return false;
    }

    public void Tapped() {
        if (mStateListener != null)
            mStateListener.onContinueState();
    }

    @Override
    public Names getStateName() {
        return Names.TAP_DECK_START;
    }
}
