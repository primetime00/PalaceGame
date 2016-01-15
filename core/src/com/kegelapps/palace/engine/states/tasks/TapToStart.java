package com.kegelapps.palace.engine.states.tasks;

import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;

/**
 * Created by keg45397 on 1/15/2016.
 */
public class TapToStart extends State {
    private Table mTable;

    private Runnable mBackRunnable, mNextRunnable;


    public TapToStart(Table table, Runnable play, Runnable back) {
        super();
        mTable = table;
        mBackRunnable = back;
        mNextRunnable = play;
    }


    @Override
    public boolean Run() {
        super.Run();
        for (Hand hand: mTable.getHands()) {
            if (hand.getEndCards().size() < 3)
                if (mBackRunnable != null)
                    mBackRunnable.run();
                return true;
        }
        return false;
    }
}
