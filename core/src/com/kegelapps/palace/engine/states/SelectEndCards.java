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

    State [] mStates;


    public SelectEndCards(State parent, Table table, StateListener listener) {
        super(parent);
        createStates(table);
        mState = 0;
        mStateListener = listener;
    }

    private void createStates(Table table) {
        mStates = new State[2];
        mPlaceCardListener = new StateListener() {
            @Override
            public void onContinueState() {
                mState = 1;
            }
        };

        mTapDeckListener = new StateListener() {
            @Override
            public void onContinueState() {
                mState = 2;
            }

            @Override
            public void onBackState() {
                mState = 0;
            }
        };

        mStates[0] = new PlaceEndCard(this, table, mPlaceCardListener);
        mStates[1] = new TapToStart(this, table, mTapDeckListener);


    }

    @Override
    public boolean Run() {
        if (mState < mStates.length)
            mStates[mState].Execute();
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
