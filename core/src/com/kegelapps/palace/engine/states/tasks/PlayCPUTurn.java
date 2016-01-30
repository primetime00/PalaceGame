package com.kegelapps.palace.engine.states.tasks;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.StateListener;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.StateProtos;

/**
 * Created by Ryan on 1/21/2016.
 */
public class PlayCPUTurn extends State {

    private Table mTable;
    private Hand mHand;

    private long mTime;


    public PlayCPUTurn(State parent, Table table) {
        super(parent);
        mTable = table;
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
    protected void OnFirstRun() {
        mTime = System.currentTimeMillis();
    }

    @Override
    public Names getStateName() {
        return Names.PLAY_CPU_TURN;
    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();

        StateProtos.PlayCPUTurnState.Builder builder = StateProtos.PlayCPUTurnState.newBuilder();
        s = s.toBuilder().setExtension(StateProtos.PlayCPUTurnState.state, builder.build()).build();
        return s;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
        StateProtos.PlayCPUTurnState selectEndCardState = ((StateProtos.State) msg).getExtension(StateProtos.PlayCPUTurnState.state);
    }

}
