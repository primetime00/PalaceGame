package com.kegelapps.palace.engine;

import java.util.ArrayList;
import java.util.List;
import com.kegelapps.palace.engine.states.Deal;
import com.kegelapps.palace.engine.states.SelectEndCards;

/**
 * Created by Ryan on 12/5/2015.
 */
public class Logic {



    enum GameState {
        START,
        DEAL,
        PLAY_FIRST_CARD,
        SELECT_END_CARDS,
        PLAY,
        MAX
    }

    private static Logic mLogic;

    private GameState mState;
    private int mNumberOfPlayers = 0;

    //states
    Deal mDealState;
    SelectEndCards mSelectEndCardsState;

    private Table mTable;


    private List<Hand> mEndHands;

    private Deck mDeck;

    boolean mPaused;

    public Logic() {
        mState = GameState.START;
        mEndHands = new ArrayList<>();
        mPaused = false;
    }

    static public Logic get() {
        if (mLogic == null)
            mLogic = new Logic();
        return mLogic;
    }

    public void SetTable(Table table) {
        mTable = table;
        mDeck = table.getDeck();
        mNumberOfPlayers = table.getHands().size();
        mDealState = new Deal(mTable, new Runnable() {
            @Override
            public void run() {
                mState = GameState.PLAY_FIRST_CARD;
            }
        });
        mSelectEndCardsState = new SelectEndCards(mTable, new Runnable() {
            @Override
            public void run() {
                mState = GameState.PLAY;
            }
        });
    }

    public void Pause(boolean pause) {
        System.out.print("Logic system is " + (pause ? "Paused" : "UnPaused") + "\n");
        mPaused = pause;
    }


    public void Run() {
        if (mTable == null)
            return;
        while (true) {
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
                    //SelectEndCards();
                    break;
            }
        }
    }

    public void Poll() {
        if (mTable == null)
            return;
        if (mPaused)
            return;
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
    }


    public void PlayerSelectCard(Hand h, Card c) {
        if (mState == GameState.SELECT_END_CARDS)
            h.SelectEndCard(c);
    }

    public void PlayerUnselectCard(Hand h, Card c) {
        if (mState == GameState.SELECT_END_CARDS)
            h.DeselectEndCard(c);
    }


}
