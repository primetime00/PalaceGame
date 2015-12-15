package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.kegelapps.palace.Card;
import com.kegelapps.palace.Deck;
import com.kegelapps.palace.Input;

/**
 * Created by keg45397 on 12/9/2015.
 */
public class DeckView extends Sprite implements Input.BoundObject {

    private Deck mDeck;

    private Array<CardView> mCardViewList;

    private TextureAtlas.AtlasRegion mDeckBack;

    public DeckView() {
        super();
        mDeck = new Deck();
        init();
    }


    public DeckView(Deck deck) {
        super();
        assert (deck == null);
        mDeck = deck;
        init();
    }

    private void init() {
        mCardViewList = new Array<>();
        for (Card c : mDeck.GetCards()) {
            mCardViewList.add(new CardView(c));
        }
        mDeckBack = CardUtils.getCardBackDeckRegion();
        setSize(mDeckBack.originalWidth, mDeckBack.originalHeight);
    }

    @Override
    public void draw(Batch batch) {
        if ( mDeck.CountCards() > 4 ) { //lets draw the stack of cards
            setRegion(mDeckBack);
        }
        else if (mDeck.CountCards() <= 4) { //we will draw cascaded cards?
            setTexture(null);
        }
        super.draw(batch);
    }

    @Override
    public Rectangle getBounds() {
        return getBoundingRectangle();
    }

    public CardView getCardView(Card c) {
        for (CardView cardView : mCardViewList) {
            if (cardView.getCard() == c) {
                return cardView;
            }
        }
        return null;
    }
}
