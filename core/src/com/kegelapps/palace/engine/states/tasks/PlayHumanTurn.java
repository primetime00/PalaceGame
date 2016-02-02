package com.kegelapps.palace.engine.states.tasks;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.StateListener;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.CardsProtos;
import com.kegelapps.palace.protos.StateProtos;

import java.util.Iterator;

/**
 * Created by Ryan on 1/21/2016.
 */
public class PlayHumanTurn extends State {

    private Table mTable;
    private Hand mHand;
    private Card mPlayCard;
    private boolean mTapped;


    public PlayHumanTurn(State parent, Table table) {
        super(parent);
        mTable = table;
        mPlayCard = null;
        mTapped = false;
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
        mPlayCard = null;
        mTapped = false;
    }

    @Override
    protected boolean OnRun() {
        boolean hasPlayed =false;
        if (mHand == null)
            return true;
        if (mTapped) {
            mHand.GetPlayCards().Clear();
            return true;
        }
        for (Iterator<Card> it = mHand.GetPlayCards().GetPendingCards().iterator(); it.hasNext(); ) {
            Card c = it.next();
            it.remove();
            Card activeCard = mHand.GetActiveCards().get(mHand.GetActiveCards().indexOf(c));
            if (mPlayCard != null) {
                if (!SameAsLastCard(activeCard)) //we are playing a card that is different
                    break;
            }

            if (mTable.AddPlayCard(mHand, activeCard)) {
                mPlayCard = activeCard;
                if (!mHand.ContainsRank(activeCard.getRank())) { //this turn is over!
                    hasPlayed = true;
                    break;
                }
                else { //we can either play another card or tap the deck to end turn
                    Director.instance().getEventSystem().Fire(EventSystem.EventType.HIGHLIGHT_DECK, true);
                }
            }
        }
        return hasPlayed;
    }

    private boolean SameAsLastCard(Card card) {
        if (mPlayCard.getRank() != card.getRank()) { //trying to add more than one card with different ranks
            Director.instance().getEventSystem().Fire(EventSystem.EventType.CARD_PLAY_FAILED, card, mHand);
            return false;
        }
        return true;
    }

    @Override
    protected void OnEndRun() {
        Director.instance().getEventSystem().Fire(EventSystem.EventType.HIGHLIGHT_DECK, false);
        mPlayCard = null;
    }

    @Override
    public void UserSignal() {
        if (mPlayCard != null)
            mTapped = true;
    }

    @Override
    public Names getStateName() {
        return Names.PLAY_HUMAN_TURN;
    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();

        StateProtos.PlayHumanTurnState.Builder builder = StateProtos.PlayHumanTurnState.newBuilder();
        if (mPlayCard != null)
            builder.setPlayCard((CardsProtos.Card) mPlayCard.WriteBuffer());
        builder.setTapped(mTapped);
        s = s.toBuilder().setExtension(StateProtos.PlayHumanTurnState.state, builder.build()).build();
        return s;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
        StateProtos.PlayHumanTurnState playHumanState = ((StateProtos.State) msg).getExtension(StateProtos.PlayHumanTurnState.state);
        if (playHumanState.hasPlayCard())
            mPlayCard = Card.GetCard(Card.Suit.values()[playHumanState.getPlayCard().getSuit()], Card.Rank.values()[playHumanState.getPlayCard().getRank()]);
        if (playHumanState.hasTapped())
            mTapped = playHumanState.getTapped();
    }


}
