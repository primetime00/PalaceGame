package com.kegelapps.palace.engine.states.dealtasks;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Deck;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.CardsProtos;
import com.kegelapps.palace.protos.StateProtos;

/**
 * Created by Ryan on 12/23/2015.
 */
public class DealCard extends State {

    private Hand mHand;
    private Deck mDeck;
    private Table mTable;
    private Card mCard;
    private CardState mCardState;

    private boolean mHidden;

    enum CardState {
        DEALING,
        ACCEPTING,
        DONE
    }


    public DealCard(State parent, Table table) {
        super(parent);
        mTable = table;
        mDeck = table.getDeck();
        mCardState = CardState.DEALING;
        mHidden = false;
    }

    public void setHidden(boolean hidden) {
        mHidden = hidden;
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
    protected void OnFirstRun() {
        mCardState = CardState.DEALING;
    }

    @Override
    protected boolean OnRun() {
        if (mHand == null)
            return true;
        switch (mCardState) {
            default:
            case DEALING:
                mCard = mDeck.Draw();
                //System.out.print(String.format("Dealing card: %s\n", mCard.toString()));
                Director.instance().getEventSystem().Fire(EventSystem.EventType.DEAL_CARD, mCard, mHand.getID()); //triggers a deal animation
                mCardState = CardState.ACCEPTING;
                return false;
            case ACCEPTING:
                if (mCard == null)
                    throw new NullPointerException("Card is null!");
                if (mHidden)
                    mHand.AddHiddenCard(mCard); //triggers lining up the hidden card
                else
                    mHand.AddActiveCard(mCard); //triggers lining up the active
                mCardState = CardState.DONE;
                return false;
            case DONE:
                return true;
        }
    }

    @Override
    public Names getStateName() {
        if (mHidden)
            return Names.DEAL_HIDDEN_CARD;
        return Names.DEAL_SHOWN_CARD;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
        StateProtos.DealCardState dealCardState = ((StateProtos.State) msg).getExtension(StateProtos.DealCardState.state);
        mHidden = dealCardState.getHidden();
        mCardState = CardState.values()[dealCardState.getCurrentState()];
        if (dealCardState.hasCard())
            mCard = Card.GetCard(dealCardState.getCard());
    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();

        StateProtos.DealCardState.Builder builder = StateProtos.DealCardState.newBuilder();
        builder.setCurrentState(mCardState.ordinal());
        builder.setHidden(mHidden);
        if (mCard != null)
            builder.setCard((CardsProtos.Card) mCard.WriteBuffer());
        s = s.toBuilder().setExtension(StateProtos.DealCardState.state, builder.build()).build();
        return s;
    }

    @Override
    public void Reset() {
        mCardState = CardState.DEALING;
        super.Reset();
    }
}
