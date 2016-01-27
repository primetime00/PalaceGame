package com.kegelapps.palace.engine.states;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Serializer;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.StateProtos;

import java.util.ArrayList;

/**
 * Created by Ryan on 12/23/2015.
 */
public class State implements Serializer{

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
        PLAY, PLAY_HUMAN_TURN, PLAY_CPU_TURN, TAP_DECK_START
    }

    public interface OnStateListener {
        void onContinueState();
        void onBackState();
        void onDoneState();
    }

    protected boolean mPaused;


    private Status mStatus, mPreviousStatus;

    protected StateListener mStateListener;

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

    public void Execute() {
        boolean ret;
        if (mStatus == Status.NOT_STARTED || mStatus == Status.DONE) {
            mStatus = Status.ACTIVE;
            Director.instance().getEventSystem().Fire(EventSystem.EventType.STATE_CHANGE, this);
            //System.out.print("State changed to " + this + "\n");
            if (mParent != null) {
                mParent.addChild(this);
            }
            FirstRun();
        }
        ret = Run();
        if (ret == true) {//we finished the state?
            EndRun();
            mStatus = Status.DONE;
            if (mStateListener != null)
                mStateListener.onDoneState();
            if (mParent != null) {
                mParent.removeChild(this);
            }
        }
    }

    protected boolean Run() {
        return true;
    }

    protected void EndRun() {

    }

    public void UserSignal() {

    }

    public void addChild(State child) {
        if (mChildren == null)
            mChildren = new ArrayList<>();
        mChildren.clear();
        mChildren.add(child);
    }

    private void removeChild(State state) {
        if (mChildren == null)
            return;
        mChildren.remove(state);
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

    protected void FirstRun() {

    }

    public Names getStateName() {
        return Names.GENERIC;
    }

    @Override
    public void ReadBuffer(Message msg) {

    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State.Builder builder = StateProtos.State.newBuilder();
        builder.setType(getStateName().ordinal());
        builder.setPaused(mPaused);
        builder.setStatus(mStatus.ordinal());
        if (mPreviousStatus != null)
            builder.setPreviousStatus(mPreviousStatus.ordinal());
        if (mChildren != null) {
            for (State s : mChildren) {
                builder.addChildren(s.getStateName().ordinal());
                builder.addChildrenStates((StateProtos.State) s.WriteBuffer());
            }
        }
        return builder.build();
    }
}
