package com.kegelapps.palace.graphics.utils;

import com.badlogic.gdx.math.Vector2;
import com.kegelapps.palace.graphics.InPlayView;

/**
 * Created by keg45397 on 2/4/2016.
 */
public class InPlayUtils {

    static public Vector2 getCenterCameraPoint(InPlayView view) {
        Vector2 v = new Vector2();
        if (view == null)
            throw new RuntimeException("Must have a valid InPlayView for find the center.");
        float x = view.getX() + (view.getWidth() /2.0f);
        float y = view.getY() + (view.getHeight() /2.0f);
        v.set(x,y);
        return v;
    }
}
