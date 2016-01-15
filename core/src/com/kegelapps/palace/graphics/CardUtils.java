package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;

/**
 * Created by keg45397 on 12/8/2015.
 */
public class CardUtils {
    static private int cardWidth = 750;
    static private int cardHeight = 1089;
    static private TextureAtlas mCardAtlas;
    static private TextureAtlas.AtlasRegion mCardBack = null;
    static private CardSize mLoadedCardSize;

    static private Pixmap mCardHighlight;
    static private Texture mCardHighlightTexture;


    public enum CardSize {
        TINY,
        SMALL,
        MEDIUM,
        LARGE
    }

    static public void loadCards(CardSize size) {
        String file;
        switch (size) {
            case TINY: file = "cards_tiny.pack"; cardWidth = 75; cardHeight = 109; break;
            default: file = "cards_large.png"; cardWidth = 750; cardHeight = 1089; break;
        }
        mLoadedCardSize = size;
        mCardAtlas = new TextureAtlas(file);
        int ov = (int)(getCardWidth() * 0.05f);
        mCardHighlight = new Pixmap(getCardWidth()-ov, getCardHeight()-ov, Pixmap.Format.RGBA8888);
        mCardHighlight.setColor(Color.WHITE);
        mCardHighlight.fillRectangle(0, 0, mCardHighlight.getWidth(), mCardHighlight.getHeight());
        mCardHighlightTexture = new Texture(mCardHighlight);

    }

    static int getCardWidth() {
        return getCardBackRegion().originalWidth;
    }
    static int getCardHeight() {
        return getCardBackRegion().originalHeight;
    }

    static public TextureAtlas.AtlasRegion getCardRegion(Card.Suit suit, Card.Rank rank) {
        String s = (Card.getRankString(rank)+"_of_"+Card.getSuitString(suit)+"s").toLowerCase();
        if ( rank == Card.Rank.JACK || rank == Card.Rank.QUEEN ||
             rank == Card.Rank.KING)
            s = s+"2";
        return mCardAtlas.findRegion(s);
    }

    static public TextureAtlas.AtlasRegion getCardBackRegion() {
        if (mCardBack == null)
            mCardBack = mCardAtlas.findRegion("card_back");
        return mCardBack;
    }

    public static TextureAtlas.AtlasRegion getCardBackDeckRegion() {
        return mCardAtlas.findRegion("back_deck");
    }


    static public int getCardTextureWidth() {
        return cardWidth;
    }
    static public int getCardTextureHeight() {
        return cardHeight;
    }

    static public Texture getCardHighlight() { return mCardHighlightTexture;}

}
