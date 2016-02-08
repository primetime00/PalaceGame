package com.kegelapps.palace;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.CardView;
import com.kegelapps.palace.graphics.HighlightView;
import com.kegelapps.palace.graphics.MessageBandView;
import com.kegelapps.palace.tween.CameraAccessor;
import com.kegelapps.palace.tween.CardAccessor;
import com.kegelapps.palace.tween.HighlightAccessor;
import com.kegelapps.palace.tween.MessageBandAccessor;

/**
 * Created by keg45397 on 12/15/2015.
 */
public class Director {
    private static Director instance = null;
    private Scene scene;
    private TweenManager mManager;
    private EventSystem mEventSystem;
    private BitmapFont mGameFont;

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
        scene = null;

        mEventSystem = new EventSystem();

        //create a camera tween
        Tween.registerAccessor(OrthographicCamera.class, new CameraAccessor());
        //create card tweens
        Tween.registerAccessor(CardView.class, new CardAccessor());
        //create highlight tweens
        Tween.registerAccessor(HighlightView.class, new HighlightAccessor());
        //create message band tweens
        Tween.registerAccessor(MessageBandView.class, new MessageBandAccessor());

        mManager = new TweenManager();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(new FileHandle("FatCow.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 45;
        mGameFont = generator.generateFont(param);
        generator.dispose();

        // Latch onto event source.
        //eventSource = ActorEventSource.instance();

        // These are scale factors for adjusting touch events to the actual size
        // of the view-port.
        //scaleFactorX = 1;
        //scaleFactorY = 1;
    }

    public TweenManager getTweenManager() {
        return mManager;
    }
    public EventSystem getEventSystem() { return mEventSystem;}

    public void update()
    {
        // Update View
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mManager.update(Gdx.graphics.getDeltaTime());
        mEventSystem.ProcessWaitingEvents();
        if (scene != null)
        {
            scene.act(Gdx.graphics.getDeltaTime());

            scene.draw();
        }
        else
        {
            Gdx.app.log("WTF!", "No scene");
        }
    }

    public synchronized void setScene(Scene scene)
    {
        // If already active scene...
        if (this.scene != null)
        {
            // Exit stage left..
            this.scene.exit();
        }

        this.scene = scene;

        if (this.scene != null)
        {
            // Enter stage right..
            this.scene.enter();

            // NOTE: Route input events to the scene.
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
        return scene;
    }

    public BitmapFont getGameFont() { return mGameFont;}
}
