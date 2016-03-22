package com.kegelapps.palace.audio;

import com.badlogic.gdx.audio.Sound;

/**
 * Created by Ryan on 3/19/2016.
 */
public class SoundEvent {
    private Sound mSound;
    private float mDelay;

    public SoundEvent(Sound mSound, float mDelay) {
        this.mDelay = mDelay;
        this.mSound = mSound;
    }

    public float getDelay() {
        return mDelay;
    }

    public Sound getSound() {
        return mSound;
    }
}
