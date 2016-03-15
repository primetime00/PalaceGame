package com.kegelapps.palace.scenes;

import aurelienribon.tweenengine.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.graphics.CardView;
import com.kegelapps.palace.graphics.TextView;
import com.kegelapps.palace.graphics.ui.MainTable;
import com.kegelapps.palace.tween.ActorAccessor;

import javax.naming.TimeLimitExceededException;

/**
 * Created by Ryan on 3/14/2016.
 */
public class IntroScene extends Scene {

    private CardView cards[];
    private TweenCallback mCallback;
    private Timeline mDropAnimation, mFlyAnimation;
    private TextView mTitle;
    private MainTable mOptionTable;


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
            addActor(cards[i]);
            i++;
        }
        mBackgroundColor = new Color(0.2f, 0.2f, 0.2f, 1);
        mCallback = new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                if (type == TweenCallback.END) {
                    if (source == mFlyAnimation)
                        startDropAnimation();
                    else if (source == mDropAnimation)
                        showTitle();
                }
            }
        };

        mTitle = new TextView(Director.instance().getAssets().get("title_font_large", BitmapFont.class));
        mTitle.setText("Palace");
        mTitle.setColor(Color.RED.r, Color.RED.g, Color.RED.b, 0.0f);
        Vector3 pos = getCamera().unproject(new Vector3( (getCamera().viewportWidth-mTitle.getWidth())/2.0f, (getCamera().viewportHeight-mTitle.getHeight())/3.0f, 0));
        mTitle.setPosition(pos.x, pos.y);

        mOptionTable = new MainTable();
        mOptionTable.setColor(Color.RED.r, Color.RED.g, Color.RED.b, 0.0f);

    }

    private void showTitle() {
        addActor(mTitle);
        addActor(mOptionTable);

        float x = mTitle.getX() + (mTitle.getWidth() - mOptionTable.getWidth()) /2.0f;
        float y = mTitle.getY() - mTitle.getHeight()*2;
        mOptionTable.setPosition(x, y);
        mOptionTable.align(Align.center);
        mOptionTable.debugAll();


        Timeline alpha = Timeline.createParallel();
        alpha.push(Tween.to(mTitle, ActorAccessor.ALPHA, 0.75f).target(1.0f).ease(TweenEquations.easeOutQuad));
        alpha.push(Tween.to(mOptionTable, ActorAccessor.ALPHA, 0.75f).target(1.0f).ease(TweenEquations.easeOutQuad));
        alpha.start(getTweenManager());
    }

    private void placeCards() {
        Vector3 pos;
        //left
        pos = getCamera().unproject(new Vector3(-cards[0].getWidth(), ( (getCamera().viewportHeight+cards[0].getHeight())/2.0f), 0));
        cards[0].setPosition(pos.x, pos.y);
        //bottom
        pos = getCamera().unproject(new Vector3 ((getCamera().viewportWidth-cards[0].getWidth())/2.0f, getCamera().viewportHeight + cards[0].getHeight(), 0));
        cards[1].setPosition(pos.x, pos.y);
        //right
        pos = getCamera().unproject(new Vector3(getCamera().viewportWidth, ( (getCamera().viewportHeight+cards[0].getHeight())/2.0f), 0));
        cards[2].setPosition(pos.x, pos.y);
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
        placeCards();
        //createAnimations();
        startDropAnimation();
        //showTitle();
    }

    private void createAnimations() {
        final float duration = 1.00f;
        final float gap = 0.5f;
        final float firstDelay = 0.75f;
        final float secondDelay = firstDelay + 0.25f + duration;
        final float thirdDelay = secondDelay + 0.25f + duration;
        Vector3 pos1, pos2, pos3;
        //left to right
        Timeline c1 = Timeline.createSequence();
        c1.pushPause(firstDelay);
        c1.beginParallel();
        pos1 = getCamera().unproject(new Vector3(getCamera().viewportWidth, ( (getCamera().viewportHeight+cards[0].getHeight())/2.0f), 0));
        c1.push(Tween.to(cards[0], ActorAccessor.POSITION_XY, duration).target(pos1.x, pos1.y).ease(TweenEquations.easeOutQuad));
        c1.push(Tween.to(cards[0], ActorAccessor.ROTATION, duration).target(360)).end();
        c1.start(getTweenManager());

        //bottom to top
        Timeline c2 = Timeline.createSequence();
        c2.pushPause(secondDelay);
        c2.beginParallel();
        pos2 = getCamera().unproject(new Vector3 ((getCamera().viewportWidth-cards[0].getWidth())/2.0f, -cards[0].getHeight(), 0));
        c2.push(Tween.to(cards[1], ActorAccessor.POSITION_XY,duration).target(pos2.x, pos2.y).ease(TweenEquations.easeOutQuad));
        c2.push(Tween.to(cards[1], ActorAccessor.ROTATION, duration).target(360)).end();
        c2.start(getTweenManager());


        //right to left
        Timeline c3 = Timeline.createSequence();
        c3.pushPause(thirdDelay);
        c3.beginParallel();
        pos3 = getCamera().unproject(new Vector3(-cards[0].getWidth(), ( (getCamera().viewportHeight+cards[0].getHeight())/2.0f), 0));
        c3.push(Tween.to(cards[2], ActorAccessor.POSITION_XY, duration).target(pos3.x, pos3.y).ease(TweenEquations.easeOutQuad));
        c3.push(Tween.to(cards[2], ActorAccessor.ROTATION, duration).target(360)).end();
        c3.pushPause(gap);
        c3.setCallbackTriggers(TweenCallback.END);
        c3.setCallback(mCallback);
        mFlyAnimation = c3;
        c3.start(getTweenManager());


    }

    private void startDropAnimation() {
        Vector3 pos;
        float w = cards[0].getWidth();
        float h = cards[0].getHeight();
        final float gap = cards[0].getWidth() * -0.5f;
        final float duration = 1.0f;
        float topEnd = mTitle.getY() - mTitle.getHeight() /2.0f;
        // mTitle.getY() + mTitle.getHeight();
        //cards[0].getHeight() * 2;

        float angle1 = 55;
        float angle2 = 30;
        float angle3 = 10;

        pos = getCamera().unproject(new Vector3(mTitle.getX()-(w*.1f), -cards[0].getHeight()*2, 0));

        cards[0].setPosition(pos.x, pos.y);
        cards[1].setPosition((float) (cards[0].getX()+ w*-Math.sin(angle1-angle2)), (float) (cards[0].getY()-(w*Math.cos(angle1-angle2))));
        cards[2].setPosition((float) (cards[1].getX()+ w*-Math.sin(angle1-angle2)), (float) (cards[1].getY()-(w*Math.cos(angle1-angle2))));


        cards[0].setOrigin(Align.bottom | Align.center);
        cards[1].setOrigin(Align.bottom | Align.center);
        cards[2].setOrigin(Align.bottom | Align.center);

        cards[0].setRotation(angle1);
        cards[1].setRotation(angle2);
        cards[2].setRotation(angle3);

        mDropAnimation = Timeline.createSequence();
        mDropAnimation.beginParallel();
        pos = getCamera().unproject(new Vector3(pos.x, topEnd, 0));
        pos.y = topEnd;
        mDropAnimation.push(Tween.to(cards[0], ActorAccessor.POSITION_XY, duration).target(cards[0].getX(), pos.y));
        mDropAnimation.push(Tween.to(cards[1], ActorAccessor.POSITION_XY, duration).target(cards[1].getX(), pos.y));
        mDropAnimation.push(Tween.to(cards[2], ActorAccessor.POSITION_XY, duration).target(cards[2].getX(), pos.y));
        mDropAnimation.setCallbackTriggers(TweenCallback.END);
        mDropAnimation.setCallback(mCallback);
        mDropAnimation.start(getTweenManager());

    }


    @Override
    public void dispose() {
        super.dispose();
        for (int i=0; i<cards.length; ++i) {
            cards[i] = null;
        }
    }
}
