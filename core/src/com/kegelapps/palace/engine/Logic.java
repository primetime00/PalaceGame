package com.kegelapps.palace.engine;

import java.util.ArrayList;
import java.util.List;

import com.kegelapps.palace.engine.states.*;
import com.kegelapps.palace.engine.states.tasks.PlaceEndCard;
import com.kegelapps.palace.engine.states.tasks.TapToStart;

/**
 * Created by Ryan on 12/5/2015.
 */
public class Logic {

    private static Logic mLogic;


    public enum LogicRequest {
        PLAY_START;
    }

    private int mNumberOfPlayers = 0;

    private boolean mFastDeal = false;

    //states
    Main mMainState;

    private Table mTable;

    boolean mPaused;

    public Logic() {
        mPaused = false;
    }

    static public Logic get() {
        if (mLogic == null)
            mLogic = new Logic();
        return mLogic;
    }

    public void SetTable(Table table) {
        mTable = table;
        mMainState = new Main(mTable);
        mNumberOfPlayers = table.getHands().size();
    }

    public void Pause(boolean pause) {
        //System.out.print("Logic system is " + (pause ? "Paused" : "UnPaused") + "\n");
        if (mMainState != null) {
            if (pause)
                mMainState.pause();
            else
                mMainState.resume();
        }
        mPaused = pause;
    }

    public void Poll() {
        if (mMainState == null)
            return;
        if (mMainState.getStatus() == State.Status.PAUSED)
            return;
        mMainState.Execute();
    }


    public void PlayerSelectCard(Hand h, Card c) {
        if (mMainState == null)
            return;
        if (mMainState.containsState(State.Names.PLACE_END_CARD))
            h.SelectEndCard(c);
        else if (mMainState.containsState(State.Names.PLAY))
            h.SelectPlayCard(c);
    }

    public void PlayerUnselectCard(Hand h, Card c) {
        if (mMainState != null)
            h.DeselectEndCard(c);
    }

    public void Request(LogicRequest req, State.Names stateName) {
        switch (req) {
            default:
            case PLAY_START:
                if (mMainState.containsState(stateName))
                    mMainState.getState(stateName).UserSignal();
                break;
        }
    }

    public boolean isFastDeal() {
            return mFastDeal;
    }

    public void setFastDeal(boolean mFastDeal) {
        this.mFastDeal = mFastDeal;
    }

    public boolean TestCard(Card card) {
        Card top = mTable.GetTopPlayCard();
        if (top == null)
            return false;
        return card.compareTo(top) > -1;
    }

    public State getMainState() {
        return mMainState;
    }
}
