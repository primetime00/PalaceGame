package com.kegelapps.palace.engine.states;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.tasks.DealCard;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.StateProtos;

/**
 * Created by Ryan on 12/23/2015.
 */
public class Deal extends State{
    private Table mTable;
    private int mCurrentPlayer, mLastPlayer;
    private int mRound;
    private Runnable mDoneRunnable;

    //state machine
    DealCard mDealHiddenCardState[];
    DealCard mDealActiveCardState[];


    public Deal(State parent, Table table) {
        super(parent);
        mTable = table;
        mCurrentPlayer = 0;
        mLastPlayer = -1;
        mRound = 0;

        createStates();
    }

    private void createStates() {
        StateListener mDealCardStateListener = new StateListener() {
            @Override
            public void onDoneState() {
                mCurrentPlayer++;
                if (mCurrentPlayer >= mTable.getHands().size()) {
                    mCurrentPlayer = 0;
                    mRound++;
                }
            }
        };

        mDealActiveCardState = new DealCard[mTable.getHands().size()];
        mDealHiddenCardState = new DealCard[mTable.getHands().size()];

        for (int i=0; i<mTable.getHands().size(); ++i) {
            mDealActiveCardState[i] = (DealCard) StateFactory.get().createState(Names.DEAL_CARD, this, i);
            mDealActiveCardState[i].setHidden(false);
            mDealActiveCardState[i].setStateListener(mDealCardStateListener);

            mDealHiddenCardState[i] = (DealCard) StateFactory.get().createState(Names.DEAL_CARD, this, i);
            mDealHiddenCardState[i].setHidden(true);
            mDealHiddenCardState[i].setStateListener(mDealCardStateListener);
        }
    }

    @Override
    protected void OnFirstRun() {
        System.out.print("Dealing cards...");
    }

    @Override
    protected boolean OnRun() {
        if (mRound < 3) {
            mDealHiddenCardState[mCurrentPlayer].Execute();
        }
        else if (mRound < 10) {
            if (mRound == 3 && mCurrentPlayer == 0)
                Director.instance().getEventSystem().Fire(EventSystem.EventType.DEAL_ACTIVE_CARDS, mRound, mCurrentPlayer);
            mDealActiveCardState[mCurrentPlayer].Execute();
        }
        else {
            return true;
        }
        mLastPlayer = mCurrentPlayer;
        return false;
    }

    @Override
    public Names getStateName() {
        return Names.DEAL;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();

        StateProtos.DealState.Builder builder = StateProtos.DealState.newBuilder();
        builder.setCurrentPlayer(mCurrentPlayer);
        builder.setRound(mRound);

        s = s.toBuilder().setExtension(StateProtos.DealState.state, builder.build()).build();
        return s;
    }
}
