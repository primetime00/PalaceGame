package com.kegelapps.palace.audio;

import com.kegelapps.palace.scenes.Scene;

/**
 * Created by Ryan on 3/20/2016.
 */
public class AudioManager {

    private AudioIDList mSoundList;


    public AudioManager() {
        mSoundList = new AudioIDList();
    }

    public void update(float delta) {
        mSoundList.update(delta);
    }

    public void FadeOutSound(Runnable done) {
        mSoundList.fadeOut(done);
    }

    public void FadeInSound(Runnable done) {
        mSoundList.fadeIn(done);
    }


    public void QueueSound(final SoundEvent evt) {
        if (evt.getDelay() <= 0) {
            mSoundList.play(evt.getSound());
            return;
        }
        mSoundList.playLater(evt.getSound(), evt.getDelay());
    }
}
