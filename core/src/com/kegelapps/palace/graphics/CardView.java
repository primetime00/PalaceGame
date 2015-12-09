package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.kegelapps.palace.Card;

/**
 * Created by keg45397 on 12/8/2015.
 */
public class CardView extends Sprite{

    private Card mCard;
    private Side mSide;

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
        setSize(CardUtils.getCardTextureWidth(), CardUtils.getCardTextureHeight());
        setRegion(CardUtils.getCardTexture(0));
        mSide = Side.FRONT;
    }


}
