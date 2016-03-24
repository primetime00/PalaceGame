package com.kegelapps.palace.audio;

import aurelienribon.tweenengine.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Array;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.loaders.types.MusicMap;
import com.kegelapps.palace.tween.AudioAccessor;

/**
 * Created by Ryan on 3/22/2016.
 */
public class SongList extends VolumeController implements Music.OnCompletionListener {
    final private float mMaxVolume = 0.15f;
    private float mMasterVolume = 1.0f;
    private TweenManager mTweenManager;
    private Array<String> mPlayList;
    private String mLastSong;

    private String mCurrentTitle;
    private boolean mPausing;

    public SongList() {
        mTweenManager = new TweenManager();
        mCurrentTitle = "";
        mLastSong = "";
        mPausing = false;
    }

    public void playMusic() {
        if (mPlayList == null || mPlayList.size == 0)
            generatePlayList();
        mTweenManager.killAll();
        if (mCurrentTitle.isEmpty()) {
            mCurrentTitle = mPlayList.first();
            Music song = Director.instance().getAssets().get("music", MusicMap.class).getSong(mCurrentTitle);
            song.play();
            setVolume(0);
            Timeline mus = Timeline.createParallel().createSequence();
            mus.push(Tween.set(this, AudioAccessor.VOLUME).target(0.0f));
            mus.push(Tween.to(this, AudioAccessor.VOLUME, 5.0f).target(1.0f));
            mus.start(mTweenManager);
            song.setOnCompletionListener(this);
        }
        else {
            Music song = Director.instance().getAssets().get("music", MusicMap.class).getSong(mCurrentTitle);
            if (song.getPosition() > 0) { //we are paused?
                song.play();
                setVolume(0);
                Timeline mus = Timeline.createParallel().createSequence();
                mus.push(Tween.set(this, AudioAccessor.VOLUME).target(0.0f));
                mus.push(Tween.to(this, AudioAccessor.VOLUME, 5.0f).target(1.0f));
                mus.start(mTweenManager);
                song.setOnCompletionListener(this);
            }
        }
        mPausing = false;
    }

    @Override
    public void onCompletion(Music music) {
        mPlayList.pop();
        if (mCurrentTitle == mLastSong) //we need a new playlist
            generatePlayList();
        mCurrentTitle = "";
        if (!mPausing)
            playMusic();
    }

    private void generatePlayList() {
        mPlayList = new Array<>(Director.instance().getAssets().get("music", MusicMap.class).getTitles());
        String last = mLastSong;
        do {
            mPlayList.shuffle();
            mLastSong = mPlayList.get(mPlayList.size - 1);
        } while (mPlayList.first().equals(last));

    }

    public void update(float delta) {
        mTweenManager.update(delta);
    }

    @Override
    public void setVolume(float volume) {
        mMasterVolume = volume;
        if (!mCurrentTitle.isEmpty()) {
            Music song = Director.instance().getAssets().get("music", MusicMap.class).getSong(mCurrentTitle);
            song.setVolume(mMasterVolume * mMaxVolume);
        }


    }

    @Override
    public float getVolume() {
        return mMasterVolume;
    }

    public void stopMusic() {
        if (!mCurrentTitle.isEmpty()) {
            Music song = Director.instance().getAssets().get("music", MusicMap.class).getSong(mCurrentTitle);
            song.stop();
            mCurrentTitle = "";
            mPlayList.clear();
        }
    }

    public void pauseMusic(float time, final Runnable done) {
        if (mCurrentTitle.isEmpty())
            return;
        mPausing = true;
        final Music song = Director.instance().getAssets().get("music", MusicMap.class).getSong(mCurrentTitle);
        mTweenManager.killAll();
        Timeline mus = Timeline.createParallel().createSequence();
        mus.push(Tween.set(this, AudioAccessor.VOLUME).target(getVolume()));
        mus.push(Tween.to(this, AudioAccessor.VOLUME, time).target(0.0f));
        mus.setCallbackTriggers(TweenCallback.END);
        mus.setCallback(new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                if (type == TweenCallback.END) {
                    if (!mCurrentTitle.isEmpty())
                        song.pause();
                    mPausing = false;
                    if (done != null)
                        done.run();
                }
            }
        });
        mus.start(mTweenManager);
    }
}
