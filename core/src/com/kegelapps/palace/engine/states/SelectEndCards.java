package com.kegelapps.palace.engine.states;

import com.badlogic.gdx.math.MathUtils;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Table;

/**
 * Created by Ryan on 1/13/2016.
 */
public class SelectEndCards extends State {

    private Table mTable;
    private int mCurrentPlayer, mLastPlayer;
    private int mRound;
    private Runnable mDoneRunnable;
    private boolean mFirstPoll;


    public SelectEndCards(Table table, Runnable done) {
        super();
        mTable = table;
        mCurrentPlayer = 0;
        mLastPlayer = -1;
        mRound = 0;
        mDoneRunnable = done;
        mFirstPoll = true;
    }

    @Override
    public boolean Run() {
        boolean mStillSelecting = false;
        Hand mHand = mTable.getHands().get(mCurrentPlayer);
        if (mFirstPoll) {
            System.out.print("Player " + mHand.getID() + " Select your 3 end cards.\n");
            mFirstPoll = false;
        }
        for (int i=0; i<mTable.getHands().size(); ++i) {
            mHand = mTable.getHands().get(i);
            if (mHand.getEndCards().size() == 3)
                continue;
            mStillSelecting = true;
            if (mHand.getType() == Hand.HandType.HUMAN) {
                for (Card c : mHand.getPlayCards()) { //make a runnable?
                    Card activeCard = mHand.getActiveCards().get(mHand.getActiveCards().indexOf(c));
                    mHand.AddEndCard(activeCard);
                    System.out.print("HUMAN SELECTS " + c + "\n");
                }
                mHand.getPlayCards().clear();
            } else {
                Card c = mHand.getActiveCards().get(MathUtils.random(mHand.getActiveCards().size() - 1));
                mHand.AddEndCard(c);
            }
        }
        return !mStillSelecting;
    }
}
