package com.kegelapps.palace.scenes;

import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kegelapps.palace.utilities.Resettable;
import com.kegelapps.palace.graphics.CardCamera;
import com.kegelapps.palace.graphics.ui.UIDebug;
import com.kegelapps.palace.protos.OptionProtos;

/**
 * Created by keg45397 on 12/15/2015.
 */
public class Scene extends Stage implements Resettable {

    private InputMultiplexer inputMultiplexer;
    protected TweenManager mTweenManager;
    protected Color mBackgroundColor;

    protected int mScreenWidth, mScreenHeight;
    protected float mViewWidth, mViewHeight;

    private boolean mFirstRun;

    public Scene() {
        super();
        this.inputMultiplexer = new InputMultiplexer(this);
        mTweenManager = new TweenManager();
        mBackgroundColor = Color.BLACK;
        getBatch().setColor(1,1,1,1);
        mScreenHeight = Gdx.graphics.getHeight();
        mScreenWidth = Gdx.graphics.getWidth();
        calculateViewportSize();
        mFirstRun = false;
    }

    public Scene(Viewport viewport) {
        super(viewport);
        viewport.setCamera(new CardCamera(viewport.getScreenWidth(), viewport.getScreenHeight()));
        viewport.apply(true);
        this.inputMultiplexer = new InputMultiplexer(this);
        mTweenManager = new TweenManager();
        mBackgroundColor = Color.BLACK;
        mScreenHeight = Gdx.graphics.getHeight();
        mScreenWidth = Gdx.graphics.getWidth();
        calculateViewportSize();
        mFirstRun = false;
    }

    public void calculateViewportSize() {
        mViewHeight = getCardCamera().viewportHeight;
        mViewWidth = getCardCamera().viewportWidth;
    }


    @Override
    public void act(float delta) {
        super.act(delta);
        mTweenManager.update(delta);
        UIDebug.get().update(delta);
    }

    protected void initFirstRun() {

    }

    public CardCamera getCardCamera() {
        return (CardCamera) getCamera();
    }

    public void exit() {

    }

    public void enter() {
        if (!mFirstRun) {
            mFirstRun = true;
            initFirstRun();
        }
    }

    public Color getBackgroundColor() {
        return mBackgroundColor;
    }

    protected void clearScene() {
        SnapshotArray<Actor> actors = new SnapshotArray<Actor>(getActors());
        for(Actor actor : actors) {
            actor.remove();
        }
    }


    public InputMultiplexer getInputMultiplexer() {
        return inputMultiplexer;
    }

    public TweenManager getTweenManager() {
        return mTweenManager;
    }

    @Override
    public void dispose() {
        super.dispose();
        mTweenManager.killAll();
        mTweenManager = null;
        inputMultiplexer = null;
    }

    @Override
    public void Reset(boolean newGame) {
        clearScene();
        mTweenManager.killAll();
    }

    public void OptionChanged(OptionProtos.Options option) {

    }

    public float getViewWidth() {
        return mViewWidth;
    }

    public float getViewHeight() {
        return mViewHeight;
    }

}
