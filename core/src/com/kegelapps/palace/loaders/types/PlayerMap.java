package com.kegelapps.palace.loaders.types;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by keg45397 on 3/17/2016.
 */
public class PlayerMap extends ObjectMap<Integer, PlayerData> implements Disposable {

    public List<Integer> getIDs() {
        List<Integer> ids = new ArrayList<>();
        for (int key: keys().toArray()) {
            ids.add(key);
        }
        return ids;
    }

    public List<Integer> getRandomIDs() {
        List<Integer> ids = getIDs();
        Collections.shuffle(ids);
        return ids;
    }

    @Override
    public void dispose() {
        clear();
    }
}
