package com.kegelapps.palace.engine.states.tasks;

import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.StateListener;
import com.kegelapps.palace.events.EventSystem;

/**
 * Created by Ryan on 1/21/2016.
 */
public class PlayHumanTurn extends State {

    private StateListener mStateListener;
    private Table mTable;
    private Hand mHand;
    private Card mPlayCard;


    public PlayHumanTurn(State parent, Table table, int id, StateListener done) {
        super(parent);
        mStateListener = done;
        mTable = table;
        mHand = table.getHands().get(id);
        mPlayCard = null;
    }

    @Override
    public boolean Run() {
        boolean hasPlayed =false;
        super.Run();
        for (Card c : mHand.getPlayCards()) { //make a runnable?
            Card activeCard = mHand.getActiveCards().get(mHand.getActiveCards().indexOf(c));
            if (mPlayCard != null && mPlayCard.getRank() != activeCard.getRank()) { //trying to add more than one card with different ranks
                Director.instance().getEventSystem().Fire(EventSystem.EventType.CARD_PLAY_FAILED, activeCard, mHand);
                continue;
            }
            if (mTable.AddPlayCard(mHand, activeCard)) {
                mPlayCard = activeCard;
                if (!mHand.ContainsRank(activeCard.getRank())) { //this turn is over!
                    hasPlayed = true;
                    break;
                }
            }
        }
        mHand.getPlayCards().clear();
        if (hasPlayed && mStateListener != null)
            mStateListener.onContinueState();
        return hasPlayed;
    }

    @Override
    public Names getStateName() {
        return Names.PLAY_HUMAN_TURN;
    }

}
