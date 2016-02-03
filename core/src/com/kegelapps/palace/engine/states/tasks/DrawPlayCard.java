package com.kegelapps.palace.engine.states.tasks;

import com.google.protobuf.Message;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Deck;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;

/**
 * Created by keg45397 on 2/3/2016.
 */
public class DrawPlayCard extends State {

    private Deck mDeck;
    private Table mTable;

    public DrawPlayCard(State parent, Table table) {
        super(parent);
        this.mTable = table;
        mDeck = table.getDeck();
    }

    @Override
    public Names getStateName() {
        return Names.DRAW_PLAY_CARD;
    }

    @Override
    protected boolean OnRun() {
        mTable.DrawCard();
        Card.Rank rank = mTable.GetTopPlayCard().getRank();
        boolean dealAgain = (rank == Card.Rank.TWO || rank == Card.Rank.TEN) && mTable.getDeck().GetCards().size() > 0;
        if (rank == Card.Rank.TEN) {//we burn
            mTable.Burn();
        }
        return !dealAgain;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
    }

    @Override
    public Message WriteBuffer() {
        return super.WriteBuffer();
    }
}
