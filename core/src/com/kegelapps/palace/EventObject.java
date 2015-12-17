package com.kegelapps.palace;

import com.kegelapps.palace.events.Event;
import com.kegelapps.palace.events.HandEvent;
import com.kegelapps.palace.events.TableEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keg45397 on 12/15/2015.
 */
public class EventObject {

    enum EventType {
        SHUFFLE,
        DEAL_CARD,
        DRAW_PLAY_CARD,
        LAYOUT_HIDDEN_CARD,
    }

    private List<Event> mEvents = new ArrayList<>();

    private com.badlogic.gdx.utils.OrderedMap<String, Object> mParams;

    public void AddParam(String name, Object mParam) {
        if (mParams == null)
            mParams = new com.badlogic.gdx.utils.OrderedMap<>();
        mParams.put(name, mParam);
    }

    public Object GetParam(String name) {
        if (mParams != null)
            return mParams.get(name);
        return null;
    }

    public void AddEvent(Event evt) {
        if (!mEvents.contains(evt)) {
            mEvents.add(evt);
        }
    }

    public void Trigger(EventType type) {
        for (Event e : mEvents) {
            switch (type) {
                case DRAW_PLAY_CARD:
                    if (e instanceof TableEvent) {
                        ((TableEvent)e).onFirstCardDrawn(mParams);
                    }
                    break;
                case DEAL_CARD:
                    if (e instanceof TableEvent) {
                        ((TableEvent)e).onCardDeal(mParams);
                    }
                    break;
                case LAYOUT_HIDDEN_CARD:
                    if (e instanceof HandEvent) {
                        ((HandEvent)e).onReceivedHiddenCard(mParams);
                    }
                default:break;
            }
        }
        mParams.clear();
    }

}
