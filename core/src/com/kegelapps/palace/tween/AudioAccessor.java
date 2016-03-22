package com.kegelapps.palace.tween;

import aurelienribon.tweenengine.TweenAccessor;
import com.badlogic.gdx.graphics.Color;
import com.kegelapps.palace.audio.AudioIDList;

/**
 * Created by Ryan on 3/21/2016.
 */
public class AudioAccessor implements TweenAccessor<AudioIDList> {
    public static final int VOLUME = 1;

    @Override
    public int getValues(AudioIDList target, int tweenType, float[] returnValues) {
        switch (tweenType) {
            case VOLUME: returnValues[0] = target.getMasterVolume(); return 1;
            default: assert false; return -1;
        }
    }

    @Override
    public void setValues(AudioIDList target, int tweenType, float[] newValues) {
        switch (tweenType) {
            case VOLUME: target.setMasterVolume(newValues[0]); break;
            default: assert false; break;
        }
    }
}
