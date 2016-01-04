package com.kegelapps.palace.engine;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.states.DealCard;
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

    List<Card> mCardsInPlay;
    List<Card> mBurntCards;

    //should the table have the hands?
    List<Hand> mHands;

    //state machine
    DealCard mDealHiddenCardState;
    DealCard mDealActiveCardState;

    private TableListener mTableListener;

    public interface TableListener {
        public void onDealCard(Hand hand, Card c);
    }



    public Table(Deck deck, int numberOfPlayers, BlockingQueue<Runnable> queue) {
        assert (numberOfPlayers != 3 || numberOfPlayers != 4);
        mDeck = deck;
        mCardsInPlay = new ArrayList<>();
        mBurntCards = new ArrayList<>();
        mHands = new ArrayList<>();
        for (int i=0; i<numberOfPlayers; ++i) {
            mHands.add(new Hand(i, i==0 ? Hand.HandType.HUMAN : Hand.HandType.CPU, mDeck, queue));
        }
    }

    public List<Hand> getHands() {
        return mHands;
    }

    public void DrawPlayCard() {
        Card c = mDeck.Draw();
        mCardsInPlay.add(c);
    }

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

    public void PlayCard() {
        Card c = mDeck.Draw();
        mCardsInPlay.add(c);
        Director.instance().getEventSystem().Fire(EventSystem.EventType.DRAW_PLAY_CARD, c);
        System.out.print("Card in play is: " + mCardsInPlay.get(0) + "\n");
    }



    public Deck getDeck() {
        return mDeck;
    }

    public void setTableListener(TableListener listener) {
        mTableListener = listener;
    }
}
