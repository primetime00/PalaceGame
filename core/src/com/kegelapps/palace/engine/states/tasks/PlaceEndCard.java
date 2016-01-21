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
    private int mCurrentPlayer, mLastPlayer;
    private int mRound;
    private StateListener mStateListener;
    private boolean mFirstPoll;


    public PlaceEndCard(State parent, Table table, StateListener done) {
        super(parent);
        mTable = table;
        mCurrentPlayer = 0;
        mLastPlayer = -1;
        mRound = 0;
        mStateListener = done;
        mFirstPoll = true;
    }

    @Override
    public boolean Run() {
        super.Run();
        boolean mStillSelecting = false;
        Hand mHand = mTable.getHands().get(mCurrentPlayer);
        if (mFirstPoll) {
            System.out.print("Player " + mHand.getID() + " Select your 3 end cards.\n");
            mFirstPoll = false;
        }
        for (int i=0; i<mTable.getHands().size(); ++i) {
            mHand = mTable.getHands().get(i);
            if (!mHand.getEndCards().contains(null)) //we have all of our cards
                continue;
            mStillSelecting = true;
            if (mHand.getType() == Hand.HandType.HUMAN) {
                for (Card c : mHand.getPlayCards()) { //make a runnable?
                    Card activeCard = mHand.getActiveCards().get(mHand.getActiveCards().indexOf(c));
                    mHand.AddEndCard(activeCard);
                    System.out.print("HUMAN SELECTS " + c + "\n");
                }
                mHand.getPlayCards().clear();
                for (Card c : mHand.getDiscardCards()) { //make a runnable?
                    Card activeCard = mHand.getEndCards().get(mHand.getEndCards().indexOf(c));
                    mHand.RemoveEndCard(activeCard);
                    System.out.print("HUMAN DESELECTS " + c + "\n");
                }
                mHand.getDiscardCards().clear();
            } else {
                Card c = mHand.getActiveCards().get(MathUtils.random(mHand.getActiveCards().size() - 1));
                mHand.AddEndCard(c);
            }
        }
        if (!mStillSelecting) {
            if (mStateListener != null)
                mStateListener.onContinueState();
            return true;
        }
        return false;
    }

    @Override
    public Names getStateName() {
        return Names.PLACE_END_CARD;
    }
}
