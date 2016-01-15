package com.kegelapps.palace.engine.states;

import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.tasks.PlaceEndCard;
import com.kegelapps.palace.engine.states.tasks.TapToStart;

/**
 * Created by Ryan on 1/13/2016.
 */
public class SelectEndCards extends State {

    private PlaceEndCard mPlaceEndCardState;
    private TapToStart mTapToStartState;
    private Runnable mMoveToTapState, mMoveToPlaceState, mTappedDeckState;
    private int mState = 0;

    private Runnable mDoneRunnable;


    public SelectEndCards(Table table, Runnable done) {
        super();
        mState = 0;
        if (mMoveToTapState == null) {
            mMoveToTapState = new Runnable() {
                @Override
                public void run() {
                    mState = 1;
                }
            };
        }
        if (mMoveToPlaceState == null) {
            mMoveToPlaceState = new Runnable() {
                @Override
                public void run() {
                    mState = 0;
                }
            };
        }
        if (mTappedDeckState == null) {
            mTappedDeckState = new Runnable() {
                @Override
                public void run() {
                    mState = 2;
                }
            };
        }
        mDoneRunnable = done;
        mPlaceEndCardState = new PlaceEndCard(table, mMoveToTapState);
        mTapToStartState = new TapToStart(table, mMoveToPlaceState, mTappedDeckState);
    }

    @Override
    public boolean Run() {
        super.Run();
        switch (mState) {
            case 0: mPlaceEndCardState.Run(); break;
            case 1: mTapToStartState.Run(); break;
            default: break;
        }
        if (mState > 1) {
            if (mDoneRunnable != null)
                mDoneRunnable.run();
            return true;
        }
        return false;
    }
}
