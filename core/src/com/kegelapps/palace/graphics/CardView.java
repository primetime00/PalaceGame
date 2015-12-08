package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by keg45397 on 12/8/2015.
 */
public class CardView extends Sprite{

    public CardView(TextureRegion region) {
        super(CardUtils.getCardTexture(0));
    }

}
