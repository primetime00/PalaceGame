package com.kegelapps.palace.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryan on 1/25/2016.
 */
public class InPlay {

    private List<Card> mCards;

    public InPlay() {
        mCards = new ArrayList<>();
    }

    public List<Card> GetCards() {
        return mCards;
    }

    public Card GetTopCard() {
        if (mCards.size() > 0)
            return mCards.get(mCards.size()-1);
        return null;
    }

    public void AddCard(Card c) {
        mCards.add(c);
    }
}
