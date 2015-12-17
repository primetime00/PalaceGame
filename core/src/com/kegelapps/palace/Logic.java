package com.kegelapps.palace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryan on 12/5/2015.
 */
public class Logic implements Hand.EndCardsListener{

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
                    SelectEndCards();
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
                SelectEndCards();
                break;
        }
    }


    private void SelectEndCards() {
        mEndHands.addAll(mTable.getHands());
        for (Hand h : mTable.getHands()) {
            h.SetEndCardListener(this);
            h.SelectEndCards();
        }
    }

    @Override
    public void onEndCardDone(Hand hand) {
        mEndHands.remove(hand);
        if (mEndHands.size() == 0) {
            mState = GameState.PLAY;
        }
    }

    public void DealCards() {
        if (mTable == null)
            return;
        mTable.DealNewGame();
    }

    static class Deal {
        private Table mTable;
        private int mCurrentPlayer;
        private int mRound;
        private Runnable mDoneRunnable;

        public Deal(Table table, Runnable done) {
            mTable = table;
            mCurrentPlayer = 0;
            mRound = 0;
            mDoneRunnable = done;
        }

        public void Run() {
            if (mRound == 0 && mCurrentPlayer == 0) {
                System.out.print("Dealing cards...");
            }
            if (mRound < 3) {
                mTable.DealHiddenCard(mCurrentPlayer);
                mCurrentPlayer +=1;
                if (mCurrentPlayer >= 4) {
                    mCurrentPlayer = 0;
                    mRound += 1;
                }
            }
            else if (mRound < 10) {
                mTable.DealActiveCard(mCurrentPlayer);
                mCurrentPlayer +=1;
                if (mCurrentPlayer >= 4) {
                    mCurrentPlayer = 0;
                    mRound += 1;
                }
            }
            else {
                if (mDoneRunnable != null) {
                    mDoneRunnable.run();
                }
            }
        }
    }

}
