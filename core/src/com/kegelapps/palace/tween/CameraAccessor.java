package com.kegelapps.palace.tween;

import aurelienribon.tweenengine.TweenAccessor;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Created by Ryan on 12/21/2015.
 */
public class CameraAccessor implements TweenAccessor<OrthographicCamera> {

    public static final int POSITION_X = 1;
    public static final int POSITION_Y = 2;
    public static final int POSITION_XY = 3;
    public static final int ZOOM = 4;

    @Override
    public int getValues(OrthographicCamera target, int tweenType, float[] returnValues) {
        switch (tweenType) {
            case POSITION_X: returnValues[0] = target.position.x; return 1;
            case POSITION_Y: returnValues[0] = target.position.y; return 1;
            case POSITION_XY:
                returnValues[0] = target.position.x;
                returnValues[1] = target.position.y;
                return 2;
            case ZOOM:
                returnValues[0] = target.zoom;
                return 1;
            default: assert false; return -1;
        }
    }

    @Override
    public void setValues(OrthographicCamera target, int tweenType, float[] newValues) {
        switch (tweenType) {
            case POSITION_X: target.position.x = newValues[0]; break;
            case POSITION_Y: target.position.y = newValues[0]; break;
            case POSITION_XY:
                target.position.x = newValues[0];
                target.position.y = newValues[1];
                break;
            case ZOOM:
                target.zoom = newValues[0];
                break;
            default: assert false; break;
        }
    }
}
