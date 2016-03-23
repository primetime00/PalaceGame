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
import com.kegelapps.palace.audio.SoundIDList;
import com.kegelapps.palace.audio.AudioManager;
import com.kegelapps.palace.audio.VolumeController;
import com.kegelapps.palace.loaders.types.MusicMap;
import com.kegelapps.palace.loaders.types.SoundMap;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.HighlightView;
import com.kegelapps.palace.graphics.MessageBandView;
import com.kegelapps.palace.graphics.ShadowView;
import com.kegelapps.palace.loaders.*;
import com.kegelapps.palace.loaders.types.PlayerMap;
import com.kegelapps.palace.loaders.types.StringStringMap;
import com.kegelapps.palace.protos.OptionProtos;
import com.kegelapps.palace.scenes.GameScene;
import com.kegelapps.palace.scenes.IntroScene;
import com.kegelapps.palace.scenes.Scene;
import com.kegelapps.palace.scenes.UIScene;
import com.kegelapps.palace.tween.AudioAccessor;
import com.kegelapps.palace.tween.CameraAccessor;
import com.kegelapps.palace.tween.ActorAccessor;
import com.kegelapps.palace.tween.HighlightAccessor;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by keg45397 on 12/15/2015.
 */
public class Director implements Disposable{
    private static Director instance = null;

    private GameScene mGameScene;
    private UIScene mUIScene;
    private IntroScene mIntroScene;
    private OptionProtos.Options mOptions;

    private AudioManager mAudioManager;

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

        mAudioManager = new AudioManager();
    }

    private void createScenes() {
        mGameScene = new GameScene(new ExtendViewport(800,480));
        mUIScene = new UIScene(new ExtendViewport(800,480));
        mIntroScene = new IntroScene(new ExtendViewport(800,480));
    }

    private void registerTweens() {
        //create a camera tween
        Tween.registerAccessor(OrthographicCamera.class, new CameraAccessor());
        //create card tweens
        Tween.registerAccessor(Actor.class, new ActorAccessor());
        //create highlight tweens
        Tween.registerAccessor(HighlightView.class, new HighlightAccessor());

        //create audio tweens
        Tween.registerAccessor(VolumeController.class, new AudioAccessor());

    }

    public TweenManager getTweenManager() {
        return mCurrentScene.getTweenManager();
    }
    public EventSystem getEventSystem() { return mEventSystem;}

    public void update()
    {
        if (mAssetManager == null) {
            loadOptions();
            loadAssets();
        }
        if (mGameScene == null) {
            createScenes();
            setScene(mGameScene);
            //setScene(mIntroScene);
        }
        // Update View
        //Gdx.gl.glClearColor(0, 0, 0, 1);
        Color c = mCurrentScene.getBackgroundColor();
        if (c != null)
            Gdx.gl.glClearColor(c.r, c.g, c.b, c.a);
        else
            Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mEventSystem.ProcessWaitingEvents();
        mAudioManager.update(Gdx.graphics.getDeltaTime());
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

    private void loadOptions() {
        try {
            mOptions = OptionProtos.Options.parseFrom(new FileInputStream(Gdx.files.getLocalStoragePath()+"/options.dat"));
        } catch (IOException e) {
            System.out.print("Could not find / parse options.dat file\n");
            mOptions = OptionProtos.Options.getDefaultInstance();
        }
    }

    public void saveOptions() {
        try {
            mOptions.writeTo(new FileOutputStream(Gdx.files.getLocalStoragePath()+"/options.dat", false));
        } catch (IOException e) {
            e.printStackTrace();
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

    public AudioManager getAudioManager() {
        return mAudioManager;
    }

    public AssetManager getAssets() {
        return mAssetManager;
    }

    public void loadAssets() {
        mAssetManager = new AssetManager();

        //load strings
        mAssetManager.setLoader(StringStringMap.class, new StringXmlLoader(new InternalFileHandleResolver()));
        mAssetManager.load("strings.xml", StringStringMap.class);

        //load players
        mAssetManager.setLoader(PlayerMap.class, new PlayerLoader(new InternalFileHandleResolver()));
        mAssetManager.load("players", PlayerMap.class);


        //lets load out font first
        mAssetManager.setLoader(BitmapFont.class, new FontLoader(new InternalFileHandleResolver()));
        FontLoader.FontParams fontParam = new FontLoader.FontParams();
        fontParam.filename = "Actor-Regular.ttf";
        fontParam.size = 45;
        fontParam.border = 2;
        mAssetManager.load("default_font", BitmapFont.class, fontParam);

        fontParam = new FontLoader.FontParams();
        fontParam.filename = "Actor-Regular.ttf";
        fontParam.size = 55;
        fontParam.border = 3;
        mAssetManager.load("message_font", BitmapFont.class, fontParam);


        fontParam = new FontLoader.FontParams();
        fontParam.size = 175;
        fontParam.border = 5;
        fontParam.filename = "title_font.ttf";
        mAssetManager.load("title_font_large", BitmapFont.class, fontParam);

        fontParam = new FontLoader.FontParams();
        fontParam.size = 45;
        fontParam.border = 1;
        fontParam.filename = "title_font.ttf";
        mAssetManager.load("title_font_small", BitmapFont.class, fontParam);


        mAssetManager.setLoader(CardResource.class, new CardLoader(new InternalFileHandleResolver()));
        mAssetManager.load("cards_tiny.pack", CardResource.class);

        mAssetManager.setLoader(CoinResource.class, new CoinLoader(new InternalFileHandleResolver()));
        mAssetManager.load("coins.pack", CoinResource.class);

        mAssetManager.load("ui.pack", TextureAtlas.class);

        mAssetManager.setLoader(ShadowView.ShadowTexture.class, new ShadowLoader(new InternalFileHandleResolver()));
        mAssetManager.load("shadow", ShadowView.ShadowTexture.class);

        mAssetManager.setLoader(MessageBandView.MessageBandTexture.class, new MessageBandLoader(new InternalFileHandleResolver()));
        mAssetManager.load("messageband", MessageBandView.MessageBandTexture.class);

        mAssetManager.setLoader(SoundMap.class, new SoundLoader(new InternalFileHandleResolver()));
        mAssetManager.load("sounds", SoundMap.class);

        mAssetManager.setLoader(MusicMap.class, new MusicLoader(new InternalFileHandleResolver()));
        mAssetManager.load("music", MusicMap.class);



        mAssetManager.finishLoading();

    }

    public void restart() {
        setScene(mGameScene);
    }

    private void createEvents() {

        getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.OPTIONS) {
            @Override
            public void handle(Object params[]) {
                getAudioManager().FadeOutSound(new Runnable() {
                    @Override
                    public void run() {
                        getAudioManager().Reset();
                        getAudioManager().SetMasterVolume(1.0f);
                    }
                });
                setScene(mUIScene);
            }
        });

        getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.GAME_OVER) {
            @Override
            public void handle(Object params[]) {
                setScene(mUIScene);
            }
        });

        getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.RESUME_GAME) {
            @Override
            public void handle(Object params[]) {
                getAudioManager().FadeInSound(null);
                setScene(mGameScene);
            }
        });

        getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.QUIT_GAME) {
            @Override
            public void handle(Object params[]) {
                setScene(mGameScene);
            }
        });

        getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.MAIN_SCREEN) {
            @Override
            public void handle(Object params[]) {
                setScene(mIntroScene);
            }
        });


        getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.RESTART_GAME) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 1 || !(params[0] instanceof Boolean)) {
                    throw new IllegalArgumentException("Invalid parameters for RESTART_GAME");
                }
                boolean newGame = (boolean) params[0];
                for (Resettable r : mResetList) {
                    r.Reset(newGame);
                }
                getAudioManager().SetMasterVolume(1.0f);
                getAudioManager().Reset();
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

    public OptionProtos.Options getOptions() {
        if (mOptions == null)
            mOptions = OptionProtos.Options.getDefaultInstance();
        return mOptions;
    }

    public void setOptions(OptionProtos.Options.Builder val) {
        mOptions = val.build();
        saveOptions();
    }

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
