package com.kegelapps.palace.graphics.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.kegelapps.palace.engine.Card;

/**
 * Created by keg45397 on 12/8/2015.
 */
public class CardUtils {
    static private int cardWidth = 750;
    static private int cardHeight = 1089;
    static private TextureAtlas mCardAtlas;
    static private TextureAtlas mCoinAtlas;
    static private float mCoinScale = 0.5f;
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

    public enum CoinType {
        GOLD,
        SILVER,
        BRONZE
    }

    static public void loadCards(CardSize size) {
        String file;
        switch (size) {
            case TINY: file = "cards_tiny.pack"; cardWidth = 75; cardHeight = 109; break;
            default: file = "cards_large.png"; cardWidth = 750; cardHeight = 1089; break;
        }
        mLoadedCardSize = size;
        mCardAtlas = new TextureAtlas(file);
        mCoinAtlas = new TextureAtlas("coins.pack");
        int ov = (int)(getCardWidth() * 0.05f);
        mCardHighlight = new Pixmap(getCardWidth()-ov, getCardHeight()-ov, Pixmap.Format.RGBA8888);
        mCardHighlight.setColor(Color.WHITE);
        mCardHighlight.fillRectangle(0, 0, mCardHighlight.getWidth(), mCardHighlight.getHeight());
        mCardHighlightTexture = new Texture(mCardHighlight);
    }

    static public int getCardWidth() {
        return getCardBackRegion().originalWidth;
    }
    static public int getCardHeight() {
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

    static public TextureAtlas.AtlasRegion getCoin(CoinType type) {
        String val;
        switch (type) {
            case GOLD: val = "gold_coin"; break;
            case SILVER: val = "silver_coin"; break;
            case BRONZE: val = "bronze_coin"; break;
            default: val = null;
        }
        if (val == null)
            return null;
        return mCoinAtlas.findRegion(val);
    }

    static public float getCoinScale() {
        return mCoinScale;
    }

    static public float getCoinOriginX() {
        return mCoinAtlas.findRegion("gold_coin").getRegionWidth()/2.0f;
    }

    static public float getCoinOriginY() {
        return mCoinAtlas.findRegion("gold_coin").getRegionHeight()/2.0f;
    }


}
