package com.kegelapps.palace;

import com.kegelapps.palace.events.Event;
import com.kegelapps.palace.events.TableEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keg45397 on 12/15/2015.
 */
public class EventObject {

    enum EventType {
        SHUFFLE,
        DRAW_PLAY_CARD,
    }

    private List<Event> mEvents = new ArrayList<>();

    public void AddEvent(Event evt) {
        if (!mEvents.contains(evt)) {
            mEvents.add(evt);
        }
    }

    public void Trigger(EventType type, Object data) {
        for (Event e : mEvents) {
            switch (type) {
                case DRAW_PLAY_CARD:
                    if (e instanceof TableEvent) {
                        ((TableEvent)e).onFirstCardDrawn((Card)data);
                    }
                    break;
                default:break;
            }
        }
    }

}
