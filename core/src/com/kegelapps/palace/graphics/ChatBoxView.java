package com.kegelapps.palace.graphics;

import aurelienribon.tweenengine.*;
import aurelienribon.tweenengine.equations.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.animations.Animation;
import com.kegelapps.palace.animations.AnimationFactory;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.graphics.utils.HandUtils;
import com.kegelapps.palace.tween.ActorAccessor;

/**
 * Created by keg45397 on 4/28/2016.
 */
public class ChatBoxView extends Table implements TweenCallback {

    Texture mTriangle;
    NinePatch mNinePatch;
    private Label mTextLabel;
    final private float maxWidth = Director.instance().getViewWidth() / 2.0f;
    final private float maxHeight = Director.instance().getViewHeight() / 2.0f;;
    final private float mPad = Director.instance().getViewWidth() * 0.010f;

    private Vector3 mTrianglePosition;
    private Vector2 mBoxPosition, mOldCalcs;
    private String mOldText = "";
    private HandUtils.HandSide mSide;

    private Timeline mAnimation;
    Animation.AnimationStatusListener mListener;

    private TweenCallback mOpenCallback, mCloseCallback;

    public interface ChatBoxStatusListener {
        void onOpened();
        void onClosed();
    }

    private ChatBoxStatusListener mChatStatusListener;


    public ChatBoxView(int radius) {
        int h = 5 * radius;
        int w = 5 * radius;
        mTrianglePosition = new Vector3();
        mBoxPosition = new Vector2();
        mOldCalcs = new Vector2();


        createBoxTexture(radius, h, w);
        setPosition(0,0);

        mTextLabel = new Label("", new Label.LabelStyle(Director.instance().getAssets().get("small_font", BitmapFont.class), Color.BLACK));
        mSide = HandUtils.HandSide.SIDE_BOTTOM;
    }

    private void createBoxTexture(int radius, int h, int w) {
        Pixmap p = new Pixmap(w/2, h/2, Pixmap.Format.RGBA8888);
        p.setColor(Color.BLACK);
        p.fillTriangle(p.getWidth() / 2, 0, 0, p.getHeight(), p.getWidth(), p.getHeight());
        mTriangle = new Texture(p);


        Pixmap roundedRect = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        roundedRect.setColor(Color.BLACK);
        roundedRect.fillRectangle(0, radius, roundedRect.getWidth(), roundedRect.getHeight() - 2 * radius);
        //roundedRect.setColor(Color.BLUE);
        roundedRect.fillRectangle(radius, 0, roundedRect.getWidth() - 2 * radius, roundedRect.getHeight());
        //roundedRect.setColor(Color.RED);
        roundedRect.fillCircle(radius, radius, radius);
        roundedRect.fillCircle(radius, roundedRect.getHeight() - radius, radius);
        roundedRect.fillCircle(roundedRect.getWidth() - radius, radius, radius);
        roundedRect.fillCircle(roundedRect.getWidth() - radius, roundedRect.getHeight() - radius, radius);
        setWidth(maxWidth);
        setHeight(maxHeight);

        mNinePatch = new NinePatch(new Texture(roundedRect), 2 * radius, 2 * radius, 2 * radius, 2 * radius);
    }

    @Override
    protected void drawBackground(Batch batch, float parentAlpha, float x, float y) {
        super.drawBackground(batch, parentAlpha, x, y);
        Color c = new Color(batch.getColor());
        batch.setColor(getColor().r, getColor().g, getColor().b, 0.85f);
        batch.draw(mTriangle, x+mTrianglePosition.x, y+mTrianglePosition.y, mTriangle.getWidth() / 2.0f, mTriangle.getHeight() / 2.0f,
                mTriangle.getWidth(), mTriangle.getHeight(),
                getScaleX(), getScaleY(), mTrianglePosition.z,
                0, 0, mTriangle.getWidth(), mTriangle.getHeight(), false, false);

        //Logic.log().info(String.format("drawing height: %f %f", getHeight(),mPad*2));
        mNinePatch.draw(batch, x+mBoxPosition.x, y+mBoxPosition.y, getWidth()+mPad*2, getHeight()+mPad*2);
        batch.setColor(c);
    }

    @Override
    public void layout() {
        mOldCalcs.set(mTextLabel.getGlyphLayout().width, mTextLabel.getGlyphLayout().height);
        Logic.get().log().info(String.format("Texts: %s %s", mOldText, mTextLabel.getText().toString()));
        super.layout();
        //if (mOldCalcs.x != mTextLabel.getGlyphLayout().width || mOldCalcs.y != mTextLabel.getGlyphLayout().height) {
        if (!mOldText.equals(mTextLabel.getText().toString())) {
            setHeight(mTextLabel.getPrefHeight());
            setWidth(mTextLabel.getGlyphLayout().width);
            Logic.get().log().info(String.format("Heights: %f %f", mTextLabel.getPrefHeight(), mTextLabel.getGlyphLayout().height));
            updatePosition();
            mOldText = mTextLabel.getText().toString();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        //setPosition(getX()+0.9f, getY()+0.9f);
    }

    @Override
    protected void drawChildren(Batch batch, float parentAlpha) {
        float x = getX();
        float y = getY();
        setPosition(x+mBoxPosition.x+mPad, y+mBoxPosition.y+mPad);
        super.drawChildren(batch, parentAlpha);
        setPosition(x,y);
    }

    private void updatePosition() {
        float x = 0;
        float y = 0;
        float textHeight = mTextLabel.getGlyphLayout().height + mPad*2;
        float textWidth = mTextLabel.getGlyphLayout().width + mPad *2;
        float textPrefHeight = mTextLabel.getPrefHeight() + mPad*2;
        float textPrefWidth = mTextLabel.getPrefWidth() + mPad *2;

        switch (mSide) {
            default:
            case SIDE_LEFT:
                mTrianglePosition.set(x, y+(Director.instance().getViewHeight() - mTriangle.getHeight()) / 2.0f, 90);
                mBoxPosition.set(x+mTriangle.getWidth(), y + (Director.instance().getViewHeight() - textPrefHeight) / 2.0f);
                break;
            case SIDE_RIGHT:
                mTrianglePosition.set(x+Director.instance().getViewWidth() - mTriangle.getWidth(), y+(Director.instance().getViewHeight() - mTriangle.getHeight()) / 2.0f, 270);
                mBoxPosition.set(x+Director.instance().getViewWidth() - mTriangle.getWidth()-textWidth, y+(Director.instance().getViewHeight() - textPrefHeight) / 2.0f);
                break;
            case SIDE_TOP:
                mTrianglePosition.set(x+(Director.instance().getViewWidth() - mTriangle.getWidth()) / 2.0f, y+(Director.instance().getViewHeight() - mTriangle.getHeight()), 0);
                mBoxPosition.set(x+(Director.instance().getScreenWidth() - textWidth) /2.0f, y+(Director.instance().getViewHeight() - mTriangle.getHeight()-(mTextLabel.getPrefHeight()+mPad*2)));
                break;
            case SIDE_BOTTOM:
                mTrianglePosition.set(x+(Director.instance().getViewWidth() - mTriangle.getWidth()) / 2.0f, y, 180);
                mBoxPosition.set(x+(Director.instance().getScreenWidth() - textWidth) /2.0f, y+mTriangle.getHeight());
                break;
        }
    }

    private void calculatePosition() {
        //reset();
        setWidth(maxWidth);
        setHeight(maxHeight);
        Cell c = getCell(mTextLabel);
        if (c == null)
            add(mTextLabel).prefHeight(maxHeight).prefWidth(maxWidth).left();
        updatePosition();
    }

    public void setText(String text, HandUtils.HandSide side) {
        mTextLabel.setText(text);
        mTextLabel.setWrap(true);
        mSide = side;
        if (text.equals(mOldText))
            return;
        calculatePosition();
    }

    public void setTextColor(Color c) {
        mTextLabel.getStyle().fontColor = new Color(c);
    }

    public String getText() {
        return mTextLabel.getText().toString();
    }

    public float getEntireHeight() {
        return mTriangle.getHeight() + getHeight()+mPad*2;
    }

    public float getEntireWidth() {
        return mTriangle.getWidth() + getWidth()+mPad*2;
    }

    public void showChatOld(final String message, final HandUtils.HandSide side, float length, Color textColor, final boolean pause) {
        if (pause) {
            AnimationFactory.get().pauseIncrement();
            mListener = new Animation.AnimationStatusListener() {
                @Override
                public void onEnd(Animation animation) {
                    AnimationFactory.get().pauseDecrement();
                }
            };
        }
        setTextColor(textColor);
        TweenEquation eq = TweenEquations.easeInOutElastic;
        Vector2 startPos = new Vector2();
        switch (side) {
            case SIDE_LEFT:
                startPos.set(-getEntireWidth(), 0); break;
            case SIDE_RIGHT:
                startPos.set(getEntireWidth(), 0); break;
            case SIDE_TOP:
                startPos.set(0,getEntireHeight()); break;
            case SIDE_BOTTOM:
                startPos.set(0,-getEntireHeight()); break;
        }
        mAnimation = Timeline.createSequence();
        setPosition(startPos.x, startPos.y);
        mAnimation.push(Tween.set(this, ActorAccessor.POSITION_XY).target(startPos.x, startPos.y));
        mAnimation.push(Tween.call(new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                setText(message, side);
            }
        }));
        mAnimation.push(Tween.to(this, ActorAccessor.POSITION_XY, 0.7f).target(0,0).ease(eq));
        mAnimation.pushPause(length);
        mAnimation.push(Tween.to(this, ActorAccessor.POSITION_XY, 0.7f).target(startPos.x, startPos.y).ease(eq));

        mAnimation.setCallback(this);
        mAnimation.setCallbackTriggers(TweenCallback.END | TweenCallback.BEGIN );


        mAnimation.start(Director.instance().getTweenManager());
    }

    public void showChat(final String message, final HandUtils.HandSide side, Color textColor, boolean pause) {
        if (pause)
            AnimationFactory.get().pauseIncrement();

        setTextColor(textColor);
        TweenEquation eq = TweenEquations.easeInOutExpo;
        Vector2 startPos = calculateStartPosition(side);
        Director.instance().getTweenManager().killTarget(this);
        mAnimation = Timeline.createSequence();
        setPosition(startPos.x, startPos.y);
        mSide = side;
        mAnimation.push(Tween.set(this, ActorAccessor.POSITION_XY).target(startPos.x, startPos.y));
        mAnimation.push(Tween.call(new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                setText(message, side);
            }
        }));
        mAnimation.push(Tween.to(this, ActorAccessor.POSITION_XY, 0.7f).target(0,0).ease(eq));

        if (mOpenCallback == null)
            mOpenCallback = new TweenCallback() {
                @Override
                public void onEvent(int type, BaseTween<?> source) {
                    if (type == END && mChatStatusListener != null)
                        mChatStatusListener.onOpened();
                }
            };
        mAnimation.setCallback(mOpenCallback);
        mAnimation.setCallbackTriggers(TweenCallback.END );


        mAnimation.start(Director.instance().getTweenManager());
    }

    public void closeChat(boolean unpause) {
        if (unpause)
            AnimationFactory.get().pauseDecrement();
        TweenEquation eq = TweenEquations.easeInOutExpo;

        Vector2 startPos = calculateStartPosition(mSide);
        float dst = startPos.dst(new Vector2(getX(), getY()));
        if (dst < 1.0f)
            return;
        Director.instance().getTweenManager().killTarget(this);
        mAnimation = Timeline.createSequence();
        setPosition(getX(), getY());
        mAnimation.push(Tween.set(this, ActorAccessor.POSITION_XY).target(getX(), getY()));
        mAnimation.push(Tween.to(this, ActorAccessor.POSITION_XY, 0.7f).target(startPos.x,startPos.y).ease(eq));
        mAnimation.push(Tween.call(new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                clearText();
            }
        }));

        if (mCloseCallback == null)
            mCloseCallback = new TweenCallback() {
                @Override
                public void onEvent(int type, BaseTween<?> source) {
                    if (type == END && mChatStatusListener != null)
                        mChatStatusListener.onClosed();
                }
            };
        mAnimation.setCallback(mCloseCallback);
        mAnimation.setCallbackTriggers(TweenCallback.END );


        mAnimation.start(Director.instance().getTweenManager());

    }


    private Vector2 calculateStartPosition(HandUtils.HandSide side) {
        Vector2 startPos = new Vector2();
        switch (side) {
            case SIDE_LEFT:
                startPos.set(-getEntireWidth(), 0); break;
            case SIDE_RIGHT:
                startPos.set(getEntireWidth(), 0); break;
            case SIDE_TOP:
                startPos.set(0,getEntireHeight()); break;
            case SIDE_BOTTOM:
                startPos.set(0,-getEntireHeight()); break;
        }
        return startPos;
    }

    @Override
    public void onEvent(int type, BaseTween<?> source) {
        if (type == TweenCallback.BEGIN) {
            if (mListener != null)
                mListener.onBegin(null);
        }
        if (type == TweenCallback.END) {
            clearText();
            if (mListener != null)
                mListener.onEnd(null);
            mListener = null;
        }
    }

    private void clearText() {
        mTextLabel.setText("");
    }

    public void setChatStatusListener(ChatBoxStatusListener listener) {
        mChatStatusListener = listener;
    }

}
