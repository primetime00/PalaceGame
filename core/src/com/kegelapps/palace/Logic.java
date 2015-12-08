package com.kegelapps.palace;
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

    private BlockingQueue<Runnable> mQueue;

    private GameState mState;
    private int mNumberOfPlayers;

    private Table mTable;

    private List<Hand> mEndHands;


    private Deck mDeck;

    private Input mInput;

    public Logic() {
        mState = GameState.DEAL;
        mNumberOfPlayers = 4;
        mDeck = new Deck();
        mQueue = new LinkedBlockingQueue<Runnable>();

        mInput = new Input();

        mTable = new Table(mDeck, mNumberOfPlayers, mQueue);

        mEndHands = new ArrayList<>();
/*
        for (int i=0; i<mNumberOfPlayers; ++i) {
            Hand h = new Hand(i, i == 0 ? Hand.HandType.HUMAN : Hand.HandType.CPU, mDeck, mQueue);
            mHands.add(h);
            h.SetEndCardListener(this);
        }*/
    }

    public void Run() {
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
        mTable.DealNewGame();
    }

    public Input getInput() {
        return mInput;
    }
}
