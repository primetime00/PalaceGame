package com.kegelapps.palace.scenes;

import aurelienribon.tweenengine.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.CardView;
import com.kegelapps.palace.graphics.ClipView;
import com.kegelapps.palace.graphics.ShadowView;
import com.kegelapps.palace.graphics.TextView;
import com.kegelapps.palace.graphics.ui.AcknowledgementDialog;
import com.kegelapps.palace.graphics.ui.MainTable;
import com.kegelapps.palace.graphics.ui.OptionsTable;
import com.kegelapps.palace.graphics.ui.common.StringMap;
import com.kegelapps.palace.tween.ActorAccessor;

/**
 * Created by Ryan on 3/14/2016.
 */
public class IntroScene extends Scene {

    private CardView cards[];
    private TweenCallback mCallback;
    private TextView mTitle;

    private ClipView mClipFrame;
    private MainTable mMainTable;
    private OptionsTable mOptionTable;
    private ShadowView mShadow;
    private boolean mShownAnimation;

    private Timeline mCardFlyAnimation, mCardDropAnimation;


    private enum IntroState {
        BUILD,
        START,
        FLY,
        PAUSE,
        DROP,
        TITLE,
        DONE
    };
    private IntroState mState;

    private AcknowledgementDialog mAckDialog;

    private final float titleFadeTime = 1.0f;
    private final float screenFadeTime = 1.0f;
    private final float initialAnimationDelayTime = 0.0f;


    public IntroScene(Viewport viewport) {
        super(viewport);
        init();
    }

    private void init() {
        Card prevCards[] = new Card[3];
        cards = new CardView[3];
        int i=0;
        while (i != 3) {
            Card.Suit s = Card.Suit.values()[(int)(Math.random() * Card.Suit.values().length)];
            Card.Rank r = Card.Rank.values()[(int)(Math.random() * Card.Rank.values().length)];
            Card current = new Card(s, r);
            if (previousContains(prevCards, s, r)) {
                continue;
            }
            prevCards[i] = current;
            cards[i] = new CardView(current, true);
            cards[i].setSide(CardView.Side.FRONT);
            i++;
        }
        mBackgroundColor = new Color(0.2f, 0.2f, 0.2f, 1);
        mCallback = new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                if (type == TweenCallback.END && mState == IntroState.PAUSE) {
                    if (source == mCardFlyAnimation) { //show the drop animation
                        mState = IntroState.DROP;
                        mShownAnimation = true;
                    }
                    else if (source == mCardDropAnimation) { //show the title
                        mState = IntroState.TITLE;
                    }
                }
            }
        };
        createEvents();

        addListener(new InputListener() {
            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {//Exit game (need a dialog)
                    //Director.instance().getEventSystem().FireLater(EventSystem.EventType.OPTIONS);
                    Director.instance().dispose();
                    Gdx.app.exit();
                }
                return super.keyUp(event, keycode);
            }
        });


        mShadow = new ShadowView();
        mShadow.shadowEntireScreen(0);

        mTitle = new TextView(Director.instance().getAssets().get("title_font_large", BitmapFont.class));
        mTitle.setVerticalPadPercent(0.6f);
        mTitle.setText(StringMap.getString("palace"));
        mTitle.setVerticalOffsetPercent(0.25f);
        mTitle.setColor(Color.RED.r, Color.RED.g, Color.RED.b, 0.0f);
        Vector2 pos = new Vector2();
        pos.x = (getCamera().viewportWidth - mTitle.getWidth())/2.0f;
        pos.y = (getCamera().viewportHeight - mTitle.getHeight())*3/4.0f;
        mTitle.setPosition(pos.x, pos.y);

        mClipFrame = new ClipView();
        mMainTable = new MainTable();
        mOptionTable = new OptionsTable();
        mMainTable.setColor(Color.RED.r, Color.RED.g, Color.RED.b, 0.0f);
        mOptionTable.setColor(Color.RED.r, Color.RED.g, Color.RED.b, 0.0f);
        mShownAnimation = false;
        mState = IntroState.START;

    }

    private void createEvents() {
        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.MAIN_NEW_GAME) {
            @Override
            public void handle(Object params[]) {
                fadeOutToNewGame();
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.MAIN_OPTIONS) {
            @Override
            public void handle(Object params[]) {
                showOptions();
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.OPTIONS_BACK) {
            @Override
            public void handle(Object params[]) {
                showMain();
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.SHOW_ACKNOWLEDGEMENTS) {
            @Override
            public void handle(Object params[]) {
                showAcknowledgements();
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.DISMISS_ACKNOWLEDGEMENTS) {
            @Override
            public void handle(Object params[]) {
                if (mAckDialog != null)
                    hideAcknowledgements();
            }
        });

    }

    private void hideAcknowledgements() {
        Tween.to(mAckDialog, ActorAccessor.ALPHA, 0.35f).target(0.0f).
                setCallbackTriggers(TweenCallback.END).
                setCallback(new TweenCallback() {
                    @Override
                    public void onEvent(int type, BaseTween<?> source) {
                        if (type == END)
                            mAckDialog.remove();
                    }
                }).
                start(getTweenManager());
    }

    private void showAcknowledgements() {
        if (mAckDialog == null)
            mAckDialog = new AcknowledgementDialog(StringMap.getString("acknowledgements"));
        mAckDialog.setColor(1,1,1,0);
        //mAckDialog.setX( (mViewWidth - mAckDialog.getWidth()) / 2.0f);
        //mAckDialog.setY( (mViewHeight - mAckDialog.getHeight()) / 2.0f);
        addActor(mAckDialog);
        mAckDialog.setScreenPercent(0.8f, 0.8f);
        mAckDialog.mark();
        Tween.to(mAckDialog, ActorAccessor.ALPHA, 0.35f).target(1.0f).start(getTweenManager());
    }

    private void showMain() {
        getTweenManager().killTarget(mOptionTable);
        getTweenManager().killTarget(mMainTable);
        Timeline ani = Timeline.createParallel();
        ani.push(Tween.to(mOptionTable, ActorAccessor.POSITION_X, 0.75f).target(mClipFrame.getWidth()).ease(TweenEquations.easeOutQuad));
        ani.push(Tween.to(mMainTable, ActorAccessor.POSITION_X, 0.75f).target(0).ease(TweenEquations.easeOutQuad));
        ani.start(getTweenManager());
    }

    private void showOptions() {
        getTweenManager().killTarget(mOptionTable);
        getTweenManager().killTarget(mMainTable);
        Timeline ani = Timeline.createParallel();
        ani.push(Tween.to(mMainTable, ActorAccessor.POSITION_X, 0.75f).target(-mClipFrame.getWidth()).ease(TweenEquations.easeOutQuad));
        ani.push(Tween.to(mOptionTable, ActorAccessor.POSITION_X, 0.75f).target(0).ease(TweenEquations.easeOutQuad));
        ani.start(getTweenManager());
    }

    private void fadeOutToNewGame() {
        addActor(mShadow);
        mShadow.setColor(Color.BLACK, 0);
        Tween alpha = Tween.to(mShadow, ActorAccessor.ALPHA, screenFadeTime).target(1);
        alpha.setCallbackTriggers(TweenCallback.END);
        alpha.setCallback(new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                if (type == TweenCallback.END)
                    Director.instance().getEventSystem().FireLater(EventSystem.EventType.RESTART_GAME, true);
            }
        });
        alpha.start(getTweenManager());
    }

    private void showTitle() {
        mTitle.setColor(Color.RED.r, Color.RED.g, Color.RED.b, 0.0f);
        mMainTable.setColor(Color.RED.r, Color.RED.g, Color.RED.b, 0.0f);
        mOptionTable.setColor(Color.RED.r, Color.RED.g, Color.RED.b, 1.0f);
        addActor(mTitle);

        mClipFrame.setWidth(Director.instance().getViewWidth() * 0.4f);
        mClipFrame.setHeight(mOptionTable.getPrefHeight());


        mClipFrame.addActor(mMainTable);
        mClipFrame.addActor(mOptionTable);
        mMainTable.setFillParent(true);
        mOptionTable.setFillParent(true);

        mMainTable.setPosition(0, 0);
        mOptionTable.setPosition(mClipFrame.getWidth(), 0);

        addActor(mClipFrame);

        float x = mTitle.getX() + (mTitle.getWidth() - mClipFrame.getWidth()) /2.0f;
        float y = mTitle.getY() - mClipFrame.getHeight();
        mClipFrame.setPosition(x, y);


        Timeline alpha = Timeline.createParallel();
        alpha.push(Tween.to(mTitle, ActorAccessor.ALPHA, titleFadeTime).target(1.0f).ease(TweenEquations.easeOutQuad));
        alpha.push(Tween.to(mMainTable, ActorAccessor.ALPHA, titleFadeTime).target(1.0f).ease(TweenEquations.easeOutQuad));
        alpha.start(getTweenManager());
    }

    private boolean previousContains(Card[] cards, Card.Suit s, Card.Rank r) {
        for (int j = 0; j<cards.length; ++j) {
            if (cards[j] != null && (cards[j].getSuit() == s || cards[j].getRank() == r)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void enter() {
        super.enter();
        calculateViewportSize();
        getTweenManager().killAll();
        setupScene();
    }

    @Override
    public void exit() {
        super.exit();
        mCardDropAnimation = null;
        mCardFlyAnimation = null;
    }

    private void setupScene() {
        SnapshotArray<Actor> children = new SnapshotArray<>(getRoot().getChildren());
        for (Actor a : children) {
            a.remove();
        }
        for (CardView c : cards) {
            addActor(c);
        }
        hideCards();
        cards[0].setOrigin(Align.bottom | Align.center);
        cards[1].setOrigin(Align.bottom | Align.center);
        cards[2].setOrigin(Align.bottom | Align.center);
        mState = IntroState.START;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        switch (mState) {
            case BUILD:
                if (mCardFlyAnimation == null)
                    mCardFlyAnimation = createFlyAnimations();
                if (mCardDropAnimation == null)
                    mCardDropAnimation = createDropAnimation();
                mState = IntroState.START;
                break;
            case START:
                if (mCardFlyAnimation == null || mCardDropAnimation == null) {
                    mState = IntroState.BUILD;
                    break;
                }
                if (mShownAnimation)
                    mState = IntroState.DROP;
                else
                    mState = IntroState.FLY;
                break;
            case FLY: //play the fly animation
                hideCards();
                mCardFlyAnimation.start(getTweenManager());
                mState = IntroState.PAUSE;
                break;
            case DROP:
                hideCards();
                mCardDropAnimation.start(getTweenManager());
                mState = IntroState.PAUSE;
                break;
            case TITLE:
                showTitle();
                mState = IntroState.DONE;
                break;
            default:
            case DONE:
            case PAUSE:
                break;
        }
    }

    private Timeline createFlyAnimations() {
        final float duration = 1.00f;
        final float gap = 0.5f;
        final float firstDelay = 0.5f;
        final float secondDelay = 0.5f;
        final float thirdDelay = 0.5f;
        Vector3 pos1, pos2, pos3, initPos;

        Timeline res = Timeline.createSequence();
        //place the cards
        res.beginParallel();
            initPos = new Vector3(-cards[0].getWidth(), ( (mViewHeight-cards[0].getHeight()/2f)/2.0f), 0);
            res.push(Tween.set(cards[0], ActorAccessor.POSITION_XY).target(initPos.x, initPos.y));
            initPos = new Vector3((mViewWidth-cards[0].getWidth()/2f)/2.0f, mViewHeight + cards[0].getHeight(), 0);
            res.push(Tween.set(cards[1], ActorAccessor.POSITION_XY).target(initPos.x, initPos.y));
            initPos = new Vector3(mViewWidth, ( (mViewHeight-cards[0].getHeight()/2f)/2.0f), 0);
            res.push(Tween.set(cards[2], ActorAccessor.POSITION_XY).target(initPos.x, initPos.y));
        res.end();
        res.pushPause(initialAnimationDelayTime);
        res.push(Tween.call(new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                showCards();
            }
        }));
        //left to right
        res.beginSequence();
            res.pushPause(firstDelay);
            res.beginParallel();
                pos1 = new Vector3(mViewWidth, ( (mViewHeight-cards[0].getHeight())/2.0f), 0);
                res.push(Tween.to(cards[0], ActorAccessor.POSITION_XY, duration).target(pos1.x, pos1.y));
                res.push(Tween.to(cards[0], ActorAccessor.ROTATION, duration).target(360));
            res.end();
        res.end();

        //bottom to top
        res.beginSequence();
            res.pushPause(secondDelay);
            res.beginParallel();
                pos2 = new Vector3 ((mViewWidth-cards[0].getWidth())/2.0f, -cards[0].getHeight(), 0);
                res.push(Tween.to(cards[1], ActorAccessor.POSITION_XY,duration).target(pos2.x, pos2.y));
                res.push(Tween.to(cards[1], ActorAccessor.ROTATION, duration).target(360));
            res.end();
        res.end();


        //right to left
        res.beginSequence();
            res.pushPause(thirdDelay);
            res.beginParallel();
                pos3 = new Vector3(-cards[0].getWidth(), ( (mViewHeight-cards[0].getHeight())/2.0f), 0);
                res.push(Tween.to(cards[2], ActorAccessor.POSITION_XY, duration).target(pos3.x, pos3.y));
                res.push(Tween.to(cards[2], ActorAccessor.ROTATION, duration).target(360));
            res.end();
        res.end();
        res.pushPause(gap);
        res.setCallbackTriggers(TweenCallback.END);
        res.setCallback(mCallback);
        return res;
    }

    private void showCards() {
        cards[0].setVisible(true);
        cards[1].setVisible(true);
        cards[2].setVisible(true);
    }

    private void hideCards() {
        cards[0].setVisible(false);
        cards[1].setVisible(false);
        cards[2].setVisible(false);
    }

    private Timeline createDropAnimation() {
        float w = cards[0].getWidth();
        final float duration = 1.0f;
        float topEnd = mTitle.getY() + mTitle.getHeight() /2.0f;
        Vector3 startCardPos[] = new Vector3[3];
        Vector3 finalPosition;

        float angle1 = 55;
        float angle2 = 30;
        float angle3 = 10;

        startCardPos[0] = getCamera().unproject(new Vector3(mTitle.getX()-(w*.1f), -cards[0].getHeight()*2, 0));

        Timeline res = Timeline.createSequence();
        res.beginParallel();
            res.push(Tween.set(cards[0], ActorAccessor.POSITION_XY).target(startCardPos[0].x, startCardPos[0].y));
            res.push(Tween.set(cards[0], ActorAccessor.ROTATION).target(angle1));
            startCardPos[1] = new Vector3(startCardPos[0].x+w*-MathUtils.sin(angle1-angle2), startCardPos[0].y-(w*MathUtils.cos(angle1-angle2)), 0);
            res.push(Tween.set(cards[1], ActorAccessor.POSITION_XY).target(startCardPos[1].x, startCardPos[1].y));
            res.push(Tween.set(cards[1], ActorAccessor.ROTATION).target(angle2));
            startCardPos[2] = new Vector3(startCardPos[1].x+ w*-MathUtils.sin(angle1-angle2), startCardPos[1].y-(w*MathUtils.cos(angle1-angle2)), 0);
            res.push(Tween.set(cards[2], ActorAccessor.POSITION_XY).target(startCardPos[2].x, startCardPos[2].y));
            res.push(Tween.set(cards[2], ActorAccessor.ROTATION).target(angle3));
        res.end();

        res.push(Tween.call(new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                showCards();
            }
        }));

        res.beginParallel();
            finalPosition = getCamera().unproject(new Vector3(startCardPos[0].x, topEnd, 0));
            finalPosition.y = topEnd;
            res.push(Tween.to(cards[0], ActorAccessor.POSITION_XY, duration).target(startCardPos[0].x, finalPosition.y));
            res.push(Tween.to(cards[1], ActorAccessor.POSITION_XY, duration).target(startCardPos[1].x, finalPosition.y));
            res.push(Tween.to(cards[2], ActorAccessor.POSITION_XY, duration).target(startCardPos[2].x, finalPosition.y));
        res.end();
        res.setCallbackTriggers(TweenCallback.END);
        res.setCallback(mCallback);
        return res;

    }

    @Override
    public void dispose() {
        super.dispose();
        for (int i=0; i<cards.length; ++i) {
            cards[i] = null;
        }
        SnapshotArray<Actor> children = new SnapshotArray<>(getRoot().getChildren());
        for (Actor a : children) {
            a.remove();
        }
        mShadow = null;
        mCallback = null;
        mCardFlyAnimation = null;
        mCardDropAnimation = null;
        mTitle = null;
        mMainTable.reset();
        mMainTable = null;
        mOptionTable.reset();
        mOptionTable = null;
    }
}
