package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Created by keg45397 on 1/14/2016.
 */
public class TextView extends Actor{

    private String mText = "";
    private BitmapFont mFont;
    private GlyphLayout mLayout;
    private boolean mBorder;
    private float mFudge = 0.0f;


    public TextView(BitmapFont fnt) {
        this(fnt, false);
    }

    public TextView(BitmapFont fnt, boolean border) {
        mFont = fnt;
        mBorder = border;
    }

    public void setFudge(float percent) {
        mFudge = percent;
    }

    public void setText(String txt) {
        if (txt.equals(mText))
            return;
        mText = txt;
        if (mLayout == null)
            mLayout = new GlyphLayout();
        mLayout.setText(mFont, txt);
        setHeight(mLayout.height);
        setWidth(mLayout.width);
    }

    public String getText() { return mText; }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (mBorder) {
            mFont.setColor(Color.BLACK);
            mFont.draw(batch, mText, getX() - 2, getY());
            mFont.draw(batch, mText, getX() + 2, getY());
            mFont.draw(batch, mText, getX(), getY() - 2);
            mFont.draw(batch, mText, getX(), getY() + 2);
        }
        mFont.setColor(getColor());
        mFont.draw(batch, mText, getX(), getY() + mLayout.height + (mLayout.height * mFudge));
    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        shapes.setColor(Color.BLUE);
        super.drawDebug(shapes);
        shapes.setColor(Color.GREEN);
        shapes.set(ShapeRenderer.ShapeType.Filled);
        shapes.circle(getX(), getY(), 10);
        shapes.set(ShapeRenderer.ShapeType.Line);
    }
}
