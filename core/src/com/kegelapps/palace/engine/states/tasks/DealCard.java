package com.kegelapps.palace.engine.states.tasks;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Deck;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.StateListener;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.StateProtos;

/**
 * Created by Ryan on 12/23/2015.
 */
public class DealCard extends State {

    private Hand mHand;
    private Deck mDeck;
    private Card mCard;
    private CardState mCardState;

    private boolean mHidden;

    enum CardState {
        DEALING,
        ACCEPTING,
        DONE
    };


    public DealCard(State parent, Hand hand, Deck deck, boolean hidden, StateListener listener) {
        super(parent);
        mHand = hand;
        mDeck = deck;
        mStateListener = listener;
        mCardState = CardState.DEALING;
        mHidden = hidden;
    }

    @Override
    protected void FirstRun() {
        mCardState = CardState.DEALING;
    }

    @Override
    protected boolean Run() {
        switch (mCardState) {
            default:
            case DEALING:
                System.out.print("Dealing " + (mHidden ? "hidden" : "shown") + " card to player " + mHand.getID() + "\n");
                mCard = mDeck.Draw();
                Director.instance().getEventSystem().Fire(EventSystem.EventType.DEAL_CARD, mCard, mHand);
                mCardState = CardState.ACCEPTING;
                return false;
            case ACCEPTING:
                if (mCard == null)
                    throw new NullPointerException("Card is null!");
                if (mHidden)
                    mHand.AddHiddenCard(mCard);
                else
                    mHand.AddActiveCard(mCard);
                mCardState = CardState.DONE;
                return false;
            case DONE:
                return true;
        }
    }

    @Override
    public Names getStateName() {
        return Names.DEAL_CARD;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();

        StateProtos.DealCardState.Builder builder = StateProtos.DealCardState.newBuilder();
        builder.setCurrentState(mCardState.ordinal());
        builder.setHidden(mHidden);

        s.toBuilder().setExtension(StateProtos.DealCardState.state, builder.build()).build();
        return s;
    }
}
