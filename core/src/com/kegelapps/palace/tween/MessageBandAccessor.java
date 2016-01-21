package com.kegelapps.palace.tween;

import aurelienribon.tweenengine.TweenAccessor;
import com.kegelapps.palace.graphics.MessageBandView;

/**
 * Created by Ryan on 1/20/2016.
 */
public class MessageBandAccessor implements TweenAccessor<MessageBandView> {
    public static final int ALPHA = 1;
    public static final int POS_X = 2;
    public static final int POS_Y = 3;
    public static final int POS_XY = 4;

    @Override
    public int getValues(MessageBandView target, int tweenType, float[] returnValues) {
        switch (tweenType) {
            case ALPHA:
                returnValues[0] = target.getAlpha();
                return 1;
            case POS_X:
                returnValues[0] = target.getWindowX();
                return 1;
            case POS_Y:
                returnValues[0] = target.getWindowY();
                return 1;
            case POS_XY:
                returnValues[0] = target.getWindowX();
                returnValues[1] = target.getWindowY();
                return 2;
            default:
                assert false;
                return -1;
        }
    }

    @Override
    public void setValues(MessageBandView target, int tweenType, float[] newValues) {
        switch (tweenType) {
            case ALPHA:
                target.setAlpha(newValues[0]);
                break;
            case POS_X:
                target.setWindowX(newValues[0]);
                break;
            case POS_Y:
                target.setWindowY(newValues[0]);
                break;
            case POS_XY:
                target.setWindowX(newValues[0]);
                target.setWindowY(newValues[1]);
                break;
            default:
                assert false;
                break;
        }
    }
}
