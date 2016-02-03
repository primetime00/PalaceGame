package com.kegelapps.palace.engine.states;

import com.google.protobuf.Message;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.protos.StateProtos;

/**
 * Created by Ryan on 1/13/2016.
 */
public class SelectEndCards extends State {

    private StateListener mPlaceCardListener, mTapDeckListener;

    private int mState = 0;

    public SelectEndCards(State parent, Table table) {
        super(parent);
        createStates(table);
        mState = 0;
    }

    private void createStates(Table table) {

        mChildrenStates.addState(Names.PLACE_END_CARD, this).setStateListener(mPlaceCardListener);
        mChildrenStates.addState(Names.TAP_DECK_START, this).setStateListener(mTapDeckListener);

        mPlaceCardListener = new StateListener() {
            @Override
            public void onContinueState() {
                mState = 1;
            }
        };

        mTapDeckListener = new StateListener() {
            @Override
            public void onContinueState() {
                mState = 2;
            }

            @Override
            public void onBackState() {
                mState = 0;
            }
        };

        mChildrenStates.addState(Names.PLACE_END_CARD, this).setStateListener(mPlaceCardListener);
        mChildrenStates.addState(Names.TAP_DECK_START, this).setStateListener(mTapDeckListener);
    }

    @Override
    public boolean OnRun() {
        switch (mState) {
            case 0:
                mChildrenStates.getState(Names.PLACE_END_CARD).Execute();
                break;
            case 1:
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
}
