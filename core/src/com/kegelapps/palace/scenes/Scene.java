package com.kegelapps.palace.scenes;

import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kegelapps.palace.Resettable;
import com.kegelapps.palace.graphics.CardCamera;

/**
 * Created by keg45397 on 12/15/2015.
 */
public class Scene extends Stage implements Resettable {

    private InputMultiplexer inputMultiplexer;
    protected TweenManager mTweenManager;

    public Scene() {
        super();
        this.inputMultiplexer = new InputMultiplexer(this);
        mTweenManager = new TweenManager();
    }

    public Scene(Viewport viewport) {
        super(viewport);
        viewport.setCamera(new CardCamera(viewport.getScreenWidth(), viewport.getScreenHeight()));
        viewport.apply(true);
        this.inputMultiplexer = new InputMultiplexer(this);
        mTweenManager = new TweenManager();
    }


    public CardCamera getCardCamera() {
        return (CardCamera) getCamera();
    }

    public void exit() {

    }

    public void enter() {

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
    public void Reset() {
        clearScene();
        mTweenManager.killAll();
    }
}