package com.kegelapps.palace;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.HighlightView;
import com.kegelapps.palace.graphics.MessageBandView;
import com.kegelapps.palace.graphics.ShadowView;
import com.kegelapps.palace.loaders.CardLoader;
import com.kegelapps.palace.loaders.CoinLoader;
import com.kegelapps.palace.loaders.FontLoader;
import com.kegelapps.palace.loaders.ShadowLoader;
import com.kegelapps.palace.tween.CameraAccessor;
import com.kegelapps.palace.tween.ActorAccessor;
import com.kegelapps.palace.tween.HighlightAccessor;
import com.kegelapps.palace.tween.MessageBandAccessor;

/**
 * Created by keg45397 on 12/15/2015.
 */
public class Director implements Disposable{
    private static Director instance = null;
    private Scene mScene;
    private TweenManager mTweenManager;
    private EventSystem mEventSystem;

    private AssetManager mAssetManager;

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
        mScene = null;

        mAssetManager = new AssetManager();

        mEventSystem = new EventSystem();

        //create a camera tween
        Tween.registerAccessor(OrthographicCamera.class, new CameraAccessor());
        //create card tweens
        Tween.registerAccessor(Actor.class, new ActorAccessor());
        //create highlight tweens
        Tween.registerAccessor(HighlightView.class, new HighlightAccessor());
        //create message band tweens
        Tween.registerAccessor(MessageBandView.class, new MessageBandAccessor());

        mTweenManager = new TweenManager();

        loadAssets();

        // Latch onto event source.
        //eventSource = ActorEventSource.instance();

        // These are scale factors for adjusting touch events to the actual size
        // of the view-port.
        //scaleFactorX = 1;
        //scaleFactorY = 1;
    }

    public TweenManager getTweenManager() {
        return mTweenManager;
    }
    public EventSystem getEventSystem() { return mEventSystem;}

    public void update()
    {
        // Update View
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mTweenManager.update(Gdx.graphics.getDeltaTime());
        mEventSystem.ProcessWaitingEvents();
        if (mScene != null)
        {
            mScene.act(Gdx.graphics.getDeltaTime());

            mScene.draw();
        }
        else
        {
            Gdx.app.log("WTF!", "No mScene");
        }
    }

    public synchronized void setScene(Scene scene)
    {
        // If already active mScene...
        if (this.mScene != null)
        {
            // Exit stage left..
            this.mScene.exit();
        }

        this.mScene = scene;

        if (this.mScene != null)
        {
            // Enter stage right..
            this.mScene.enter();

            // NOTE: Route input events to the mScene.
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
        return mScene;
    }

    public AssetManager getAssets() {
        return mAssetManager;
    }

    public void loadAssets() {
        //lets load out font first
        mAssetManager.setLoader(BitmapFont.class, new FontLoader(new InternalFileHandleResolver()));
        mAssetManager.load("FatCow.ttf", BitmapFont.class);

        mAssetManager.setLoader(CardResource.class, new CardLoader(new InternalFileHandleResolver()));
        mAssetManager.load("cards_tiny.pack", CardResource.class);

        mAssetManager.setLoader(CoinResource.class, new CoinLoader(new InternalFileHandleResolver()));
        mAssetManager.load("coins.pack", CoinResource.class);

        mAssetManager.load("ui.pack", TextureAtlas.class);
        //mAssetManager.load("card-board-small.png", Texture.class);

        mAssetManager.setLoader(ShadowView.ShadowTexture.class, new ShadowLoader(new InternalFileHandleResolver()));
        mAssetManager.load("shadow", ShadowView.ShadowTexture.class);


        mAssetManager.finishLoading();

    }

    @Override
    public void dispose() {
        mAssetManager.dispose();
        mEventSystem.dispose();
        mTweenManager.killAll();
        mScene.dispose();

        mAssetManager = null;
        mEventSystem = null;
        mTweenManager = null;
        mScene = null;
        instance = null;
    }
}
