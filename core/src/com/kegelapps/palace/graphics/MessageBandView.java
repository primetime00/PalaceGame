package com.kegelapps.palace.graphics;

import aurelienribon.tweenengine.*;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kegelapps.palace.CardResource;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.animations.Animation;
import com.kegelapps.palace.animations.AnimationFactory;
import com.kegelapps.palace.tween.MessageBandAccessor;

/**
 * Created by Ryan on 1/19/2016.
 */
public class MessageBandView extends Actor implements TweenCallback{

    private Texture mTexture, mShadowTex;

    private float mAlpha;

    private Timeline mAnimation;

    private TextView mText;
    private Vector3 mCenter, mConvert;
    private float mWindowX, mWindowY;
    private boolean mChanged = false;

    private ShadowView mShadow;

    Animation.AnimationStatusListener mListener;

    static public class MessageBandTexture extends Texture {

        public MessageBandTexture(Pixmap pixmap) {
            super(pixmap);
        }
    }

    public MessageBandView() {
        int height = Director.instance().getAssets().get("cards_tiny.pack", CardResource.class).getHeight();
        int width = Director.instance().getScreenWidth();
        setWidth(width);
        setHeight(height);
        mTexture = Director.instance().getAssets().get("messageband");
        mShadow = new ShadowView();
        mShadow.setColor(Color.BLACK, 0.5f);
        mText = new TextView(Director.instance().getAssets().get("default_font", BitmapFont.class));

        setWindowX(0);
        setWindowY((Director.instance().getScreenHeight() + getHeight())/2.0f);
        mCenter = new Vector3();
        mConvert = new Vector3();
        setAlpha(1.0f);
    }

    public float getWindowX() {
        return mWindowX;
    }

    public void setWindowX(float mWindowX) {
        this.mWindowX = mWindowX;
        mChanged = true;
    }

    public float getWindowY() {
        return mWindowY;
    }

    public void setWindowY(float mWindowY) {
        this.mWindowY = mWindowY;
        mChanged = true;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Camera cam = this.getStage().getCamera();
        if (cam == null)
            throw new RuntimeException("MessageBandView has a null camera!");
        if (mChanged) {
            mChanged = false;
            mCenter.set(mWindowX, mWindowY, 0);
            mConvert.set(mCenter);
        }
        Vector3 pos = cam.unproject(mCenter);
        setX(pos.x);
        setY(pos.y);
        mCenter.set(mConvert);
        super.draw(batch, parentAlpha);
        mShadow.shadowBottomRight(getHeight() * 0.1f, this);
        mShadow.draw(batch, parentAlpha);
        batch.setColor(getColor().r, getColor().g, getColor().b, mAlpha);
        batch.draw(mTexture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation(), 0, 0, mTexture.getWidth(), mTexture.getHeight(), false, false);
        if (mText.getText().length() > 0) {
            mText.setX((getWidth() - mText.getWidth())/2.0f + getX());
            mText.setY((getY() + mText.getHeight()) + (( getHeight() - mText.getHeight())/2.0f));
            mText.draw(batch, mAlpha);
        }
    }

    public void setText(String text) {
        mText.setText(text);
    }

    public void setTextColor(Color c) {
        mText.setColor(c);
    }

    public String getText() {
        return mText.getText();
    }

    public float getAlpha() {
        return mAlpha;
    }

    public void setAlpha(float val) {
        mAlpha = val;
    }

    public void showMessage(String message, float length, Color textColor, final boolean pause) {
        if (pause) {
            AnimationFactory.get().pauseIncrement();
            mListener = new Animation.AnimationStatusListener() {
                @Override
                public void onEnd(Animation animation) {
                    AnimationFactory.get().pauseDecrement();
                }
            };
        }
        setWindowX(Director.instance().getScreenWidth());
        setText(message);
        setTextColor(textColor);
        TweenEquation eq = TweenEquations.easeInOutQuart;
        mAnimation = Timeline.createSequence().push(Tween.to(this, MessageBandAccessor.POS_X, 0.7f).target(0).ease(eq));
        mAnimation.pushPause(length);
        mAnimation.push(Tween.to(this, MessageBandAccessor.POS_X, 0.7f).target(-Director.instance().getScreenWidth()).ease(eq));

        mAnimation.setCallback(this);
        mAnimation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END );


        mAnimation.start(Director.instance().getTweenManager());
    }

    @Override
    public void onEvent(int type, BaseTween<?> source) {
        if (type == TweenCallback.BEGIN) {
            if (mListener != null)
                mListener.onBegin(null);
        }
        if (type == TweenCallback.END) {
            setText("");
            if (mListener != null)
                mListener.onEnd(null);
            mListener = null;
        }
    }
}
