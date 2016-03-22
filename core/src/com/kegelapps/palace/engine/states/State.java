package com.kegelapps.palace.engine.states;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Serializer;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.StateProtos;

import java.util.ArrayList;
import java.util.Iterator;

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
        DEAL_HIDDEN_CARD,
        DEAL_SHOWN_CARD,
        SELECT_END_CARDS,
        PLACE_END_CARD,
        PLAY,
        PLAY_HUMAN_TURN,
        PLAY_CPU_TURN,
        DRAW_PLAY_CARD,
        TAP_DECK_START,
        BURN_CARDS,
        SELECT_CARDS_FROM_DECK,
        PLAY_HIDDEN_CARD,
        WIN
    }

    public interface OnStateListener {
        void onContinueState();
        void onBackState();
        void onDoneState();
        void onDoneState(Object result);
    }

    protected boolean mPaused;
    protected int mID;


    protected Status mStatus, mPreviousStatus;

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

    public void UserSignal(Logic.RequestType type) {

    }

    public void addChild(State child) {
        if (mChildren == null)
            mChildren=  new ArrayList<>();
        if (!mChildren.contains(child))
            mChildren.add(child);
    }

    private void removeChild(State child) {
        if (mChildren == null)
            return;
        for (Iterator<State> it = mChildren.iterator(); it.hasNext();) {
            State state = it.next();
            if (state == child)
                it.remove();
        }
    }


    public boolean containsState(Names name) {
        if (getStateName() == name)
            return true;
        if (mChildren != null) {
            for (State s : mChildren) {
                if (s.containsState(name))
                    return true;
            }
        }
        return false;
    }

    public State getState(Names name) {
        return getState(name, 0);
    }

    public State getState(Names name, int id) {
        if (getStateName() == name && getID() == id)
            return this;

        if (mChildren != null) {
            for (State s : mChildren) {
                State res = s.getState(name, id);
                if (res != null)
                    return res;
            }
        }
        return null;
    }


    public void pause() {
        if (mStatus != Status.PAUSED)
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
        setStatus(Status.values()[state.getStatus()]);
        mPaused = state.getPaused();
        setID(state.getId());
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
        builder.setId(getID());
        builder.setStatus(getStatus().ordinal());
        if (mPreviousStatus != null)
            builder.setPreviousStatus(mPreviousStatus.ordinal());
        stateProto = StateFactory.get().WriteStateList(mChildrenStates, builder.build(), this);
        return stateProto;
    }

    public void Reset() {
        mPaused = false;
        mStatus = Status.NOT_STARTED;

        for (ArrayList<State> s : mChildrenStates.values()) {
            for (State state : s) {
                state.Reset();
            }
        }


    }

}
