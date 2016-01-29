package com.kegelapps.palace.engine.states;

import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.tasks.PlayCPUTurn;
import com.kegelapps.palace.engine.states.tasks.PlayHumanTurn;

/**
 * Created by Ryan on 1/21/2016.
 */
public class Play extends State {

    private StateListener mPlaceCardListener, mTapDeckListener, mTappedDeckRunnable;

    private int mState = 0;

    private StateListener mStateListener;

    private State mTurnState[];

    private int mCurrentPlayer;
    private Table mTable;

    private StateListener mSingleTurnDone;


    public Play(State parent, Table table) {
        super(parent);
        mState = 0;
        mCurrentPlayer = 0;
        mTable = table;

        mSingleTurnDone = new StateListener() {
            @Override
            public void onDoneState() {
                mCurrentPlayer++;
                mCurrentPlayer %= mTable.getHands().size();
            }
        };

        mTurnState = new State[mTable.getHands().size()];
        for (int i=0; i<mTable.getHands().size(); ++i) {
            if (mTable.getHands().get(i).getType() == Hand.HandType.HUMAN) {
                mTurnState[i] = StateFactory.get().createState(Names.PLAY_HUMAN_TURN, this, i);
                mTurnState[i].setStateListener(mSingleTurnDone);
            }
            else {
                mTurnState[i] = StateFactory.get().createState(Names.PLAY_CPU_TURN, this, i);
                mTurnState[i].setStateListener(mSingleTurnDone);
            }
        }
    }

    @Override
    protected boolean OnRun() {
        mTurnState[mCurrentPlayer].Execute();
        return false;
    }

    @Override
    public Names getStateName() {
        return Names.PLAY;
    }
}
