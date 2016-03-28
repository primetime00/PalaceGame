package com.kegelapps.palace.audio;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.kegelapps.palace.tween.AudioAccessor;

import java.util.Iterator;

/**
 * Created by Ryan on 3/21/2016.
 */
public class SoundIDList extends VolumeController{
    private final float mMaxDuration = 4.0f;
    private float mMasterVolume = 1.0f;
    private Array<SoundItem> mList;
    private TweenManager mTweenManager;


    public SoundIDList() {
        mMasterVolume = 1.0f;
        mList = new Array<>();
        mTweenManager = new TweenManager();
    }

    public void play(Sound s) {
        long id = s.play(mMasterVolume);
        mList.add(new SoundItem(s, id));
    }

    public void playLater(final Sound s, float delay) {
        Tween.call(new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                play(s);
            }
        }).delay(delay).start(mTweenManager);

    }

    public void update(float delta) {
        mTweenManager.update(delta);
        if (mList.size == 0)
            return;
        for (Iterator<SoundItem> it = mList.iterator(); it.hasNext();) {
            SoundItem item = it.next();
            item.getSound().setVolume(item.getID(), mMasterVolume);
            item.addTime(delta);
            if (item.isOverTime(mMaxDuration)) {
                item.getSound().stop(item.getID());
                it.remove();
            }
        }
    }

    public void fadeOut(final Runnable done) {
        Tween fade = Tween.to(this, AudioAccessor.VOLUME, 1.0f).target(0.0f);
        fade.setCallbackTriggers(TweenCallback.END);
        fade.setCallback(new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                stopAllSounds();
                if (done != null)
                    done.run();
            }
        });
        fade.start(mTweenManager);
    }

    private void stopAllSounds() {
        for (Iterator<SoundItem> it = mList.iterator(); it.hasNext();) {
            SoundItem item = it.next();
            item.getSound().stop(item.getID());
            it.remove();
        }
    }

    public void fadeIn(final Runnable done) {
        Tween fade = Tween.to(this, AudioAccessor.VOLUME, 1.0f).target(1.0f);
        fade.setCallbackTriggers(TweenCallback.END);
        fade.setCallback(new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                if (done != null)
                    done.run();
            }
        });
        fade.start(mTweenManager);
    }

    public void Reset() {
        stopAllSounds();
    }

    @Override
    public void setVolume(float volume) {
        mMasterVolume = volume;
    }

    @Override
    public float getVolume() {
        return mMasterVolume;
    }
}
