package com.kegelapps.palace.engine.states.dealtasks;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Deck;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.CardsProtos;
import com.kegelapps.palace.protos.StateProtos;

/**
 * Created by keg45397 on 2/3/2016.
 */
public class DrawPlayCard extends State {

    private Table mTable;
    private DrawState mState;

    private enum DrawState {
        DRAW,
        BURN
    }

    public DrawPlayCard(State parent, Table table) {
        super(parent);
        this.mTable = table;
        mState = DrawState.DRAW;
    }

    @Override
    public Names getStateName() {
        return Names.DRAW_PLAY_CARD;
    }

    @Override
    protected boolean OnRun() {
        switch (mState) {
            case DRAW:
                mTable.DrawCard();
                if (mTable.GetTopPlayCard().getRank() == Card.Rank.TEN) {
                    mState = DrawState.BURN;
                    return false;
                }
                if (mTable.GetTopPlayCard().getRank() != Card.Rank.TWO)
                    return true;
                break;
            case BURN:
                mTable.Burn();
                mState = DrawState.DRAW;
                break;
        }
        return false;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
        StateProtos.DrawPlayCardState drawState = ((StateProtos.State) msg).getExtension(StateProtos.DrawPlayCardState.state);
        if (drawState.hasDrawState())
            mState = DrawState.values()[drawState.getDrawState()];

    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();
        StateProtos.DrawPlayCardState.Builder builder = StateProtos.DrawPlayCardState.newBuilder();
        builder.setDrawState(mState.ordinal());
        s = s.toBuilder().setExtension(StateProtos.DrawPlayCardState.state, builder.build()).build();
        return s;

    }

    @Override
    public void Reset() {
        mState = DrawState.DRAW;
        super.Reset();
    }
}
