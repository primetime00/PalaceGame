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
    private float mVerticalPad = 0.0f;


    public TextView(BitmapFont fnt) {
        this(fnt, false);
    }

    public TextView(BitmapFont fnt, boolean border) {
        mFont = fnt;
        mBorder = border;
    }

    public void setVerticalOffsetPercent(float percent) {
        mFudge = percent;
    }

    public void setVerticalPadPercent(float percent) {
        mVerticalPad = percent;
        if (mLayout != null)
            setHeight(mLayout.height + (mLayout.height * (mVerticalPad/1.0f)));
    }

    public void setText(String txt) {
        if (txt.equals(mText))
            return;
        mText = txt;
        if (mLayout == null)
            mLayout = new GlyphLayout();
        mLayout.setText(mFont, txt);
        setHeight(mLayout.height + (mLayout.height * (mVerticalPad/1.0f)));
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
        float pad = (mLayout.height * (mVerticalPad/2.0f));
        float offset = (mLayout.height * mFudge);
        float y = getY() + mLayout.height + pad + offset;

        mFont.draw(batch, mText, getX(), y);
        //mFont.draw(batch, mText, getX(), getY() + mLayout.height + (mLayout.height * mFudge) + (mLayout.height * (mVerticalPad/2.0f)));
    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        shapes.set(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.RED);
        shapes.rect(getX(), getY(), getWidth(), getHeight());
        shapes.setColor(Color.GOLDENROD);
        shapes.set(ShapeRenderer.ShapeType.Filled);
        shapes.circle(getX(), getY(), 10);
        shapes.set(ShapeRenderer.ShapeType.Line);
    }
}
