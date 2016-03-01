package com.kegelapps.palace.graphics;

import aurelienribon.tweenengine.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.animations.Animation;
import com.kegelapps.palace.animations.AnimationFactory;
import com.kegelapps.palace.graphics.utils.CardUtils;
import com.kegelapps.palace.tween.MessageBandAccessor;

/**
 * Created by Ryan on 1/19/2016.
 */
public class MessageBandView extends Actor implements TweenCallback{

    private Pixmap mPixmap, mShadowPix;
    private Texture mTexture, mShadowTex;
    private float mShadowLength;

    private float mAlpha;

    private Timeline mAnimation;

    private TextView mText;
    private Vector3 mCenter, mConvert;
    private float mWindowX, mWindowY;
    private boolean mChanged = false;

    Animation.AnimationStatusListener mListener;

    public MessageBandView() {
        mPixmap = new Pixmap(Director.instance().getScreenWidth(), CardUtils.getCardHeight(), Pixmap.Format.RGBA8888);
        mPixmap.setColor(Color.WHITE);
        mPixmap.fillRectangle(0, 0, mPixmap.getWidth(), mPixmap.getHeight());
        mTexture = new Texture(mPixmap);

        mShadowPix = new Pixmap(Director.instance().getScreenWidth(), CardUtils.getCardHeight(), Pixmap.Format.RGBA8888);
        mShadowPix.setColor(Color.BLACK);
        mShadowPix.fillRectangle(0, 0, mPixmap.getWidth(), mPixmap.getHeight());

        mShadowTex = new Texture(mShadowPix);

        mShadowLength = CardUtils.getCardHeight() * 0.1f;

        mText = new TextView(Director.instance().getGameFont());

        setWidth(mPixmap.getWidth());
        setHeight(mPixmap.getHeight());

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
        //OrthographicCamera cam = (OrthographicCamera) Director.instance().getScene().getCamera();
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
        batch.setColor(0, 0, 0, mAlpha*0.5f);
        batch.draw(mShadowTex, getX()+mShadowLength, getY()-mShadowLength, getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation(), 0, 0, mShadowPix.getWidth(), mShadowPix.getHeight(), false, false);
        batch.setColor(getColor().r, getColor().g, getColor().b, mAlpha);
        batch.draw(mTexture, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation(), 0, 0, mPixmap.getWidth(), mPixmap.getHeight(), false, false);
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
