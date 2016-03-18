package com.kegelapps.palace.engine.states.playtasks;

import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ryan on 1/21/2016.
 */
public class PlayCPUTurn extends PlayTurn {

    private long mTime;


    public PlayCPUTurn(State parent, Table table) {
        super(parent, table);
    }

    @Override
    protected boolean DoPlayCard() {
        if (System.currentTimeMillis() - mTime < 500)
            return false;
        Logic.ChallengeResult playResult = Logic.ChallengeResult.FAIL;
        //find a card to play
        switch (mPlayMode) {
            default:
                throw new RuntimeException("I don't expect to be in this mode: " + mPlayMode);
            case HIDDEN:
                mTurnState = TurnState.PLAY_HIDDEN_CARD;
                return false;
            case ACTIVE:
                for (Card c : mHand.GetActiveCards()) {
                    playResult = Logic.get().ChallengeCard(c);
                    if (playResult != Logic.ChallengeResult.FAIL) {
                        boolean res = PlayCard(c);
                        mPlayMode = CheckPlayMode();
                        return res;
                    }
                }
                break;
            case END:
                for (Card c : mHand.GetEndCards()) {
                    if (c == null)
                        continue;
                    playResult = Logic.get().ChallengeCard(c);
                    if (playResult != Logic.ChallengeResult.FAIL) {
                        boolean res = PlayCard(c);
                        mPlayMode = CheckPlayMode();
                        return res;
                    }
                }
                break;
            case NO_CARDS:
                mTurnState = TurnState.CARDS_GONE;
                return false;
        }
        // Logic challenge failed!
        mTable.PickUpStack(mHand.getID());
        mPlayMode = CheckPlayMode();
        return false; //probably need to pick up!
    }


    public Card SelectHiddenCard() {
        List<Integer> res = new ArrayList<>();
        for (int i=0; i<mHand.GetHiddenCards().size(); ++i)
            res.add(i);
        if (mHand.getIdentity() == null) {
            Collections.shuffle(res);
            return selectFirstCard(res);
        }
        switch (mHand.getIdentity().get().getHiddenOrder()) {
            case -1: //stars from the back;
                Collections.reverse(res); break;
            case 0: // random
                Collections.shuffle(res); break;
            default: //reg order
                break;
        }
        return selectFirstCard(res);
    }

    private Card selectFirstCard(List<Integer> list) {
        for (int i : list) { //here we pick a random card
            Card c = mHand.GetHiddenCards().get(i);
            if (c == null)
                continue;
            return c;
        }
        return null;
    }

    @Override
    protected boolean PlayCard(Card card) {
        Logic.ChallengeResult res;
        res = mTable.AddPlayCard(mHand, card);
        switch (res) {
            case SUCCESS:
                if (mHand.ContainsRank(card.getRank())) { //we could play another one...
                    if (Math.random() > 0.0f)
                        return PlayCard(mHand.FindRank(card.getRank()));
                    return true;
                }
                return true;
            case SUCCESS_AGAIN:
                return false;
            case SUCCESS_BURN:
                mTurnState = TurnState.BURN;
                return false;
            default:
                return false;
        }
    }

    @Override
    protected void OnFirstRun() {
        super.OnFirstRun();
        mTime = System.currentTimeMillis();
    }

    @Override
    protected void OnEndRun() {
        System.out.print("CPU TURN END\n");
    }

    @Override
    public Names getStateName() {
        return Names.PLAY_CPU_TURN;
    }

}
