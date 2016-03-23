package com.kegelapps.palace.engine.states.dealtasks;

import com.google.protobuf.Message;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.ai.EndCardSelection;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.protos.StateProtos;

import java.util.Iterator;

/**
 * Created by keg45397 on 1/15/2016.
 */
public class PlaceEndCard extends State {

    private Table mTable;
    private Hand mHand;

    private enum PlaceState {
        HUMAN_TURN,
        CPU_SELECT,
        CPU_PLAY,
        DONE,
    }

    private PlaceState mState;


    public PlaceEndCard(State parent, Table table) {
        super(parent);
        mTable = table;
        mState = PlaceState.DONE;
    }

    @Override
    protected void OnFirstRun() {
        if (mHand.getType() == Hand.HandType.HUMAN)
            mState = PlaceState.HUMAN_TURN;
        else {
            if (mHand.HasAllEndCards())
                mState = PlaceState.DONE;
            else
                mState = PlaceState.CPU_SELECT;
        }
    }

    protected boolean OnRun() {
        switch (mState) {
            case HUMAN_TURN:
                if (PlaceCardsHuman())
                    mState = PlaceState.DONE;
                break;
            case CPU_SELECT:
                mHand.GetAI().SelectEndCards();
                mState = PlaceState.CPU_PLAY;
                break;
            case CPU_PLAY:
                Card c = mHand.GetAI().PopSelectedCard();
                if (c != null)
                    mHand.AddEndCard(c);
                else
                    mState = PlaceState.DONE;
                break;
            case DONE:
                if (mStateListener != null)
                    mStateListener.onContinueState();
                return true;
        }
        return false;
    }

    private boolean PlaceCardsHuman() {
        while (mHand.GetPlayCards().GetPendingCards().size() > 0) {
            Card c = mHand.GetPlayCards().PopCard();
            Card activeCard = mHand.GetActiveCards().get(mHand.GetActiveCards().indexOf(c));
            mHand.AddEndCard(activeCard);
        }
        for (Iterator<Card> it = mHand.GetDiscardCards().iterator(); it.hasNext(); ) {
            Card c = it.next();
            it.remove();
            Card activeCard = mHand.GetEndCards().get(mHand.GetEndCards().indexOf(c));
            mHand.RemoveEndCard(activeCard);
        }
        return mHand.HasAllEndCards();
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
    public Names getStateName() {
        return Names.PLACE_END_CARD;
    }


    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();

        StateProtos.PlaceEndCardState.Builder builder = StateProtos.PlaceEndCardState.newBuilder();
        builder.setPlaceState(mState.ordinal());
        s = s.toBuilder().setExtension(StateProtos.PlaceEndCardState.state, builder.build()).build();
        return s;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
        StateProtos.PlaceEndCardState selectEndCardState = ((StateProtos.State) msg).getExtension(StateProtos.PlaceEndCardState.state);
        mState = PlaceState.values()[selectEndCardState.getPlaceState()];


    }

}
