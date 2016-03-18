package com.kegelapps.palace.graphics.ui.common;

import com.badlogic.gdx.utils.ObjectMap;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.loaders.types.StringStringMap;

/**
 * Created by keg45397 on 3/15/2016.
 */
public class StringMap {

    static private StringMap mInstance;

     private StringStringMap mNameMap;

    public StringMap() {
        mNameMap = Director.instance().getAssets().get("strings.xml", StringStringMap.class);
    }

    public StringStringMap getMap() {
        return mNameMap;
    }
    public static String getString(String key) {
        if (mInstance == null)
            mInstance = new StringMap();
        return mInstance.getMap().get(key);
    }
}
