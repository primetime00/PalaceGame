package com.kegelapps.palace.engine.states.dealtasks;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.StateProtos;

/**
 * Created by keg45397 on 1/15/2016.
 */
public class TapToStart extends State {

    private Table mTable;
    private boolean mTapped;

    public TapToStart(State parent, Table table) {
        super(parent);
        mTable = table;
        mTapped = false;
    }

    @Override
    protected void OnFirstRun() {
        mTapped = false;
    }

    @Override
    protected boolean OnRun() {
        if (mTapped || mTable.isEveryPlayerCPU()) {
            if (mStateListener != null)
                mStateListener.onContinueState();
            return true;
        }

        for (int i=0; i<mTable.getHands().size(); ++i) {
            Hand mHand = mTable.getHands().get(i);
            if (mHand.getType() == Hand.HandType.HUMAN) {
                for (Card c : mHand.GetDiscardCards()) { //make a runnable?
                    Card activeCard = mHand.GetEndCards().get(mHand.GetEndCards().indexOf(c));
                    mHand.RemoveEndCard(activeCard);
                    System.out.print("HUMAN DESELECTS " + c + "\n");
                    if (mStateListener != null)
                        mStateListener.onBackState();
                    mHand.GetDiscardCards().remove(activeCard);
                    return true;
                }
                mHand.GetDiscardCards().clear();
            }
        }
        return false;
    }

    @Override
    public void UserSignal(Logic.RequestType type) {
        if (type == Logic.RequestType.SELECT_DECK)
            mTapped = true;
    }

    @Override
    public Names getStateName() {
        return Names.TAP_DECK_START;
    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();

        StateProtos.TapToStartState.Builder builder = StateProtos.TapToStartState.newBuilder();
        builder.setTapped(mTapped);
        s = s.toBuilder().setExtension(StateProtos.TapToStartState.state, builder.build()).build();
        return s;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
        StateProtos.TapToStartState tappedState = ((StateProtos.State) msg).getExtension(StateProtos.TapToStartState.state);
        mTapped = tappedState.getTapped();

    }
}
