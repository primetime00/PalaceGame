package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kegelapps.palace.loaders.types.CoinResource;
import com.kegelapps.palace.Director;

/**
 * Created by Ryan on 2/23/2016.
 */
public class CoinView extends Actor {

    private TextureAtlas.AtlasRegion mCoinRegion;
    private CoinResource.CoinType mType;

    public CoinView(CoinResource.CoinType type) {
        mType = type;
        mCoinRegion = Director.instance().getAssets().get("coins", CoinResource.class).getCoin(type);
        setWidth(mCoinRegion.originalWidth);
        setHeight(mCoinRegion.originalHeight);
        setOrigin(mCoinRegion.originalWidth/2, mCoinRegion.originalWidth/2);
        setScale(1, 1);
        setRotation(0);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.draw(mCoinRegion, getX(), getY(), getOriginX(), getOriginY(), getWidth(),
                getHeight(), getScaleX(), getScaleY(), getRotation());
    }

    public CoinResource.CoinType getType() {
        return mType;
    }
}
