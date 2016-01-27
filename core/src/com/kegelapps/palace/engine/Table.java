package com.kegelapps.palace.engine;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.states.tasks.DealCard;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.events.EventSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by keg45397 on 12/7/2015.
 */
public class Table {
    private Deck mDeck; //the deck on the table

    InPlay mPlayCards;

    List<Card> mBurntCards;

    //should the table have the hands?
    List<Hand> mHands;

    //state machine
    DealCard mDealHiddenCardState;
    DealCard mDealActiveCardState;

    public interface TableListener {
        public void onDealCard(Hand hand, Card c);
    }

    public Table(Deck deck, ArrayList<Hand> hands, InPlay inPlay)
    {
        mDeck = deck;
        mPlayCards = inPlay;
        mHands = hands;
    }

    public Table(Deck deck, int numberOfPlayers, BlockingQueue<Runnable> queue) {
        assert (numberOfPlayers != 3 || numberOfPlayers != 4);
        mDeck = deck;
        mPlayCards = new InPlay();
        mBurntCards = new ArrayList<>();
        mHands = new ArrayList<>();
        for (int i=0; i<numberOfPlayers; ++i) {
            mHands.add(new Hand(i, i==0 ? Hand.HandType.HUMAN : Hand.HandType.CPU, mDeck, queue));
        }
    }

    public List<Hand> getHands() {
        return mHands;
    }

    public InPlay getInPlay() { return mPlayCards; }

    public boolean DealHiddenCard(int player) {
        if (mDealHiddenCardState == null || mDealHiddenCardState.getStatus() == State.Status.DONE) {
            mDealHiddenCardState = new DealCard(mHands.get(player), mDeck, true, null);
        }
        return mDealHiddenCardState.Run();
    }

    public boolean DealActiveCard(int player) {
        if (mDealActiveCardState == null || mDealActiveCardState.getStatus() == State.Status.DONE) {
            mDealActiveCardState = new DealCard(mHands.get(player), mDeck, false, null);
        }
        return mDealActiveCardState.Run();
    }

    public void DrawCard() {
        Card c = mDeck.Draw();
        mPlayCards.AddCard(c);
        Director.instance().getEventSystem().Fire(EventSystem.EventType.DRAW_PLAY_CARD, c);
    }

    public Card GetTopPlayCard() {
        return mPlayCards.GetTopCard();
    }

    public List<Card> GetPlayCards() {
        return mPlayCards.GetCards();
    }



    public Deck getDeck() {
        return mDeck;
    }

    public boolean AddPlayCard(Hand hand, Card activeCard) {
        Card top = GetTopPlayCard();
        if (activeCard.compareTo(top) > -1) {
            GetPlayCards().add(GetPlayCards().size(), activeCard);
            hand.getActiveCards().remove(activeCard);
            Director.instance().getEventSystem().Fire(EventSystem.EventType.CARD_PLAY_SUCCESS, activeCard, hand);
            return true;
        }
        else {
            Director.instance().getEventSystem().Fire(EventSystem.EventType.CARD_PLAY_FAILED, activeCard, hand);
            return false;
        }
    }

}
