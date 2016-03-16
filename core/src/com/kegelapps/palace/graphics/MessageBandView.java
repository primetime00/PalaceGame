package com.kegelapps.palace.graphics;

import aurelienribon.tweenengine.*;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.kegelapps.palace.CardResource;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.animations.Animation;
import com.kegelapps.palace.animations.AnimationFactory;
import com.kegelapps.palace.tween.ActorAccessor;

/**
 * Created by Ryan on 1/19/2016.
 */
public class MessageBandView extends Group implements TweenCallback{

    private Image mBand;

    private Timeline mAnimation;

    private TextView mText;

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
        mBand = new Image(Director.instance().getAssets().get("messageband", MessageBandView.MessageBandTexture.class));
        mBand.setWidth(width);
        mBand.setHeight(height);
        mShadow = new ShadowView();
        mShadow.setColor(Color.BLACK, 0.5f);
        mText = new TextView(Director.instance().getAssets().get("message_font", BitmapFont.class));

        addActor(mShadow);
        addActor(mBand);
        addActor(mText);

        mShadow.shadowBottomRight(getHeight() * 0.1f, mBand);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Camera cam = this.getStage().getCamera();
        if (cam == null)
            throw new RuntimeException("MessageBandView has a null camera!");
        super.draw(batch, parentAlpha);
    }

    public void setText(String text) {
        mText.setText(text);
        mText.setX((getWidth() - mText.getWidth()) / 2.0f);
        mText.setY( (getHeight() - mText.getHeight()) / 2.0f );
    }

    public void setTextColor(Color c) {
        mText.setColor(c);
    }

    public String getText() {
        return mText.getText();
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
        setX(Director.instance().getScreenWidth()-30);
        setY((Director.instance().getScreenHeight() - getHeight()) / 2.0f);
        setText(message);
        setTextColor(textColor);
        TweenEquation eq = TweenEquations.easeInOutQuart;
        mAnimation = Timeline.createSequence().push(Tween.to(this, ActorAccessor.POSITION_X, 0.7f).target(0).ease(eq));
        mAnimation.pushPause(length);
        mAnimation.push(Tween.to(this, ActorAccessor.POSITION_X, 0.7f).target(-Director.instance().getScreenWidth()).ease(eq));

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
