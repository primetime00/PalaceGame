package com.kegelapps.palace.engine.states.playtasks;

import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.Play;
import com.kegelapps.palace.engine.states.State;

/**
 * Created by Ryan on 2/5/2016.
 */
public class EndTurnDrawCards extends State {

    private Table mTable;
    private int mCurrentPlayer;

    public EndTurnDrawCards(State parent, Table table) {
        super(parent);
        this.mTable = table;
    }

    @Override
    protected void OnFirstRun() {
        //whose turn is it
        Play state = (Play) Logic.get().GetMainState().getState(Names.PLAY);
        if (state == null)
            throw new RuntimeException("Could not find Play state.  It is required!");
        mCurrentPlayer = mTable.getCurrentPlayer();
    }

    @Override
    protected boolean OnRun() {
        mTable.DrawEndTurnCards(mCurrentPlayer);
        return true;
    }

    @Override
    public Names getStateName() {
        return Names.SELECT_CARDS_FROM_DECK;
    }
}
