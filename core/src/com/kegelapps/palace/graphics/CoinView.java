package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kegelapps.palace.graphics.utils.CardUtils;

/**
 * Created by Ryan on 2/23/2016.
 */
public class CoinView extends Actor {

    private TextureAtlas.AtlasRegion mCoinRegion;

    public CoinView(CardUtils.CoinType type) {
        mCoinRegion = CardUtils.getCoin(type);
        setWidth(mCoinRegion.originalWidth);
        setHeight(mCoinRegion.originalHeight);
        setOrigin(mCoinRegion.originalWidth/2, mCoinRegion.originalWidth/2);
        setScale(1, 1);
        setRotation(0);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.draw(mCoinRegion, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
    }
}
