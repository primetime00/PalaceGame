package com.kegelapps.palace.engine.states;

import com.kegelapps.palace.engine.Deck;
import com.kegelapps.palace.engine.Table;

/**
 * Created by keg45397 on 1/18/2016.
 */
public class Main extends State {

    enum GameState {
        START,
        DEAL,
        PLAY_FIRST_CARD,
        SELECT_END_CARDS,
        PLAY,
        MAX
    }

    private GameState mState;

    private Table mTable;
    private Deck mDeck;

    //states
    Deal mDealState;
    SelectEndCards mSelectEndCardsState;


    public Main(Table table) {
        mTable = table;
        mState = GameState.START;
        mDeck = mTable.getDeck();

        mDealState = new Deal(mTable, new Runnable() {
            @Override
            public void run() {
                mState = GameState.PLAY_FIRST_CARD;
            }
        });
        mSelectEndCardsState = new SelectEndCards(this, mTable, new StateListener() {
            @Override
            public void onContinueState() {
                mState = GameState.PLAY;
            }
        });

    }

    @Override
    public boolean Run() {
        super.Run();
        if (mTable == null)
            return false;
        if (mPaused)
            return false;
        switch (mState) {
            case START:
                mDeck.Shuffle();
                mState = GameState.DEAL;
                break;
            case DEAL:
                mDealState.Run();
                break;
            case PLAY_FIRST_CARD:
                mTable.PlayCard();
                mState = GameState.SELECT_END_CARDS;
                break;
            case SELECT_END_CARDS:
                mSelectEndCardsState.Run();
                break;
        }
        return false;
    }
}
