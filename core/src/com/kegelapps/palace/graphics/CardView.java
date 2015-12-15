package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kegelapps.palace.Card;

/**
 * Created by keg45397 on 12/8/2015.
 */
public class CardView extends Actor {

    private Card mCard;
    private Side mSide;
    private TextureAtlas.AtlasRegion mCardRegion;
    private TextureAtlas.AtlasRegion mCardFace;

    public enum Side {
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
        mCardFace = CardUtils.getCardRegion(mCard.getSuit(), mCard.getRank());
        setSide(Side.FRONT);
        setBounds(0,0,mCardRegion.originalWidth, mCardRegion.originalHeight);
    }

    public Card getCard() {
        return mCard;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.draw(mCardRegion, getX(), getY(), getWidth(), getHeight());
    }

    public void setSide(Side side) {
        mSide = side;
        mCardRegion = side == Side.FRONT ? mCardFace : CardUtils.getCardBackRegion();
    }
}
