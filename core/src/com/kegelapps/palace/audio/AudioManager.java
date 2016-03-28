package com.kegelapps.palace.audio;

import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Logic;

/**
 * Created by Ryan on 3/20/2016.
 */
public class AudioManager {

    final float mMusicVolume = 0.15f;
    private SoundIDList mSoundList;
    private SongList mSongList;

    public enum AudioEvent {
        TRANSITION_TO_MAIN,
        STOP_GAME,
        TRANSITION_TO_OPTIONS,
        GAME_LOADED,
        NEW_GAME, RESUME_FROM_OPTIONS
    }


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
        if (Logic.get().isSimulate())
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

    public void StopMusic() {
        mSongList.stopMusic();
    }

    public void PauseMusic(float time, Runnable done) {
        mSongList.pauseMusic(time, done);
    }

    public void SendEvent(AudioEvent evt, float time) {
        switch (evt) {
            case GAME_LOADED:
                PlayMusic();
                break;
            case STOP_GAME:
            case TRANSITION_TO_MAIN:
                PauseMusic(time, new Runnable() {
                    @Override
                    public void run() {
                        StopMusic();
                    }
                });
                break;
            case TRANSITION_TO_OPTIONS:
                PauseMusic(time, null);
                break;
            case RESUME_FROM_OPTIONS:
                PlayMusic();
                break;
            case NEW_GAME:
                PlayMusic();
                break;
            default:
                break;
        }
    }

    public void SendEvent(AudioEvent evt) {
        SendEvent(evt, 0);
    }

}
