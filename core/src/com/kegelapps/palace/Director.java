package com.kegelapps.palace;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.kegelapps.palace.engine.states.SelectEndCards;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.HighlightView;
import com.kegelapps.palace.graphics.MessageBandView;
import com.kegelapps.palace.graphics.ShadowView;
import com.kegelapps.palace.loaders.*;
import com.kegelapps.palace.scenes.GameScene;
import com.kegelapps.palace.scenes.Scene;
import com.kegelapps.palace.scenes.UIScene;
import com.kegelapps.palace.tween.CameraAccessor;
import com.kegelapps.palace.tween.ActorAccessor;
import com.kegelapps.palace.tween.HighlightAccessor;
import com.kegelapps.palace.tween.MessageBandAccessor;

import java.util.ArrayList;

/**
 * Created by keg45397 on 12/15/2015.
 */
public class Director implements Disposable{
    private static Director instance = null;

    private GameScene mGameScene;
    private UIScene mUIScene;

    private Scene mCurrentScene;
    private EventSystem mEventSystem;

    private AssetManager mAssetManager;

    private ArrayList<Resettable> mResetList;

    public synchronized static Director instance()
    {
        if (instance == null)
        {
            instance = new Director();
        }

        return instance;
    }

    public Director()
    {
        mCurrentScene = null;
        mResetList = new ArrayList<>();

        mEventSystem = new EventSystem();
        registerTweens();
        createEvents();
    }

    private void createScenes() {
        mGameScene = new GameScene(new ExtendViewport(800,480));
        mUIScene = new UIScene(new ExtendViewport(800,480));
    }

    private void registerTweens() {
        //create a camera tween
        Tween.registerAccessor(OrthographicCamera.class, new CameraAccessor());
        //create card tweens
        Tween.registerAccessor(Actor.class, new ActorAccessor());
        //create highlight tweens
        Tween.registerAccessor(HighlightView.class, new HighlightAccessor());
        //create message band tweens
        Tween.registerAccessor(MessageBandView.class, new MessageBandAccessor());
    }

    public TweenManager getTweenManager() {
        return mCurrentScene.getTweenManager();
    }
    public EventSystem getEventSystem() { return mEventSystem;}

    public void update()
    {
        if (mAssetManager == null) {
            loadAssets();
        }
        if (mGameScene == null) {
            createScenes();
            restart();
        }
        // Update View
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mEventSystem.ProcessWaitingEvents();
        if (mCurrentScene != null)
        {
            mCurrentScene.act(Gdx.graphics.getDeltaTime());

            //we want to overlay the UI, so lets still draw the gameScene
            if (mCurrentScene instanceof UIScene && mGameScene != null)
                mGameScene.draw();
            mCurrentScene.draw();
        }
        else
        {
            Gdx.app.log("WTF!", "No mCurrentScene");
        }
    }

    public synchronized void setScene(Scene scene)
    {
        // If already active mCurrentScene...
        if (this.mCurrentScene != null)
        {
            // Exit stage left..
            this.mCurrentScene.exit();
        }

        this.mCurrentScene = scene;

        if (this.mCurrentScene != null)
        {
            // Enter stage right..
            this.mCurrentScene.enter();

            // NOTE: Route input events to the mCurrentScene.
            Gdx.input.setInputProcessor(scene.getInputMultiplexer());
        }
    }

    public void zoom(float factor) {
        OrthographicCamera c = (OrthographicCamera) this.getScene().getCamera();
        c.zoom = factor;
        c.update();
    }

    public int getScreenHeight() {
        return Gdx.graphics.getHeight();
    }
    public int getScreenWidth() {
        return Gdx.graphics.getWidth();
    }

    public int getVirtualWidth() {
        return 1200;
    }
    public int getVirtualHeight() {
        return 1200;
    }

    public Scene getScene() {
        return mCurrentScene;
    }

    public AssetManager getAssets() {
        return mAssetManager;
    }

    public void loadAssets() {
        mAssetManager = new AssetManager();
        //lets load out font first
        mAssetManager.setLoader(BitmapFont.class, new FontLoader(new InternalFileHandleResolver()));
        mAssetManager.load("FatCow.ttf", BitmapFont.class);

        mAssetManager.setLoader(CardResource.class, new CardLoader(new InternalFileHandleResolver()));
        mAssetManager.load("cards_tiny.pack", CardResource.class);

        mAssetManager.setLoader(CoinResource.class, new CoinLoader(new InternalFileHandleResolver()));
        mAssetManager.load("coins.pack", CoinResource.class);

        mAssetManager.load("ui.pack", TextureAtlas.class);

        mAssetManager.setLoader(ShadowView.ShadowTexture.class, new ShadowLoader(new InternalFileHandleResolver()));
        mAssetManager.load("shadow", ShadowView.ShadowTexture.class);

        mAssetManager.setLoader(MessageBandView.MessageBandTexture.class, new MessageBandLoader(new InternalFileHandleResolver()));
        mAssetManager.load("messageband", MessageBandView.MessageBandTexture.class);


        mAssetManager.finishLoading();

    }

    public void restart() {
        setScene(mGameScene);
    }

    private void createEvents() {
        getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.GAME_OVER) {
            @Override
            public void handle(Object params[]) {
                setScene(mUIScene);
            }
        });

        getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.RESTART_GAME) {
            @Override
            public void handle(Object params[]) {
                for (Resettable r : mResetList) {
                    r.Reset();
                }
                setScene(mGameScene);
            }
        });
    }

    public void addResetter(Resettable r) {
        addResetter(r, mResetList.size());
    }

    public void addResetter(Resettable r, int position) {
        if (!mResetList.contains(r))
            mResetList.add(position, r);
    }


    @Override
    public void dispose() {
        mResetList.clear();
        mAssetManager.dispose();
        mEventSystem.dispose();
        mUIScene.dispose();
        mGameScene.dispose();

        mAssetManager = null;
        mEventSystem = null;
        mCurrentScene = null;
        mUIScene = null;
        mGameScene = null;
        mResetList = null;
        instance = null;
    }
}
