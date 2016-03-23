package com.kegelapps.palace.tween;

import aurelienribon.tweenengine.TweenAccessor;
import com.kegelapps.palace.audio.SoundIDList;
import com.kegelapps.palace.audio.VolumeController;

/**
 * Created by Ryan on 3/21/2016.
 */
public class AudioAccessor implements TweenAccessor<VolumeController> {
    public static final int VOLUME = 1;

    @Override
    public int getValues(VolumeController target, int tweenType, float[] returnValues) {
        switch (tweenType) {
            case VOLUME: returnValues[0] = target.getVolume(); return 1;
            default: assert false; return -1;
        }
    }

    @Override
    public void setValues(VolumeController target, int tweenType, float[] newValues) {
        switch (tweenType) {
            case VOLUME:
                target.setVolume(newValues[0]); break;
            default: assert false; break;
        }
    }
}
