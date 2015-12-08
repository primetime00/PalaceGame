package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by keg45397 on 12/8/2015.
 */
public class CardUtils {
    static final int cardWidth = 750;
    static final int cardHeight = 1089;
    static private TextureRegion[][] mRegions;

    static private Texture mCards;

    static public void loadCards(String file) {
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

}
