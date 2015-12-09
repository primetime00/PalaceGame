package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by keg45397 on 12/8/2015.
 */
public class CardUtils {
    static private int cardWidth = 750;
    static private int cardHeight = 1089;
    static private TextureRegion[][] mRegions;

    public enum CardSize {
        TINY,
        SMALL,
        MEDIUM,
        LARGE
    }

    static private Texture mCards;

    static public void loadCards(CardSize size) {
        String file;
        switch (size) {
            case TINY: file = "cards_tiny.jpg"; cardWidth = 75; cardHeight = 109; break;
            default: file = "cards_large.jpg"; cardWidth = 750; cardHeight = 1089; break;
        }
        mCards = new Texture(file);
        mRegions = TextureRegion.split(mCards, cardWidth, cardHeight );
    }

    static public Texture getTexture() {
        return mCards;
    }

    static public TextureRegion getCardTexture(int index) {
        if (mRegions == null)
            mRegions = TextureRegion.split(mCards, cardWidth, cardHeight );
        return mRegions[0][1];
    }

    static public TextureRegion getCardBackTexture() {
        if (mRegions == null)
            mRegions = TextureRegion.split(mCards, cardWidth, cardHeight );
        return mRegions[0][0];
    }

    static public int getCardTextureWidth() {
        return cardWidth;
    }
    static public int getCardTextureHeight() {
        return cardHeight;
    }


}
