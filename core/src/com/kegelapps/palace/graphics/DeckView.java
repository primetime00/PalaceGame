package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;
import com.kegelapps.palace.Card;
import com.kegelapps.palace.Deck;

/**
 * Created by keg45397 on 12/9/2015.
 */
public class DeckView extends Sprite {

    private Deck mDeck;

    private Array<CardView> mCardViewList;

    public DeckView() {
        super();
        mDeck = new Deck();
        mCardViewList = new Array<>();
        for (Card c : mDeck.GetCards()) {
            mCardViewList.add(new CardView(c));
        }
    }

    public DeckView(Deck deck) {
        super();
        assert (deck == null);
        mDeck = deck;
        mCardViewList = new Array<>();
        for (Card c : mDeck.GetCards()) {
            mCardViewList.add(new CardView(c));
        }
    }

}
