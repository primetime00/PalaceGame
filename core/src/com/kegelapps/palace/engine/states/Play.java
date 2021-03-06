package com.kegelapps.palace.engine.states;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.StateProtos;

/**
 * Created by Ryan on 1/21/2016.
 */
public class Play extends State {

    private Table mTable;

    private StateListener mSingleTurnDone;
    private boolean mGameOver = false;


    public Play(State parent, Table table) {
        super(parent);
        mTable = table;
        mGameOver = false;

        mSingleTurnDone = new StateListener() {
            @Override
            public void onDoneState() {
                boolean keepPlaying = mTable.NextPlayTurn();
                if (keepPlaying) {
                    Logic.get().SaveState();
                    Director.instance().getEventSystem().Fire(EventSystem.EventType.CHANGE_TURN, mTable.getHands().get(mTable.getCurrentPlayTurn()).getID());
                }
                else {
                    if (mStateListener != null)
                        mStateListener.onDoneState();
                    mGameOver = true;
                }
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
    protected void OnFirstRun() {
        Director.instance().getEventSystem().Fire(EventSystem.EventType.CHANGE_TURN, mTable.getHands().get(mTable.getCurrentPlayTurn()).getID());
    }

    @Override
    protected boolean OnRun() {
        int mCurrentPlayer = mTable.getCurrentPlayTurn();
        if (mTable.getHands().get(mCurrentPlayer).getType() == Hand.HandType.HUMAN)
            mChildrenStates.getState(Names.PLAY_HUMAN_TURN, mCurrentPlayer).Execute();
        else
            mChildrenStates.getState(Names.PLAY_CPU_TURN, mCurrentPlayer).Execute();

        return mGameOver;
    }

    @Override
    public Names getStateName() {
        return Names.PLAY;
    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();

        StateProtos.PlayState.Builder builder = StateProtos.PlayState.newBuilder();
        s = s.toBuilder().setExtension(StateProtos.PlayState.state, builder.build()).build();
        return s;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
        StateProtos.PlayState selectEndCardState = ((StateProtos.State) msg).getExtension(StateProtos.PlayState.state);
        //mCurrentPlayer = selectEndCardState.getCurrentPlayTurn();
    }

    @Override
    public void Reset() {
        mGameOver = false;
        super.Reset();
    }
}
