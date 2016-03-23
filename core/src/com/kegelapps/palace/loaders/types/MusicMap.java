package com.kegelapps.palace.loaders.types;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Created by Ryan on 3/21/2016.
 */
public class MusicMap  extends ObjectMap<String, Music> implements Disposable {

    private Array<String> playList;

    public String getSongTitle() {
        if (playList == null || playList.size == 0) {
            createPlayList();
        }
        return playList.pop();
    }

    public Array<String> getTitles() {
        return keys().toArray();
    }

    public Music getSong(String title) {
        return get(title);
    }

    public void createPlayList() {
        playList = new Array<>();
        playList.addAll(this.keys().toArray());
        playList.shuffle();
    }

    @Override
    public void dispose() {
        for (Music m : values().toArray())
            m.dispose();
        if (playList != null)
            playList.clear();
        clear();
    }
}
