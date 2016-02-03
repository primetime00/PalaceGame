package com.kegelapps.palace.engine.states.tasks;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.StateListener;
import com.kegelapps.palace.events.EventSystem;
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
    protected boolean OnRun() {
        Logic.ChallengeResult playResult = Logic.ChallengeResult.FAIL;
        if (mHand == null)
            return true;
        if (System.currentTimeMillis() - mTime < 1000)
            return false;

        //find a card to play
        for (Card c : mHand.GetActiveCards()) {
            Card topCard = mTable.GetTopPlayCard();
            playResult = Logic.get().ChallengeCard(c);
            if (playResult != Logic.ChallengeResult.FAIL) {
                PlayCard(c);
                break;
            }
        }
        switch (playResult) {
            case FAIL:
                System.out.print("CPU NEED TO PICK UP!");
                return false;
            case SUCCESS:
                return true;
            case SUCCESS_AGAIN:
                return false;
            case SUCCESS_BURN:
                return false;
        }
        return false;
    }

    private boolean PlayCard(Card card) {
        Card current = mTable.GetTopPlayCard();
        Logic.ChallengeResult res = mTable.AddPlayCard(mHand, card);
        switch (res) {
            case SUCCESS:
                return true;
            case SUCCESS_AGAIN:
                return false;
            case SUCCESS_BURN:
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
    public Names getStateName() {
        return Names.PLAY_CPU_TURN;
    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();

        StateProtos.PlayCPUTurnState.Builder builder = StateProtos.PlayCPUTurnState.newBuilder();
        s = s.toBuilder().setExtension(StateProtos.PlayCPUTurnState.state, builder.build()).build();
        return s;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
        StateProtos.PlayCPUTurnState selectEndCardState = ((StateProtos.State) msg).getExtension(StateProtos.PlayCPUTurnState.state);
    }

}
