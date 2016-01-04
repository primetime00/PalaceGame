package com.kegelapps.palace.engine.states;

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
        if (mStatus == Status.NOT_STARTED)
            mStatus = Status.ACTIVE;
        return true;
    }
}
