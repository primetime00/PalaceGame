package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

/**
 * Created by keg45397 on 3/15/2016.
 */
public class ClipView extends Group {
    public ClipView() {
    }

    @Override
    public void addActor(Actor actor) {
        super.addActor(actor);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Rectangle scissors = new Rectangle();
        Rectangle clip = new Rectangle(getX(), getY(), getWidth(), getHeight());
        ScissorStack.calculateScissors(getStage().getCamera(), batch.getTransformMatrix(), clip, scissors);
        batch.flush();
        ScissorStack.pushScissors(scissors);
        super.draw(batch, parentAlpha);
        batch.flush();
        ScissorStack.popScissors();
    }
}
