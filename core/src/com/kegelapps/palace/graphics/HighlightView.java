package com.kegelapps.palace.graphics;

import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kegelapps.palace.CardResource;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.tween.HighlightAccessor;

/**
 * Created by keg45397 on 1/15/2016.
 */
public class HighlightView {

    protected float mAlpha;
    protected float mMinAlpha;
    protected float mDuration;
    protected static float mMaxAlpha = 0.8f;
    private Tween mAnimation;
    private boolean mVisible;
    protected Color mColor;

    public HighlightView() {
        mColor = Color.WHITE;
        mMinAlpha = 0;
        mAlpha = mMaxAlpha;
        mDuration = 0.4f;
    }

    public void setAlpha(float v) { mAlpha = v;
        if (mAlpha > mMaxAlpha)
            mAlpha = mMaxAlpha;
        if (mAlpha < mMinAlpha)
            mAlpha = mMinAlpha;
    }

    public float getAlpha() {
        return mAlpha;
    }

    public void setColor (Color c) {
        mColor = c;
    }

    public void draw(Batch batch, Actor actor) {
        assert (actor != null);
        float pos = (actor.getWidth() * 0.05f)/2.0f;
        Color c = batch.getColor();
        batch.setColor(mColor.r,mColor.g,mColor.b,mAlpha);
        batch.draw(Director.instance().getAssets().get("cards", CardResource.class).getCardHighlight(),
                actor.getX()+pos, actor.getY()+pos,
                actor.getOriginX(),actor.getOriginY(),
                actor.getWidth(),actor.getHeight(),
                actor.getScaleX(),actor.getScaleY(),actor.getRotation(), 0, 0,
                Director.instance().getAssets().get("cards", CardResource.class).getCardHighlight().getWidth(),
                Director.instance().getAssets().get("cards", CardResource.class).getCardHighlight().getHeight(),
                false, false);
        batch.setColor(c);
    }

    public void show() {
        if (mVisible)
            return;
        mAnimation = Tween.to(this, HighlightAccessor.ALPHA, mDuration).target(mMinAlpha).repeatYoyo(Tween.INFINITY, 0.0f);
        mAnimation.start(Director.instance().getTweenManager());
        mVisible = true;
    }

    public void hide() {
        if (!mVisible)
            return;
        if (mAnimation != null) {
            //mAnimation.free();
            Director.instance().getTweenManager().killTarget(this);
        }
        mAlpha = mMaxAlpha;
        mVisible = false;
    }

    public boolean isVisible() {
        return mVisible;
    }
}
