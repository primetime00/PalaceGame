package com.kegelapps.palace.engine.states.playtasks;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.StateProtos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ryan on 1/21/2016.
 */
public class PlayCPUTurn extends PlayTurn {

    private long mTime;
    private boolean mPlayMultiple;
    private boolean mStalemate;


    public PlayCPUTurn(State parent, Table table) {
        super(parent, table);
        mStalemate = false;
    }

    @Override
    protected boolean DoPlayCard() {
        boolean res;
        Card card;
        if (!Logic.get().isSimulate()  && System.currentTimeMillis() - mTime < 500)
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
                if (mStalemate)
                    card = mHand.GetAI().SelectLowCard();
                else
                    card = mHand.GetAI().SelectPlayCard();
                if (card != null) {
                    res = PlayCard(card);
                    mPlayMode = CheckPlayMode();
                    return res;
                }
                break;
            case END:
                for (Card c : mHand.GetEndCards()) {
                    if (c == null)
                        continue;
                    playResult = Logic.get().ChallengeCard(c);
                    if (playResult != Logic.ChallengeResult.FAIL) {
                        res = PlayCard(c);
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
        Logic.get().log().info(String.format("My cards: %s", mHand.getID()));
        ArrayList<Card> blah = new ArrayList<>();
        blah.addAll(mHand.GetActiveCards());
        Collections.sort(blah);
        for (Card c : blah) {
            Logic.get().log().info(String.format("   %s", c.toString()));
        }
        if (CheckForStaleMate()) {
            mStalemate = true;
            Logic.get().log().info("THERE IS A STALEMATE!!!!!!!!!!!!!!!!!");
        }
        mPlayMode = CheckPlayMode();
        if (mTable.AllCardsUnplayable(mHand.getID())) {
            mTable.SkipTurn();
            return true; //the rest of your turn is skipped!
        }
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
                    if (mPlayMultiple)
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
        mPlayMultiple = Math.random() >= 1-mHand.getIdentity().get().getPlayMultiple();
    }

    @Override
    protected void OnEndRun() {
    }

    @Override
    public Names getStateName() {
        return Names.PLAY_CPU_TURN;
    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();
        StateProtos.PlayCPUTurnState.Builder builder = StateProtos.PlayCPUTurnState.newBuilder();
        builder.setPlayMultiple(mPlayMultiple);
        s = s.toBuilder().setExtension(StateProtos.PlayCPUTurnState.state, builder.build()).build();
        return s;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
        StateProtos.PlayCPUTurnState playState = ((StateProtos.State) msg).getExtension(StateProtos.PlayCPUTurnState.state);
        mPlayMultiple = playState.getPlayMultiple();
    }

    @Override
    public void Reset() {
        super.Reset();
        mStalemate = false;
    }
}
