package com.kegelapps.palace;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by keg45397 on 12/7/2015.
 */
public class Table extends EventObject {
    private Deck mDeck; //the deck on the table

    List<Card> mCardsInPlay;
    List<Card> mBurntCards;

    //should the table have the hands?
    List<Hand> mHands;

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


    public void DealNewGame() {
        mBurntCards.clear();
        mCardsInPlay.clear();
        System.out.print("Dealing cards...");
        //first 3 are for hidden
        int i;
        for (i=0; i<3; ++i) {
            for (Hand h : mHands) {
                Card c = mDeck.Draw();
                if (mTableListener != null)
                    mTableListener.onDealCard(h,c);
                h.AddHiddenCard(c);
            }
        }
        //next 7 are active!
        for (i=0; i<7; ++i) {
            for (Hand h : mHands) {
                Card c = mDeck.Draw();
                if (mTableListener != null)
                    mTableListener.onDealCard(h,c);
                h.AddActiveCard(c);
            }
        }
    }

    public void DealHiddenCard(int player) {
        Hand h = mHands.get(player);
        Card c = mDeck.Draw();
        h.AddHiddenCard(c);
    }

    public void DealActiveCard(int player) {
        Hand h = mHands.get(player);
        Card c = mDeck.Draw();
        h.AddActiveCard(c);
    }

    public void PlayCard() {
        Card c = mDeck.Draw();
        mCardsInPlay.add(c);
        Trigger(EventType.DRAW_PLAY_CARD, c);
        System.out.print("Card in play is: " + mCardsInPlay.get(0) + "\n");
    }



    public Deck getDeck() {
        return mDeck;
    }

    public void setTableListener(TableListener listener) {
        mTableListener = listener;
    }
}
