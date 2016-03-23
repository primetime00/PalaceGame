package com.kegelapps.palace.audio;

import com.badlogic.gdx.audio.Music;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.loaders.types.MusicMap;

/**
 * Created by Ryan on 3/20/2016.
 */
public class AudioManager {

    final float mMusicVolume = 0.15f;
    private SoundIDList mSoundList;
    private SongList mSongList;


    public AudioManager() {
        mSoundList = new SoundIDList();
        mSongList = new SongList();
    }

    public void update(float delta) {
        mSoundList.update(delta);
        mSongList.update(delta);
    }

    public void FadeOutSound(Runnable done) {
        mSoundList.fadeOut(done);
    }

    public void FadeInSound(Runnable done) {
        mSoundList.fadeIn(done);
    }


    public void QueueSound(final SoundEvent evt) {
        if (!Director.instance().getOptions().getSound())
            return;
        if (evt.getDelay() <= 0) {
            mSoundList.play(evt.getSound());
            return;
        }
        mSoundList.playLater(evt.getSound(), evt.getDelay());
    }

    public void PlayMusic() {
        if (!Director.instance().getOptions().getMusic())
            return;
        mSongList.playMusic();
    }

    public void SetMasterVolume(float v) {
        mSoundList.setVolume(v);
        mSongList.setVolume(v);
    }

    public void Reset() {
        mSoundList.Reset();
    }

}
