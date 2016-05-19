package com.kegelapps.palace.engine.states;

import com.google.protobuf.Message;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.protos.StateProtos;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryan on 1/13/2016.
 */
public class SelectEndCards extends State {

    private StateListener mPlaceCardListener, mTapDeckListener;

    private int mState = 0;
    private Table mTable;
    private List<Hand> mDoneHands;


    public SelectEndCards(State parent, Table table) {
        super(parent);
        mTable = table;
        createStates();
        mState = 0;
        mDoneHands = new ArrayList<>();
    }

    private void createStates() {

        Table table = mTable;

        mPlaceCardListener = new StateListener() {
            @Override
            public void onDoneState(Object result) {
                Hand hand = (Hand) result;
                if (!mDoneHands.contains(hand))
                    mDoneHands.add(hand);
                if (mDoneHands.size() == mTable.getHands().size())
                    mState = 1;
            }
        };

        mTapDeckListener = new StateListener() {
            @Override
            public void onContinueState() {
                mState = 3;
            }

            @Override
            public void onBackState() {
                mState = 0;
            }
        };

        for (Hand h : table.getHands()) {
            State s = mChildrenStates.addState(Names.PLACE_END_CARD, this, h.getID());
            s.setStateListener(mPlaceCardListener);
        }

        mChildrenStates.addState(Names.TAP_DECK_START, this).setStateListener(mTapDeckListener);

    }

    @Override
    public boolean OnRun() {
        switch (mState) {
            case 0:
                for (Hand h: mTable.getHands()) {
                    Status s = mChildrenStates.getState(Names.PLACE_END_CARD, h.getID()).getStatus();
                    if (s != Status.DONE || h.getType() == Hand.HandType.HUMAN)
                        mChildrenStates.getState(Names.PLACE_END_CARD, h.getID()).Execute();
                }
                break;
            case 1:
                for (Hand h: mTable.getHands()) {
                    if (!h.HasAllEndCards())
                        break;
                    mState = 2;
                }
                break;
            case 2:
                mChildrenStates.getState(Names.TAP_DECK_START).Execute();
                break;
            default:
                if (mStateListener != null)
                    mStateListener.onContinueState();
                return true;
        }
        return false;
    }

    @Override
    public Names getStateName() {
        return Names.SELECT_END_CARDS;
    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();

        StateProtos.SelectEndCardState.Builder builder = StateProtos.SelectEndCardState.newBuilder();
        builder.setCurrentState(mState);
        s = s.toBuilder().setExtension(StateProtos.SelectEndCardState.state, builder.build()).build();
        return s;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
        StateProtos.SelectEndCardState selectEndCardState = ((StateProtos.State) msg).getExtension(StateProtos.SelectEndCardState.state);
        mState = selectEndCardState.getCurrentState();

    }

    @Override
    public void Reset() {
        mState = 0;
        mDoneHands.clear();
        super.Reset();
    }
}
