package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.kegelapps.palace.Card;

/**
 * Created by keg45397 on 12/8/2015.
 */
public class CardView extends Sprite{

    private Card mCard;
    private Side mSide;
    private TextureAtlas.AtlasRegion mCardRegion;

    enum Side {
        FRONT,
        BACK
    }

    public CardView(Card.Suit suit, Card.Rank rank) {
        super();
        mCard = new Card(suit, rank);
        init();
    }

    public CardView(Card card) {
        super();
        assert(card == null);
        mCard = card;
        init();
    }

    private void init() {
        mCardRegion = CardUtils.getCardRegion(mCard.getSuit(), mCard.getRank());
        setRegion(mCardRegion);
        setSize(mCardRegion.originalWidth, mCardRegion.originalHeight);
        mSide = Side.FRONT;
    }

    public Card getCard() {
        return mCard;
    }


}
