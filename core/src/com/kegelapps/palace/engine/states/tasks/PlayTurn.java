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
 * Created by keg45397 on 2/2/2016.
 */
public class PlayTurn extends State {

    protected Table mTable;
    protected Hand mHand;
    protected TurnState mTurnState;

    protected enum TurnState {
        PLAY_CARD,
        BURN,
        SELECT_CARDS,
        DONE
    }

    public PlayTurn(State parent, Table table) {
        super(parent);
        mTable = table;
        mTurnState = TurnState.PLAY_CARD;
    }

    @Override
    public void setID(int id) {
        super.setID(id);
        mHand = null;
        for (Hand h: mTable.getHands()) {
            if (h.getID() == id) {
                mChildrenStates.addState(Names.BURN_CARDS, this, id).setStateListener(new StateListener() {
                    @Override
                    public void onDoneState() {
                        mTurnState = TurnState.PLAY_CARD;
                    }
                });

                mChildrenStates.addState(Names.SELECT_CARDS_FROM_DECK, this, id).setStateListener(new StateListener() {
                    @Override
                    public void onDoneState() {
                        mTurnState = TurnState.DONE;
                    }
                });
                mHand = h;
                break;
            }
        }
    }

    @Override
    protected void OnFirstRun() {
        mTurnState = TurnState.PLAY_CARD;
        Director.instance().getEventSystem().Fire(EventSystem.EventType.CHANGE_TURN, mHand.getID());
    }

    protected boolean DoPlayCard() {
        return true;
    }

    protected boolean PlayCard(Card c) {
        return true;
    }

    @Override
    protected boolean OnRun() {
        if (mHand == null)
            throw new RuntimeException("PlayTurn need a proper hand!");

        switch (mTurnState) {
            case PLAY_CARD:
                if (DoPlayCard())
                    mTurnState = TurnState.SELECT_CARDS;
                return false;
            case BURN:
                mChildrenStates.getState(Names.BURN_CARDS, getID()).Execute();
                return false;
            case SELECT_CARDS:
                mChildrenStates.getState(Names.SELECT_CARDS_FROM_DECK, getID()).Execute();
                return false;
            case DONE:
                return true;
            default:
                throw new RuntimeException("PlayTurn State is in an unknown state?");
        }
    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();
        StateProtos.PlayTurnState.Builder builder = StateProtos.PlayTurnState.newBuilder();
        builder.setTurnState(mTurnState.ordinal());
        s = s.toBuilder().setExtension(StateProtos.PlayTurnState.state, builder.build()).build();
        return s;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
        StateProtos.PlayTurnState playState = ((StateProtos.State) msg).getExtension(StateProtos.PlayTurnState.state);
        mTurnState = TurnState.values()[playState.getTurnState()];

    }



}
