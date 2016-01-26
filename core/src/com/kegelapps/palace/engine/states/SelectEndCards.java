package com.kegelapps.palace.engine.states;

import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.tasks.PlaceEndCard;
import com.kegelapps.palace.engine.states.tasks.TapToStart;

/**
 * Created by Ryan on 1/13/2016.
 */
public class SelectEndCards extends State {

    private StateListener mPlaceCardListener, mTapDeckListener;

    private int mState = 0;

    private StateListener mStateListener;
    State [] mStates;


    public SelectEndCards(State parent, Table table, StateListener done) {
        super(parent);
        createStates(table);
        mState = 0;
        mStateListener = done;
    }

    private void createStates(Table table) {
        mStates = new State[2];
        mPlaceCardListener = new StateListener() {
            @Override
            public void onContinueState() {
                mStates[mState].setStatus(Status.NOT_STARTED);
                mState = 1;
            }
        };

        mTapDeckListener = new StateListener() {
            @Override
            public void onContinueState() {
                mStates[mState].setStatus(Status.NOT_STARTED);
                mState = 2;
            }

            @Override
            public void onBackState() {
                mStates[mState].setStatus(Status.NOT_STARTED);
                mState = 0;
            }
        };

        mStates[0] = new PlaceEndCard(this, table, mPlaceCardListener);
        mStates[1] = new TapToStart(this, table, mTapDeckListener);


    }

    @Override
    public boolean Run() {
        super.Run();
        if (mState < mStates.length)
            mStates[mState].Run();
        if (mState > 1) {
            if (mStateListener != null)
                mStateListener.onContinueState();
            return true;
        }
        return false;
    }

    @Override
    public Names getStateName() {
        return Names.SELECT_END_CARDS;
    }
}
