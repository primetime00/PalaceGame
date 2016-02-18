package com.kegelapps.palace.engine.states;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.utils.HandUtils;
import com.kegelapps.palace.protos.StateProtos;

/**
 * Created by Ryan on 1/21/2016.
 */
public class Play extends State {

    private Table mTable;

    private StateListener mSingleTurnDone;


    public Play(State parent, Table table) {
        super(parent);
        mTable = table;

        mSingleTurnDone = new StateListener() {
            @Override
            public void onDoneState() {
                mTable.NextTurn();
                Director.instance().getEventSystem().Fire(EventSystem.EventType.CHANGE_TURN, mTable.getHands().get(mTable.getCurrentPlayer()).getID());
            }
        };

        for (int i=0; i<mTable.getHands().size(); ++i) {
            if (mTable.getHands().get(i).getType() == Hand.HandType.HUMAN) {
                mChildrenStates.addState(Names.PLAY_HUMAN_TURN, this, i).setStateListener(mSingleTurnDone);
            }
            else {
                mChildrenStates.addState(Names.PLAY_CPU_TURN, this, i).setStateListener(mSingleTurnDone);
            }
        }
    }

    @Override
    protected boolean OnRun() {
        int mCurrentPlayer = mTable.getCurrentPlayer();
        if (mTable.getHands().get(mCurrentPlayer).getType() == Hand.HandType.HUMAN)
            mChildrenStates.getState(Names.PLAY_HUMAN_TURN, mCurrentPlayer).Execute();
        else
            mChildrenStates.getState(Names.PLAY_CPU_TURN, mCurrentPlayer).Execute();
        return false;
    }

    @Override
    public Names getStateName() {
        return Names.PLAY;
    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();

        StateProtos.PlayState.Builder builder = StateProtos.PlayState.newBuilder();
        //builder.setCurrentPlayer(mCurrentPlayer);
        s = s.toBuilder().setExtension(StateProtos.PlayState.state, builder.build()).build();
        return s;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
        StateProtos.PlayState selectEndCardState = ((StateProtos.State) msg).getExtension(StateProtos.PlayState.state);
        //mCurrentPlayer = selectEndCardState.getCurrentPlayer();
    }
}
