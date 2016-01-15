package com.kegelapps.palace.engine.states;

import com.kegelapps.palace.Director;
import com.kegelapps.palace.events.EventSystem;

/**
 * Created by Ryan on 12/23/2015.
 */
public class State {

    public enum Status{
        NOT_STARTED,
        ACTIVE,
        DONE,
        PAUSED
    }

    private Status mStatus;

    public State() {
        mStatus = Status.NOT_STARTED;
    }

    public Status getStatus() {return mStatus;}
    public void setStatus(Status stat) {
        mStatus = stat;
    }

    public boolean Run() {
        if (mStatus == Status.NOT_STARTED) {
            mStatus = Status.ACTIVE;
            Director.instance().getEventSystem().Fire(EventSystem.EventType.STATE_CHANGE, this);
            System.out.print("State changed to " + this);
        }
        return true;
    }
}
