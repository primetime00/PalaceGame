package com.kegelapps.palace.engine.states;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Serializer;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.StateProtos;

import java.util.ArrayList;
import java.util.List;

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
        PLAY,
        PLAY_HUMAN_TURN,
        PLAY_CPU_TURN,
        TAP_DECK_START
    }

    public interface OnStateListener {
        void onContinueState();
        void onBackState();
        void onDoneState();
    }

    protected boolean mPaused;
    protected int mID;


    private Status mStatus, mPreviousStatus;

    protected StateListener mStateListener;

    private State mParent;
    private ArrayList<State> mChildren;
    protected StateFactory.StateList mChildrenStates;

    public State() {
        init();
        mParent = null;
        mChildrenStates = new StateFactory.StateList();
    }
    public State(State parent) {
        init();
        mParent = parent;
        mChildrenStates = new StateFactory.StateList();
    }

    public void setID(int id) {
        mID = id;
    }

    public int getID() {
        return mID;
    }

    public void setStateListener(StateListener listener) {
        mStateListener = listener;
    }

    private void init() {
        mPaused = false;
        mStatus = Status.NOT_STARTED;
        mID = 0;
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
            OnFirstRun();
        }
        ret = OnRun();
        if (ret == true) {//we finished the state?
            OnEndRun();
            mStatus = Status.DONE;
            if (mStateListener != null)
                mStateListener.onDoneState();
            if (mParent != null) {
                mParent.removeChild(this);
            }
        }
    }

    protected boolean OnRun() {
        return true;
    }

    protected void OnEndRun() {

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

    public List<State> getChildren() {
        return mChildren;
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

    protected void OnFirstRun() {

    }

    public Names getStateName() {
        return Names.GENERIC;
    }

    @Override
    public void ReadBuffer(Message msg) {
        StateProtos.State state = (StateProtos.State) msg;
        mStatus = Status.values()[state.getStatus()];
        mPaused = state.getPaused();
        mID = state.getId();
        if (state.hasPreviousStatus())
            mPreviousStatus = Status.values()[state.getPreviousStatus()];
        if (state.getChildrenStatesCount() > 0) {
            mChildrenStates = StateFactory.get().ParseStateList(state, this);
        }

    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State stateProto;
        StateProtos.State.Builder builder = StateProtos.State.newBuilder();
        builder.setType(getStateName().ordinal());
        builder.setPaused(mPaused);
        builder.setId(mID);
        builder.setStatus(mStatus.ordinal());
        if (mPreviousStatus != null)
            builder.setPreviousStatus(mPreviousStatus.ordinal());
        stateProto = StateFactory.get().WriteStateList(mChildrenStates, builder.build(), this);
        return stateProto;
    }
}
