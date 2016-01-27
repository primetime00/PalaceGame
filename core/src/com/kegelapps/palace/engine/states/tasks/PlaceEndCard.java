package com.kegelapps.palace.engine.states.tasks;

import com.badlogic.gdx.math.MathUtils;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.StateListener;

/**
 * Created by keg45397 on 1/15/2016.
 */
public class PlaceEndCard extends State {

    private Table mTable;


    public PlaceEndCard(State parent, Table table, StateListener listener) {
        super(parent);
        mTable = table;
        mStateListener = listener;
    }

    @Override
    protected void FirstRun() {
        Hand mHand = mTable.getHands().get(0);
        System.out.print("Player " + mHand.getID() + " Select your 3 end cards.\n");
    }

    @Override
    protected boolean Run() {
        boolean res = true;
        for (int i=0; i<mTable.getHands().size(); ++i) {
            Hand mHand = mTable.getHands().get(i);
            if (!mHand.getEndCards().contains(null)) //we have all of our cards
                continue;
            res = false;
            if (mHand.getType() == Hand.HandType.HUMAN) {
                for (Card c : mHand.GetPlayCards()) { //make a runnable?
                    Card activeCard = mHand.GetActiveCards().get(mHand.GetActiveCards().indexOf(c));
                    mHand.AddEndCard(activeCard);
                    System.out.print("HUMAN SELECTS " + c + "\n");
                }
                mHand.GetPlayCards().clear();
                for (Card c : mHand.GetDiscardCards()) { //make a runnable?
                    Card activeCard = mHand.getEndCards().get(mHand.getEndCards().indexOf(c));
                    mHand.RemoveEndCard(activeCard);
                    System.out.print("HUMAN DESELECTS " + c + "\n");
                }
                mHand.GetDiscardCards().clear();
            } else {
                Card c = mHand.GetActiveCards().get(MathUtils.random(mHand.GetActiveCards().size() - 1));
                mHand.AddEndCard(c);
            }
        }
        if (res) {
            if (mStateListener != null)
                mStateListener.onContinueState();
        }
        return res;
    }

    @Override
    public Names getStateName() {
        return Names.PLACE_END_CARD;
    }
}
