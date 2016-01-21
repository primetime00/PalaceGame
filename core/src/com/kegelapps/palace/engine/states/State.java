package com.kegelapps.palace.engine.states;

import com.kegelapps.palace.Director;
import com.kegelapps.palace.events.EventSystem;

import java.util.ArrayList;

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

    public enum Names {
        GENERIC,
        MAIN,
        DEAL,
        DEAL_CARD,
        SELECT_END_CARDS,
        PLACE_END_CARD,
        TAP_DECK_START
    }

    public interface OnStateListener {
        void onContinueState();
        void onBackState();
    }

    protected boolean mPaused;


    private Status mStatus, mPreviousStatus;

    private State mParent;
    private ArrayList<State> mChildren;

    public State() {
        init();
        mParent = null;
    }
    public State(State parent) {
        init();
        mParent = parent;
    }

    private void init() {
        mPaused = false;
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
            //System.out.print("State changed to " + this + "\n");
            if (mParent != null) {
                mParent.addChild(this);
            }
        }
        return true;
    }

    public void addChild(State child) {
        if (mChildren == null)
            mChildren = new ArrayList<>();
        mChildren.clear();
        mChildren.add(child);
    }

    public boolean containsState(Names name) {
        if (getStateName() == name)
            return true;
        if (mChildren != null) {
            for (State child : mChildren) {
                return child.containsState(name);
            }
        }
        return false;
    }

    public State getState(Names name) {
        if (getStateName() == name)
            return this;
        if (mChildren != null) {
            for (State child : mChildren) {
                return child.getState(name);
            }
        }
        return null;
    }


    public void pause() {
        mPreviousStatus = mStatus;
        mStatus = Status.PAUSED;
    }

    public void resume() {
        if (mStatus == Status.PAUSED)
            mStatus = mPreviousStatus;
    }

    public Names getStateName() {
        return Names.GENERIC;
    }
}
