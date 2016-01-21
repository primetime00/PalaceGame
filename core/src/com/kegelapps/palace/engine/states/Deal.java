package com.kegelapps.palace.engine.states;

import com.kegelapps.palace.Director;
import com.kegelapps.palace.animations.CameraAnimation;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.events.EventSystem;

/**
 * Created by Ryan on 12/23/2015.
 */
public class Deal extends State{
    private Table mTable;
    private int mCurrentPlayer, mLastPlayer;
    private int mRound;
    private Runnable mDoneRunnable;

    public Deal(Table table, Runnable done) {
        super();
        mTable = table;
        mCurrentPlayer = 0;
        mLastPlayer = -1;
        mRound = 0;
        mDoneRunnable = done;
    }

    @Override
    public boolean Run() {
        super.Run();
        boolean result = false;
        if (mRound == 0 && mCurrentPlayer == 0) {
            System.out.print("Dealing cards...");
        }
        if (mRound < 3) {
            if (mTable.DealHiddenCard(mCurrentPlayer)) { //returns true once the card is dealt!, otherwise false!
                mCurrentPlayer += 1;
                if (mCurrentPlayer >= 4) {
                    mCurrentPlayer = 0;
                    mRound += 1;
                }
            }
            result = false;
        }
        else if (mRound < 10) {
            if (mRound == 3 && mCurrentPlayer == 0)
                Director.instance().getEventSystem().Fire(EventSystem.EventType.DEAL_ACTIVE_CARDS, mRound, mCurrentPlayer);
            if (mTable.DealActiveCard(mCurrentPlayer)) {
                mCurrentPlayer += 1;
                if (mCurrentPlayer >= 4) {
                    mCurrentPlayer = 0;
                    mRound += 1;
                }
            }
            result = false;
        }
        else {
            if (mDoneRunnable != null) {
                mDoneRunnable.run();
            }
            setStatus(Status.DONE);
            result = true;
        }
        mLastPlayer = mCurrentPlayer;
        return result;
    }

    @Override
    public Names getStateName() {
        return Names.DEAL;
    }
}
