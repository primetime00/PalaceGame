package com.kegelapps.palace.audio;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Created by Ryan on 3/19/2016.
 */
public class SoundMap extends ObjectMap<String, SoundMap.SoundList> implements Disposable {

    static public class SoundList extends Array<Sound> {
        public Sound random() {
            int i = MathUtils.random(0, size-1);
            return get(i);
        }
    }

    public Sound getRandom(String category) {
        if (get(category) == null)
            return null;
        return get(category).random();
    }

    @Override
    public void dispose() {
        Array<SoundList> soundLists = values().toArray();
        for (SoundList sList : soundLists) {
            for (Sound s : sList)
                s.dispose();
        }
        clear();
    }

}
