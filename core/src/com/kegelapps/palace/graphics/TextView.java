package com.kegelapps.palace.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by keg45397 on 1/14/2016.
 */
public class TextView extends Actor implements Disposable{

    private String mText = "";
    private BitmapFont mFont;
    private GlyphLayout mLayout;
    private float mFudge = 0.0f;
    private float mVerticalPad = 0.0f;


    private FrameBuffer mFrameBuffer;
    private TextureRegion mRegion;


    public TextView(BitmapFont fnt) {
        mFont = fnt;
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
        generateFBO();
    }

    private void generateFBO() {
        if (mFrameBuffer != null)
            mFrameBuffer.dispose();
        mFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        mRegion = new TextureRegion(mFrameBuffer.getColorBufferTexture());
        mRegion.flip(false, true);
        mFrameBuffer.begin();

        Gdx.gl.glClearColor(0f, 0f, 0f, 0f); //transparent black
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT); //clear the color buffer

        SpriteBatch batch = new SpriteBatch();
        batch.begin();
        batch.setColor(getColor());
        mFont.draw(batch, mText, 0, calcY());
        batch.end();
        mFrameBuffer.end();
        if (mText.contains("Select"))
            System.out.print(String.format("Y is %f\n", calcY()));

    }

    private float calcY() {
        float pad = (mLayout.height * (mVerticalPad/2.0f));
        float offset = (mLayout.height * mFudge);
        return mLayout.height + pad + offset+1;
    }

    public String getText() { return mText; }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        if (mRegion == null)
            return;
        Color c = new Color(batch.getColor());
        Color current = new Color(getColor());
        batch.setColor(current.r, current.g, current.b, current.a * parentAlpha);
        batch.draw(mRegion, getX(), getY(), getOriginX(), getOriginY(), mRegion.getRegionWidth(), mRegion.getRegionHeight(), getScaleX(), getScaleY(), getRotation());
        batch.setColor(c);
    }

    @Override
    public void dispose() {
        if (mFrameBuffer != null)
            mFrameBuffer.dispose();
        mRegion = null;
        mFrameBuffer = null;
        mFont = null;
    }
}
