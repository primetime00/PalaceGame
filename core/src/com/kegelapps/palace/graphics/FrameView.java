package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.kegelapps.palace.Director;

/**
 * Created by keg45397 on 3/1/2016.
 */
public class FrameView extends Table{

    private TiledDrawable mCenterTile, mBottomTile, mTopTile, mLeftTile, mRightTile;
    private TextureRegion mGrid[][];
    private ShadowView mShadow;
    private final int sz = 26;
    private boolean mNeedsRedraw;

    public FrameView() {
        super();
        TextureRegion t = ((TextureAtlas) Director.instance().getAssets().get("ui.pack")).findRegion("card-board-small");
        mShadow = new ShadowView();
        mNeedsRedraw = true;
        int w = t.getRegionWidth();
        int h = t.getRegionHeight();
        mGrid = new TextureRegion[3][3];
        mGrid[0][0] = new TextureRegion(t, 0,    0,     sz,         sz);
        mGrid[0][1] = new TextureRegion(t, sz,   0,     w-(sz*2),   sz);
        mGrid[0][2] = new TextureRegion(t, w-sz, 0,     sz,         sz);

        mGrid[1][0] = new TextureRegion(t, 0,    sz,    sz,         h-(sz*2));
        mGrid[1][1] = new TextureRegion(t, sz,   sz,    w-(sz*2),   h-(sz*2));
        mGrid[1][2] = new TextureRegion(t, w-sz, sz,    sz,         h-(sz*2));

        mGrid[2][0] = new TextureRegion(t, 0,    h-sz,  sz,         sz);
        mGrid[2][1] = new TextureRegion(t, sz,   h-sz,  w-(sz*2),   sz);
        mGrid[2][2] = new TextureRegion(t, w-sz, h-sz,  sz,         sz);


        mCenterTile = new TiledDrawable(mGrid[1][1]);
        mTopTile = new TiledDrawable(mGrid[0][1]);
        mBottomTile = new TiledDrawable(mGrid[2][1]);
        mLeftTile = new TiledDrawable(mGrid[1][0]);
        mRightTile = new TiledDrawable(mGrid[1][2]);

        setWidth((sz*14));
        setHeight((sz*14));
        setX(30);
        setY(30);
        pad(sz + sz*0.5f);
    }



    @Override
    public void setWidth(float width) {
        if (width < (sz*3))
            width = (sz*3);
        super.setWidth(width);
    }

    @Override
    public void setHeight(float height) {
        if (height < (sz*3))
            height = (sz*3);
        super.setHeight(height);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    @Override
    protected void drawBackground(Batch batch, float parentAlpha, float x, float y) {
        mShadow.shadowBottomRight(20, this);
        mShadow.setColor(Color.BLACK, 0.5f);
        mShadow.draw(batch, parentAlpha);

        //top
        batch.draw(mGrid[0][0], x, y+getHeight()-sz);
        batch.draw(mGrid[0][2], x + getWidth()-sz, y+getHeight()-sz);
        mTopTile.draw(batch, x + sz, y+getHeight()-sz, getWidth() - (sz*2), sz);

        //bottom
        batch.draw(mGrid[2][0], x, y);
        batch.draw(mGrid[2][2], x + getWidth()-sz, y);
        mBottomTile.draw(batch, x + sz, y, getWidth() - (sz*2), sz);

        //center
        mLeftTile.draw(batch, x, y + sz, sz, getHeight() - (sz*2));
        mRightTile.draw(batch, x + getWidth() - sz, y + sz, sz, getHeight() - (sz*2));
        mCenterTile.draw(batch, x + sz, y + sz, getWidth() - (sz*2), getHeight() - (sz*2));

    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (needsRedraw())
            update();
    }

    public boolean needsRedraw() {
        return mNeedsRedraw;
    }

    public void update() {
        mNeedsRedraw = false;
    }

    public void mark() {
        mNeedsRedraw = true;
    }
}
