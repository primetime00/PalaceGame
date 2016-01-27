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
public class PlayCPUTurn extends State {

    private StateListener mStateListener;
    private Table mTable;
    private Hand mHand;

    private long mTime;


    public PlayCPUTurn(State parent, Table table, int id, StateListener listener) {
        super(parent);
        mStateListener = listener;
        mTable = table;
        mHand = table.getHands().get(id);
    }

    @Override
    protected boolean Run() {
        boolean hasPlayed =false;
        super.Run();
        if (System.currentTimeMillis() - mTime < 1000)
            return false;

        for (Card c : mHand.GetActiveCards()) {
            Card current = mTable.GetTopPlayCard();
            if (c.compareTo(current) > -1) {
                mTable.AddPlayCard(mHand, c);
                hasPlayed = true;
                setStatus(Status.NOT_STARTED);
                break;
            }
        }
        if (!hasPlayed) {
            System.out.print("CPU NEED TO PICK UP!");
        }
        return hasPlayed;
    }

    @Override
    protected void FirstRun() {
        mTime = System.currentTimeMillis();
    }

    @Override
    public Names getStateName() {
        return Names.PLAY_CPU_TURN;
    }

}
