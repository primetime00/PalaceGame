package com.kegelapps.palace.audio;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kegelapps.palace.tween.AudioAccessor;

import java.util.Iterator;

/**
 * Created by Ryan on 3/21/2016.
 */
public class AudioIDList {
    private final float duration = 3.0f;
    private float mMasterVolume = 1.0f;
    private Array<AudioItem> mList;
    private TweenManager mTweenManager;


    public AudioIDList() {
        mMasterVolume = 1.0f;
        mList = new Array<>();
        mTweenManager = new TweenManager();
    }

    public float getMasterVolume() { return mMasterVolume;    }
    public void setMasterVolume(float val) { mMasterVolume = val;}

    public void play(Sound s) {
        long id = s.play(mMasterVolume);
        mList.add(new AudioItem(s, id));
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
        for (Iterator<AudioItem> it = mList.iterator(); it.hasNext();) {
            AudioItem item = it.next();
            item.getSound().setVolume(item.getID(), mMasterVolume);
            item.addTime(delta);
            if (item.isOverTime(duration)) {
                item.getSound().stop(item.getID());
                System.out.print(String.format("Removing item %d from queue\n", item.getID()));
                it.remove();
            }
        }
    }

    public void fadeOut(final Runnable done) {
        Tween fade = Tween.to(this, AudioAccessor.VOLUME, 1.0f).target(0.0f);
        if (done != null) {
            fade.setCallbackTriggers(TweenCallback.END);
            fade.setCallback(new TweenCallback() {
                @Override
                public void onEvent(int type, BaseTween<?> source) {
                    done.run();
                }
            });
        }
        fade.start(mTweenManager);
    }

    public void fadeIn(final Runnable done) {
        Tween fade = Tween.to(this, AudioAccessor.VOLUME, 1.0f).target(1.0f);
        if (done != null) {
            fade.setCallbackTriggers(TweenCallback.END);
            fade.setCallback(new TweenCallback() {
                @Override
                public void onEvent(int type, BaseTween<?> source) {
                    done.run();
                }
            });
        }
        fade.start(mTweenManager);
    }

}
