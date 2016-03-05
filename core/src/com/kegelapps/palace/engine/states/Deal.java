package com.kegelapps.palace.engine.states;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.StateProtos;

/**
 * Created by Ryan on 12/23/2015.
 */
public class Deal extends State{
    private Table mTable;
    private int mRound;


    public Deal(State parent, Table table) {
        super(parent);
        mTable = table;
        mRound = 0;

        createStates();
    }

    private void createStates() {
        StateListener mDealCardStateListener = new StateListener() {
            @Override
            public void onDoneState() {
                if (mTable.NextDealTurn()) //we've come around full circle
                    mRound++;
            }
        };

        State s;
        for (int i=0; i<mTable.getHands().size(); ++i) {
            s = mChildrenStates.addState(Names.DEAL_HIDDEN_CARD, this, i);
            s.setStateListener(mDealCardStateListener);

            s = mChildrenStates.addState(Names.DEAL_SHOWN_CARD, this, i);
            s.setStateListener(mDealCardStateListener);

        }
    }

    @Override
    protected void OnFirstRun() {
        System.out.print("Dealing cards...");
    }

    @Override
    protected boolean OnRun() {
        if (mRound < 3) {
            mChildrenStates.getState(Names.DEAL_HIDDEN_CARD, mTable.getCurrentDealTurn()).Execute();
        }
        else if (mRound < 10) {
            if (mRound == 3 && mTable.getCurrentDealTurn() == 0)
                Director.instance().getEventSystem().Fire(EventSystem.EventType.DEAL_ACTIVE_CARDS, mRound, mTable.getCurrentDealTurn());
            mChildrenStates.getState(Names.DEAL_SHOWN_CARD, mTable.getCurrentDealTurn()).Execute();
        }
        else {
            return true;
        }
        return false;
    }

    @Override
    public Names getStateName() {
        return Names.DEAL;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
        StateProtos.DealState dealState = ((StateProtos.State) msg).getExtension(StateProtos.DealState.state);
        mRound = dealState.getRound();
    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();

        StateProtos.DealState.Builder builder = StateProtos.DealState.newBuilder();
        builder.setRound(mRound);

        s = s.toBuilder().setExtension(StateProtos.DealState.state, builder.build()).build();
        return s;
    }

    @Override
    public void Reset() {
        mRound = 0;
        super.Reset();
    }
}
