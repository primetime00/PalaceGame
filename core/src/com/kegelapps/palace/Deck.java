package com.kegelapps.palace;
import java.util.*;

/**
 * Created by Ryan on 12/5/2015.
 */
public class Deck {

    private List<Card> mCards;

    public Deck() {
        mCards = new ArrayList<Card>();
        for (Card.Rank r : Card.Rank.values()) {
            for (Card.Suit s : Card.Suit.values()) {
                mCards.add(new Card(s, r));
            }
        }
    }

    public void Shuffle() {
        long seed = System.nanoTime();
        Collections.shuffle(mCards, new Random(seed));
    }

    public List<Card> Draw(int cards) {
        if (mCards.size() <= 0) {
            return null;
        }
        List<Card> drawCards = new ArrayList<>();
        for (int i=0; i<cards; ++i) {
            drawCards.add(mCards.get(0));
            mCards.remove(0);
            if (mCards.size() == 0)
                break;
        }
        return drawCards;
    }

    public Card Draw() {
        if (mCards.size() <= 0) {
            return null;
        }
        Card c = mCards.get(0);
        mCards.remove(0);
        return c;
    }
}
