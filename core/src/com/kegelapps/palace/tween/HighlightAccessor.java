package com.kegelapps.palace.tween;

import aurelienribon.tweenengine.TweenAccessor;
import com.kegelapps.palace.graphics.HighlightView;

/**
 * Created by keg45397 on 1/15/2016.
 */
public class HighlightAccessor implements TweenAccessor<HighlightView> {
    public static final int ALPHA = 1;

    @Override
    public int getValues(HighlightView target, int tweenType, float[] returnValues) {
        switch (tweenType) {
            case ALPHA:
                returnValues[0] = target.getAlpha();
                return 1;
            default:
                assert false;
                return -1;
        }
    }

    @Override
    public void setValues(HighlightView target, int tweenType, float[] newValues) {
        switch (tweenType) {
            case ALPHA:
                target.setAlpha(newValues[0]);
                break;
            default:
                assert false;
                break;
        }
    }
}
