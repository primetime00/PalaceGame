package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kegelapps.palace.Director;

/**
 * Created by keg45397 on 3/2/2016.
 */
public class ShadowView extends Actor {

    static public class ShadowTexture extends Texture {

        public ShadowTexture(Pixmap pixmap) {
            super(pixmap);
        }
    }

    ShadowTexture mShadow;
    public ShadowView() {
        super();
        mShadow = Director.instance().getAssets().get("shadow");
    }
    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color c = new Color(batch.getColor());
        Color current = new Color(getColor());
        current.a *= c.a;
        batch.setColor(current);
        batch.draw(mShadow, getX(), getY(), getWidth(), getHeight());
        batch.setColor(c);
    }

    public void shadowLeft(float amount, Actor a) {
        setPosition(a.getX() - amount, a.getY());
        setWidth(a.getWidth());
        setHeight(a.getHeight());
    }

    public void shadowRight(float amount, Actor a) {
        setPosition(a.getX(), a.getY());
        setWidth(a.getWidth() + amount);
        setHeight(a.getHeight());
    }

    public void shadowTop(float amount, Actor a) {
        setPosition(a.getX(), a.getY());
        setWidth(a.getWidth());
        setHeight(a.getHeight() + amount);
    }

    public void shadowBottom(float amount, Actor a) {
        setPosition(a.getX(), a.getY() - amount);
        setWidth(a.getWidth());
        setHeight(a.getHeight());
    }

    public void shadowTopLeft(float amount, Actor a) {
        shadowTop(amount, a);
        setX(getX()-amount);
    }

    public void shadowTopRight(float amount, Actor a) {
        shadowTop(amount, a);
        setX(getX()+amount);
    }

    public void shadowBottomLeft(float amount, Actor a) {
        shadowBottom(amount, a);
        setX(getX()-amount);
    }

    public void shadowBottomRight(float amount, Actor a) {
        shadowBottom(amount, a);
        setX(getX()+amount);
    }

    public void shadowEntireScreen(float amount) {
        setPosition(0,0);
        setWidth(Director.instance().getViewWidth());
        setHeight(Director.instance().getViewHeight());
    }

    public void setAlpha(float value) {
        Color c = getColor();
        c.a = value;
        setColor(c);
    }

    public void setColor(Color c, float alpha) {
        Color res = new Color(c);
        res.a = alpha;
        setColor(res);
    }




}
