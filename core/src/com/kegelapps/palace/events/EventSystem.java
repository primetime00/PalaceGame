package com.kegelapps.palace.events;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Created by Ryan on 12/22/2015.
 */
public class EventSystem implements Disposable{
    public enum EventType {
        SHUFFLE,
        DEAL_CARD,
        DRAW_PLAY_CARD,
        LAYOUT_HIDDEN_CARD,
        LAYOUT_ACTIVE_CARD,
        DEAL_ACTIVE_CARDS, SELECT_END_CARD, STATE_CHANGE, CARD_PLAY_FAILED, CARD_PLAY_SUCCESS,
    }

    ObjectMap<EventType, Array<Event>> mListeners;
    Object params[];

    public EventSystem() {
        mListeners = new ObjectMap<>();
    }

    public void RegisterEvent(Event evt) {
        if (mListeners.containsKey(evt.getType()) && !mListeners.get(evt.getType()).contains(evt, false)) {
            mListeners.get(evt.getType()).add(evt);
        }
        else if (!mListeners.containsKey(evt.getType())) {
            Array<Event> items = new Array<Event>();
            items.add(evt);
            mListeners.put(evt.getType(), items);
        }
    }

    public void UnregisterEvent(Event evt) {
        if (mListeners.containsKey(evt.getType()) && mListeners.get(evt.getType()).contains(evt, false)) {
            mListeners.get(evt.getType()).removeValue(evt, false);
        }
    }

    public void Fire(EventType evt) {
        processEvents(evt);
    }

    public void Fire(EventType evt, Object... objs) {
        params = objs;
        processEvents(evt);
    }


    private void processEvents(EventType evt) {
        Array<Event> items = mListeners.get(evt);
        if (items == null)
            return;
        for (Event e : items) {
            e.updateParams();
            e.handle(params);
        }
    }

    @Override
    public void dispose() {
        mListeners.clear();
    }

    public interface EventProcessor {
        void updateParams();
        void handle(Object [] params);
    }

    static public class Event implements EventProcessor {
        private ObjectMap<String, Object> mParamMap;
        private EventType mType;

        public Event(EventType type) {
            mParamMap = new ObjectMap<>();
            mType = type;
        }

        public EventType getType() {
            return mType;
        }

        public void AddParam(String name, Object item) {
            mParamMap.put(name, item);
        }

        @Override
        public void updateParams() {

        }

        @Override
        public void handle(Object params[]) {

        }
    }


}
