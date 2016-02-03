package com.kegelapps.palace;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kegelapps.palace.graphics.CardCamera;

/**
 * Created by keg45397 on 12/15/2015.
 */
public class Scene extends Stage {

    private InputMultiplexer inputMultiplexer;

    public Scene() {
        super();
        this.inputMultiplexer = new InputMultiplexer(this);
    }

    public Scene(Viewport viewport) {
        super(viewport);
        viewport.setCamera(new CardCamera(viewport.getScreenWidth(), viewport.getScreenHeight()));
        viewport.apply(true);
        this.inputMultiplexer = new InputMultiplexer(this);
    }

    public CardCamera getCardCamera() {
        return (CardCamera) getCamera();
    }

    public void exit() {

    }

    public void enter() {

    }

    public InputMultiplexer getInputMultiplexer() {
        return inputMultiplexer;
    }

}
