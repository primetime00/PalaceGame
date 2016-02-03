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
        DEAL_ACTIVE_CARDS,
        SELECT_END_CARD,
        STATE_CHANGE,
        CARD_PLAY_FAILED,
        CARD_PLAY_SUCCESS,
        REPARENT_ALL_VIEWS,
        STATE_LOADED,
        HIGHLIGHT_DECK,
        SELECT_MULTIPLE_CARDS,
        INPLAY_CARDS_CHANGED,
        UNSELECT_MULTIPLE_CARDS,
        CHANGE_TURN, BURN_CARDS,
    }

    ObjectMap<EventType, Array<EventListener>> mListeners;
    Array<Event> mLaterEvents;

    public EventSystem() {
        mListeners = new ObjectMap<>();
        mLaterEvents = new Array<>();
    }

    public void RegisterEvent(EventListener evt) {
        if (mListeners.containsKey(evt.getType()) && !mListeners.get(evt.getType()).contains(evt, false)) {
            mListeners.get(evt.getType()).add(evt);
        }
        else if (!mListeners.containsKey(evt.getType())) {
            Array<EventListener> items = new Array<EventListener>();
            items.add(evt);
            mListeners.put(evt.getType(), items);
        }
    }

    public void UnregisterEvent(EventListener evt) {
        if (mListeners.containsKey(evt.getType()) && mListeners.get(evt.getType()).contains(evt, false)) {
            mListeners.get(evt.getType()).removeValue(evt, false);
        }
    }

    public void Fire(EventType evt) {
        processEvent(new Event(evt, null));
    }

    public void Fire(EventType evt, Object... objs) {
        processEvent(new Event(evt, objs));
    }

    public void FireLater(EventType evt, Object... objs) {
        addLaterEvent(new Event(evt, objs));
    }

    public void FireLater(EventType evt) {
        addLaterEvent(new Event(evt, null));
    }

    private void addLaterEvent(Event event) {
        mLaterEvents.add(event);
    }

    private void processEvent(Event evt) {
        Array<EventListener> items = mListeners.get(evt.GetType());
        if (items == null)
            return;
        for (EventListener e : items) {
            e.updateParams();
            e.handle(evt.GetParams());
        }
    }

    public void ProcessWaitingEvents() {
        for (Event e : mLaterEvents) {
            processEvent(e);
        }
        mLaterEvents.clear();
    }

    @Override
    public void dispose() {
        mListeners.clear();
        mLaterEvents.clear();
    }

    public interface EventProcessor {
        void updateParams();
        void handle(Object[] params);
    }

    static public class Event {
        private EventType mType;
        private Object mParams[];

        public Event(EventType type, Object params[]) {
            mType = type;
            mParams = params;
        }

        public EventType GetType() {
            return mType;
        }

        public Object[] GetParams() {
            return mParams;
        }
    }

    static public class EventListener implements EventProcessor {
        private ObjectMap<String, Object> mParamMap;
        private EventType mType;

        public EventListener(EventType type) {
            mParamMap = new ObjectMap<>();
            mType = type;
        }

        public EventType getType() {
            return mType;
        }


        @Override
        public void updateParams() {

        }

        @Override
        public void handle(Object params[]) {

        }
    }


}
