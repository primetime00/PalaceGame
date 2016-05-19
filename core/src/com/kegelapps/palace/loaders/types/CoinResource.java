package com.kegelapps.palace.loaders.types;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by keg45397 on 3/1/2016.
 */
public class CoinResource implements Disposable {

    public enum CoinType {
        GOLD,
        SILVER,
        BRONZE
    }

    private TextureAtlas mCoinAtlas;
    private float mScale;
    private Vector2 mOrigin;

    public CoinResource(String directory, float scale) {
        String filename = directory+"/coins.pack";
        TextureAtlas atlas;
        atlas = new TextureAtlas(new FileHandle(filename));
        if (atlas == null || scale == 0) {
            throw new RuntimeException("Coin atlas cannot be null or scale can't be 0!");
        }
        this.mCoinAtlas = atlas;
        mScale = scale;
        mOrigin = new Vector2(mCoinAtlas.findRegion("gold_coin").getRegionWidth()/2.0f, mCoinAtlas.findRegion("gold_coin").getRegionHeight()/2.0f);
    }

    public TextureAtlas.AtlasRegion getCoin(CoinType type) {
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

    public float getScale() {
        return mScale;
    }

    public float getCoinOriginX() {
        return mOrigin.x;
    }

    public float getCoinOriginY() {
        return mOrigin.y;
    }

    @Override
    public void dispose() {
        mCoinAtlas.dispose();

        mOrigin = null;
        mCoinAtlas = null;
    }
}
