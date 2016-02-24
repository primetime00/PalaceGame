package com.kegelapps.palace.engine.states.playtasks;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.CardsProtos;
import com.kegelapps.palace.protos.StateProtos;

/**
 * Created by Ryan on 1/21/2016.
 */
public class PlayHumanTurn extends PlayTurn {

    private Card mPlayCard;
    private boolean mDeckTapped;
    private boolean mPlayTapped;

    public PlayHumanTurn(State parent, Table table) {
        super(parent, table);
        mPlayCard = null;
        mDeckTapped = false;
    }

    @Override
    protected void OnFirstRun() {
        super.OnFirstRun();
        mPlayCard = null;
        mDeckTapped = false;
        mPlayTapped = false;

        //lets see if we can even play a card!  We might have to pick up!
        if (CheckForPossiblePlay() == false) { //we will be picking up the pile!
            Director.instance().getEventSystem().Fire(EventSystem.EventType.HIGHLIGHT_PLAY, true);
        }
        else {
            Director.instance().getEventSystem().Fire(EventSystem.EventType.HIGHLIGHT_PLAY, false);
        }

    }

    @Override
    protected boolean DoPlayCard() {
        //normal play state
        boolean hasPlayed = false;
        if (mHand == null)
            throw new RuntimeException("Hand is null.  It should not be");
        if (mDeckTapped) {
            mHand.GetPlayCards().Clear();
            return true;
        }
        if (mPlayTapped) {
            mTable.PickUpStack(mHand.getID());
            mPlayTapped = false;
            return false;
        }

        int pendingSize = mHand.GetPlayCards().GetPendingCards().size();
        if (pendingSize == 0)
            return false;
        while (mHand.GetPlayCards().GetPendingCards().size() > 0) {
            Card c = mHand.GetPlayCards().PopCard();
            Card activeCard = null;
            mPlayMode = CheckPlayMode();
            switch (mPlayMode) {
                case ACTIVE:
                    activeCard = mHand.GetActiveCards().get(mHand.GetActiveCards().indexOf(c));
                    break;
                case END:
                    activeCard = mHand.GetEndCards().get(mHand.GetEndCards().indexOf(c));
                    break;
            }
            if (mPlayCard == null)
                hasPlayed = PlayCard(activeCard);
            else {
                if (!SameAsLastCard(activeCard)) //we are playing a card that is different
                    hasPlayed = false;
                else
                    hasPlayed = PlayCard(activeCard);
            }
        }
        return hasPlayed;
    }

    private boolean CheckForPossiblePlay() {
        switch (mPlayMode) {
            case ACTIVE:
                for (Card c : mHand.GetActiveCards()) {
                    if (Logic.get().ChallengeCard(c) != Logic.ChallengeResult.FAIL)
                        return true;
                }
                break;
            case HIDDEN:
                return true;
            case END:
                for (Card c : mHand.GetEndCards()) {
                    if (c != null) {
                        if (Logic.get().ChallengeCard(c) != Logic.ChallengeResult.FAIL)
                            return true;
                    }
                }
                break;
        }
        return false;
    }

    @Override
    protected boolean PlayCard(Card activeCard) {
        Logic.ChallengeResult res = mTable.AddPlayCard(mHand, activeCard);
        switch (res) {
            case SUCCESS:
                mPlayCard = activeCard;
                if (!mHand.ContainsRank(activeCard.getRank())) { //this turn is over!
                    return true;
                }
                //we can possibly play another card so the turn isn't over!
                Director.instance().getEventSystem().Fire(EventSystem.EventType.HIGHLIGHT_DECK, true);
                break;
            case SUCCESS_AGAIN: //we played a 2, so we get to go again!
                mPlayCard = null;
                return false;
            case SUCCESS_BURN: //we've burned the deck, we go again!
                if (mHand.GetPlayCards().GetPendingCards().isEmpty()) //make sure we aren't playing multiple burns!
                    mTurnState = TurnState.BURN;
                mPlayCard = null;
                return false;
            default: //we failed, we shouldn't reach this state
                break;
        }
        return false;
    }

    private boolean SameAsLastCard(Card card) {
        if (mPlayCard.getRank() != card.getRank()) { //trying to add more than one card with different ranks
            Director.instance().getEventSystem().Fire(EventSystem.EventType.CARD_PLAY_FAILED, card, mHand);
            return false;
        }
        return true;
    }

    @Override
    protected void OnEndRun() {
        Director.instance().getEventSystem().Fire(EventSystem.EventType.HIGHLIGHT_DECK, false);
        mPlayCard = null;
        System.out.print("HUMAN TURN END\n");
    }

    @Override
    public void UserSignal() {
        if (CheckForPossiblePlay() == false) {//we need to pick up
            mPlayTapped = true;
        }
        else if (mPlayCard != null) {
            mDeckTapped = true;
        }
    }

    @Override
    public Names getStateName() {
        return Names.PLAY_HUMAN_TURN;
    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();

        StateProtos.PlayHumanTurnState.Builder builder = StateProtos.PlayHumanTurnState.newBuilder();
        if (mPlayCard != null)
            builder.setPlayCard((CardsProtos.Card) mPlayCard.WriteBuffer());
        builder.setDeckTapped(mDeckTapped);
        builder.setPlayTapped(mPlayTapped);
        s = s.toBuilder().setExtension(StateProtos.PlayHumanTurnState.state, builder.build()).build();
        return s;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
        StateProtos.PlayHumanTurnState playHumanState = ((StateProtos.State) msg).getExtension(StateProtos.PlayHumanTurnState.state);
        if (playHumanState.hasPlayCard())
            mPlayCard = Card.GetCard(Card.Suit.values()[playHumanState.getPlayCard().getSuit()], Card.Rank.values()[playHumanState.getPlayCard().getRank()]);
        if (playHumanState.hasDeckTapped())
            mDeckTapped = playHumanState.getDeckTapped();
        if (playHumanState.hasPlayTapped())
            mPlayTapped = playHumanState.getPlayTapped();

    }


}
