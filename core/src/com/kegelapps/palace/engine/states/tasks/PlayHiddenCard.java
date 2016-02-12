package com.kegelapps.palace.engine.states.tasks;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.CardsProtos;
import com.kegelapps.palace.protos.StateProtos;

/**
 * Created by keg45397 on 2/10/2016.
 */
public class PlayHiddenCard extends State {

    private Table mTable;
    private Hand mHand;
    private Card mPlayCard;
    private PlayHumanTurn mHumanState;
    private PlayCPUTurn mCPUTurn;
    private HiddenState mState;

    private enum HiddenState{
        ATTEMPT,
        CHECK,
        FAIL,
        SUCCESS
    }

    public PlayHiddenCard(State parent, Table table) {
        super(parent);
        mTable = table;
        mState = HiddenState.ATTEMPT;
        if (parent instanceof PlayHumanTurn)
            mHumanState = (PlayHumanTurn) parent;
        else if (parent instanceof PlayCPUTurn)
            mCPUTurn = (PlayCPUTurn) parent;
        else
            throw new RuntimeException("PlayHiddenCard State must be child of PlayHumanState or PlayCPUState.");
    }

    @Override
    public Names getStateName() {
        return Names.PLAY_HIDDEN_CARD;
    }

    @Override
    protected void OnEndRun() {
        super.OnEndRun();
    }

    @Override
    public void setID(int id) {
        super.setID(id);
        for (Hand h: mTable.getHands()) {
            if (h.getID() == id)
                mHand = h;
        }
    }

    @Override
    protected void OnFirstRun() {
        mState = HiddenState.ATTEMPT;
        if (mCPUTurn != null) {//we are the cpu, we will pick a random card!
            for (int i : mCPUTurn.RandomCardList()) { //here we pick a random card
                Card c = mHand.GetHiddenCards().get(i);
                if (c == null)
                    continue;
                mPlayCard = c;
                break;
            }
        }
    }

    @Override
    protected boolean OnRun() {
        if (mPlayCard != null) { //we have a card to play!
            switch (mState) {
                case ATTEMPT:
                    Director.instance().getEventSystem().Fire(EventSystem.EventType.ATTEMPT_HIDDEN_PLAY, mHand.getID(), mPlayCard);
                    mState = HiddenState.CHECK;
                    break;
                case CHECK:
                    Logic.ChallengeResult res = Logic.get().ChallengeCard(mPlayCard);
                    if (res == Logic.ChallengeResult.FAIL) {//we failed!
                        Director.instance().getEventSystem().Fire(EventSystem.EventType.FAILED_HIDDEN_PLAY, mPlayCard);
                        mState = HiddenState.FAIL;
                    } else {
                        Director.instance().getEventSystem().Fire(EventSystem.EventType.SUCCESS_HIDDEN_PLAY, mPlayCard);
                        mState = HiddenState.SUCCESS;
                    }
                    break;
                case FAIL:
                    //mTable.PickUpStack(mHand.getID());
                    if (mStateListener != null)
                        mStateListener.onDoneState(Logic.ChallengeResult.FAIL);
                    return true;
                case SUCCESS:
                    //mTable.AddPlayCard(mHand, mPlayCard);
                    if (mStateListener != null)
                        mStateListener.onDoneState(Logic.ChallengeResult.SUCCESS);
                    return true;
            }
        }
        return false;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
        StateProtos.PlayHiddenAttemptState playHidden = ((StateProtos.State) msg).getExtension(StateProtos.PlayHiddenAttemptState.state);
        if (playHidden.hasPlayCard())
            mPlayCard = Card.GetCard(Card.Suit.values()[playHidden.getPlayCard().getSuit()], Card.Rank.values()[playHidden.getPlayCard().getRank()]);
        if (playHidden.hasHiddenState())
            mState = HiddenState.values()[playHidden.getHiddenState()];

    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();
        StateProtos.PlayHiddenAttemptState.Builder builder = StateProtos.PlayHiddenAttemptState.newBuilder();
        if (mPlayCard != null)
            builder.setPlayCard((CardsProtos.Card) mPlayCard.WriteBuffer());
        builder.setHiddenState(mState.ordinal());
        s = s.toBuilder().setExtension(StateProtos.PlayHiddenAttemptState.state, builder.build()).build();
        return s;
    }
}
