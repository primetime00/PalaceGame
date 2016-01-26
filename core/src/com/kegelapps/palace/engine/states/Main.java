package com.kegelapps.palace.engine.states;

import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Deck;
import com.kegelapps.palace.engine.Logic;
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
    Play mPlayState;


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
        mPlayState = new Play(this, mTable, null);

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
                Logic.get().setFastDeal(true);
                mDeck.Shuffle();
                mState = GameState.DEAL;
                break;
            case DEAL:
                mDealState.Run();
                break;
            case PLAY_FIRST_CARD:
                Logic.get().setFastDeal(false);
                mTable.DrawCard();
                if (mTable.GetTopPlayCard().getRank() == Card.Rank.TWO && mTable.getDeck().GetCards().size() > 0) {//we need to draw again!
                    mState = GameState.PLAY_FIRST_CARD;
                }
                else {
                    mState = GameState.SELECT_END_CARDS;
                }
                break;
            case SELECT_END_CARDS:
                mSelectEndCardsState.Run();
                break;
            case PLAY:
                mPlayState.Run();
                break;
        }
        return false;
    }

    @Override
    public Names getStateName() {
        return Names.MAIN;
    }
}
