package com.kegelapps.palace;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Disposable;
import com.kegelapps.palace.engine.Card;

/**
 * Created by keg45397 on 3/1/2016.
 */
public class CardResource implements Disposable{

    public enum CardSize {
        TINY,
        SMALL,
        MEDIUM,
        LARGE
    }

    private TextureAtlas mCardAtlas;
    private CardSize mSize;

    private TextureAtlas.AtlasRegion mCardBack = null;

    private Pixmap mCardHighlight;
    private Texture mCardHighlightTexture;

    public CardResource(TextureAtlas mCardAtlas, CardSize size) {
        if (mCardAtlas == null || size == null) {
            throw new RuntimeException("Card atlas and size cannot be null!");
        }
        this.mCardAtlas = mCardAtlas;
        mSize = size;

        int ov = (int)(getWidth() * 0.05f);
        mCardHighlight = new Pixmap(getWidth()-ov, getHeight()-ov, Pixmap.Format.RGBA8888);
        mCardHighlight.setColor(Color.WHITE);
        mCardHighlight.fillRectangle(0, 0, mCardHighlight.getWidth(), mCardHighlight.getHeight());
        mCardHighlightTexture = new Texture(mCardHighlight);
    }

    public TextureAtlas.AtlasRegion getCard(Card.Suit suit, Card.Rank rank) {
        String s = (Card.getRankString(rank)+"_of_"+Card.getSuitString(suit)+"s").toLowerCase();
        if ( rank == Card.Rank.JACK || rank == Card.Rank.QUEEN ||
                rank == Card.Rank.KING)
            s = s+"2";
        return mCardAtlas.findRegion(s);
    }

    public TextureAtlas.AtlasRegion getCardBack() {
        if (mCardBack == null)
            mCardBack = mCardAtlas.findRegion("card_back");
        return mCardBack;
    }

    public TextureAtlas.AtlasRegion getCardBackDeck() {
        return mCardAtlas.findRegion("back_deck");
    }

    public int getWidth() {
        return getCardBack().originalWidth;
    }

    public int getHeight() {
        return getCardBack().originalHeight;
    }

    public Texture getCardHighlight() {
        return mCardHighlightTexture;
    }

    @Override
    public void dispose() {
        mCardAtlas.dispose();
        mCardHighlight.dispose();
        mCardHighlightTexture.dispose();

        mCardBack = null;
        mCardAtlas = null;
        mCardHighlight = null;
        mCardHighlightTexture = null;
    }
}
