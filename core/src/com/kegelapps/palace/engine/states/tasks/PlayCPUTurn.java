package com.kegelapps.palace.engine.states.tasks;

import com.google.protobuf.Message;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.protos.StateProtos;

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
        for (Card c : mHand.GetActiveCards()) {
            playResult = Logic.get().ChallengeCard(c);
            if (playResult != Logic.ChallengeResult.FAIL) {
                return PlayCard(c);
            }
        }
        System.out.print("I need to pick up a card!\n");
        return false; //probably need to pick up!
    }

    @Override
    protected boolean PlayCard(Card card) {
        Logic.ChallengeResult res = mTable.AddPlayCard(mHand, card);
        switch (res) {
            case SUCCESS:
                if (mHand.ContainsRank(card.getRank())) { //we could play another one...
                    if (Math.random() > 0.4f)
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
