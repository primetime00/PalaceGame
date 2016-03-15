package com.kegelapps.palace.tween;

import aurelienribon.tweenengine.TweenAccessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kegelapps.palace.graphics.CardView;

/**
 * Created by Ryan on 12/21/2015.
 */
public class ActorAccessor implements TweenAccessor<Actor>{

    public static final int POSITION_X = 1;
    public static final int POSITION_Y = 2;
    public static final int POSITION_XY = 3;
    public static final int ROTATION = 4;
    public static final int ALPHA = 5;

    @Override
    public int getValues(Actor target, int tweenType, float[] returnValues) {
        switch (tweenType) {
            case POSITION_X: returnValues[0] = target.getX(); return 1;
            case POSITION_Y: returnValues[0] = target.getY(); return 1;
            case POSITION_XY:
                returnValues[0] = target.getX();
                returnValues[1] = target.getY();
                return 2;
            case ROTATION:
                returnValues[0] = target.getRotation();
                return 1;
            case ALPHA:
                Color c = target.getColor();
                returnValues[0] = c.a;
                return 1;
            default: assert false; return -1;
        }
    }

    @Override
    public void setValues(Actor target, int tweenType, float[] newValues) {
        switch (tweenType) {
            case POSITION_X: target.setX(newValues[0]); break;
            case POSITION_Y: target.setY(newValues[0]); break;
            case POSITION_XY:
                target.setX(newValues[0]);
                target.setY(newValues[1]);
                break;
            case ROTATION:
                target.setRotation(newValues[0]);
                break;
            case ALPHA:
                Color c = target.getColor();
                target.setColor(c.r, c.g, c.b, newValues[0]);
            default: assert false; break;
        }
    }
}
