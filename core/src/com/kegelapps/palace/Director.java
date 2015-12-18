package com.kegelapps.palace;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * Created by keg45397 on 12/15/2015.
 */
public class Director {
    private static Director instance = null;
    private Scene scene;

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

        // Latch onto event source.
        //eventSource = ActorEventSource.instance();

        // These are scale factors for adjusting touch events to the actual size
        // of the view-port.
        //scaleFactorX = 1;
        //scaleFactorY = 1;
    }

    public void update()
    {
        // Update events.
        //eventSource.update();

        // Update View
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
}
