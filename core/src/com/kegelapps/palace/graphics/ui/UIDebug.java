package com.kegelapps.palace.graphics.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.Iterator;

/**
 * Created by keg45397 on 4/26/2016.
 */
public class UIDebug {

    static public class Entry {
        protected ChangeListener listener;
        protected float timer;
        protected Actor actor;

        public Entry(Actor actor, ChangeListener listener, float timer) {
            this.actor = actor;
            this.listener = listener;
            this.timer = timer;
        }
    }
    private ObjectMap<String, Entry> mChangeListenerList;

    private static UIDebug ourInstance = new UIDebug();

    private float mBaseTime;
    private boolean mEnable;

    public static UIDebug get() {
        return ourInstance;
    }

    private UIDebug() {
        mChangeListenerList = new ObjectMap<>();
        mBaseTime = 0;
        mEnable = false;
    }

    public void addChangeListener(String id, ChangeListener listener, float timer) {
        addChangeListener(id, listener, timer, null);
    }

    public void addChangeListener(String id, ChangeListener listener, float timer, Actor actor) {
        if (!mEnable)
            return;
        if (!mChangeListenerList.containsKey(id)) {
            mChangeListenerList.put(id, new Entry(actor, listener, timer+mBaseTime));
        }
    }


    public void update(float delta) {
        if (!mEnable)
            return;
        mBaseTime += delta;
        for (Iterator it = mChangeListenerList.iterator(); it.hasNext();) {
            ObjectMap.Entry<String, Entry> entry = (ObjectMap.Entry<String, Entry>) it.next();
            if (mBaseTime >= entry.value.timer) {
                entry.value.listener.changed(null, entry.value.actor);
                it.remove();
            }
        }
    }

}
