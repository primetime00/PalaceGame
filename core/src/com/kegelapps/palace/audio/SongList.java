package com.kegelapps.palace.audio;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
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
    private float mCurrentPosition;

    public SongList() {
        mTweenManager = new TweenManager();
        mCurrentTitle = "";
        mCurrentPosition = 0.0f;
        mLastSong = "";
    }

    public void playMusic() {
        if (mPlayList == null)
            generatePlayList();
        if (mCurrentTitle.isEmpty()) {
            mCurrentTitle = mPlayList.first();
            Music song = Director.instance().getAssets().get("music", MusicMap.class).getSong(mCurrentTitle);
            song.play();
            setVolume(0);
            Tween.to(this, AudioAccessor.VOLUME, 5.0f).target(1.0f).start(mTweenManager);
            song.setOnCompletionListener(this);
        }

    }

    @Override
    public void onCompletion(Music music) {
        mPlayList.pop();
        if (mCurrentTitle == mLastSong) //we need a new playlist
            generatePlayList();
        mCurrentTitle = "";
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
}
