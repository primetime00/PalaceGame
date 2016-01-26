package com.kegelapps.palace.engine;
import java.util.*;

/**
 * Created by Ryan on 12/5/2015.
 */
public class Deck {

    private List<Card> mCards;

    private List<Card.Rank> mDebugRanks;

    public Deck() {
        mDebugRanks = new ArrayList<>();
        //mDebugRanks.add(Card.Rank.THREE);
        //mDebugRanks.add(Card.Rank.FOUR);
        //mDebugRanks.add(Card.Rank.FIVE);
        mCards = new ArrayList<Card>();

        if (mDebugRanks.size() > 0) { //this is debug mode
            for (Card.Suit s : Card.Suit.values()) {
                for (int rank = 0; rank < Card.Rank.values().length; ++rank) {
                    Card.Rank r = mDebugRanks.get(rank % mDebugRanks.size());
                    mCards.add(new Card(s, r));
                }
            }
        }
        else {
            for (Card.Suit s : Card.Suit.values()) {
                for (Card.Rank r : Card.Rank.values()) {
                    mCards.add(new Card(s, r));
                }
            }
        }
    }

    public void Shuffle() {
        long seed = System.nanoTime();
        Collections.shuffle(mCards, new Random(seed));
    }

    public Card Draw() {
        if (mCards.size() <= 0) {
            return null;
        }
        Card c = mCards.get(0);
        mCards.remove(0);
        return c;
    }

    public List<Card> GetCards() {
        return mCards;
    }

    public int CountCards() {
        return mCards.size();
    }
}
