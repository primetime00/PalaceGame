package com.kegelapps.palace.graphics.ui.common;

import com.badlogic.gdx.utils.ObjectMap;
import com.kegelapps.palace.Director;

/**
 * Created by keg45397 on 3/15/2016.
 */
public class StringMap {

    static private StringMap mInstance;

     private ObjectMap<String, String> mNameMap;

    public StringMap() {
        mNameMap = Director.instance().getAssets().get("strings.xml", ObjectMap.class);
    }

    public ObjectMap<String, String> getMap() {
        return mNameMap;
    }
    public static String getString(String key) {
        if (mInstance == null)
            mInstance = new StringMap();
        return mInstance.getMap().get(key);
    }
}
