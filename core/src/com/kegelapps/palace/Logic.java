package com.kegelapps.palace;
import com.kegelapps.palace.events.LogicEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Ryan on 12/5/2015.
 */
public class Logic implements Hand.EndCardsListener{

    enum GameState {
        DEAL,
        SELECT_END_CARDS,
        PLAY,
        MAX
    }

    private static Logic mLogic;

    private GameState mState;
    private int mNumberOfPlayers = 0;

    private Table mTable;


    private List<Hand> mEndHands;

    private Deck mDeck;

    public Logic() {
        mState = GameState.DEAL;
        mEndHands = new ArrayList<>();
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
    }


    public void Run() {
        if (mTable == null)
            return;
        while (true) {
            switch (mState) {
                case DEAL:
                    mDeck.Shuffle();
                    DealCards();
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
        switch (mState) {
            case DEAL:
                mDeck.Shuffle();
                DealCards();
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

}
