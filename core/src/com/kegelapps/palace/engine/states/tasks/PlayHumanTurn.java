package com.kegelapps.palace.engine.states.tasks;

import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
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


    public PlayHumanTurn(State parent, Table table) {
        super(parent);
        mTable = table;
        mPlayCard = null;
    }

    @Override
    public void setID(int id) {
        super.setID(id);
        mHand = null;
        for (Hand h: mTable.getHands()) {
            if (h.getID() == id) {
                mHand = h;
                break;
            }
        }
    }

    @Override
    protected boolean OnRun() {
        boolean hasPlayed =false;
        if (mHand == null)
            return true;
        for (Card c : mHand.GetPlayCards()) { //make a runnable?
            Card activeCard = mHand.GetActiveCards().get(mHand.GetActiveCards().indexOf(c));
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
        mHand.GetPlayCards().clear();
        return hasPlayed;
    }

    @Override
    public Names getStateName() {
        return Names.PLAY_HUMAN_TURN;
    }

}
