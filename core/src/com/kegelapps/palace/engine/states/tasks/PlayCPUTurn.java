package com.kegelapps.palace.engine.states.tasks;

import com.google.protobuf.Internal;
import com.google.protobuf.Message;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.protos.StateProtos;

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
        }
        // Logic challenge failed!
        mTable.PickUpStack(mHand.getID());
        return false; //probably need to pick up!
    }

    public List<Integer> RandomCardList() {
        List<Integer> res = new ArrayList<>();
        for (int i=0; i<mHand.GetHiddenCards().size(); ++i)
            res.add(i);
        Collections.shuffle(res);
        return res;
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
