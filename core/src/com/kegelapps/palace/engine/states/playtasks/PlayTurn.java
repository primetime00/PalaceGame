package com.kegelapps.palace.engine.states.playtasks;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.*;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.StateListener;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.StateProtos;

/**
 * Created by keg45397 on 2/2/2016.
 */
public class PlayTurn extends State {

    protected Table mTable;
    protected Hand mHand;
    protected TurnState mTurnState;

    protected PlayMode mPlayMode;

    protected enum TurnState {
        PLAY_CARD,
        BURN,
        SELECT_CARDS,
        PLAY_HIDDEN_CARD,
        CARDS_GONE,
        DONE
    }

    protected enum PlayMode {
        ACTIVE,
        END,
        HIDDEN,
        NO_CARDS
    }

    public PlayTurn(State parent, Table table) {
        super(parent);
        mTable = table;
        mTurnState = TurnState.PLAY_CARD;
        mPlayMode = PlayMode.ACTIVE;
    }

    @Override
    public void setID(int id) {
        super.setID(id);
        mHand = null;
        for (Hand h: mTable.getHands()) {
            if (h.getID() == id) {
                mChildrenStates.addState(Names.BURN_CARDS, this, id).setStateListener(new StateListener() {
                    @Override
                    public void onDoneState() {
                        mTurnState = TurnState.PLAY_CARD;
                        mPlayMode = CheckPlayMode();
                        if (mPlayMode == PlayMode.HIDDEN)
                            mTurnState = TurnState.PLAY_HIDDEN_CARD;
                        if (!mTable.GetUnplayableCards().isEmpty()) {
                            mTable.GetUnplayableCards().clear();
                            Logic.get().SortActiveCards(mHand);
                            Director.instance().getEventSystem().Fire(EventSystem.EventType.SORT_HAND, mHand);
                        }
                    }
                });

                mChildrenStates.addState(Names.SELECT_CARDS_FROM_DECK, this, id).setStateListener(new StateListener() {
                    @Override
                    public void onDoneState() {
                        mTurnState = TurnState.DONE;
                    }
                });

                mChildrenStates.addState(Names.PLAY_HIDDEN_CARD, this, id).setStateListener(new StateListener() {
                    @Override
                    public void onDoneState(Object result) {
                        if ( !(result instanceof Logic.ChallengeResult) )
                            throw new RuntimeException("Expecting a ChallengeResult");
                        Logic.ChallengeResult res = (Logic.ChallengeResult) result;
                        mPlayMode = CheckPlayMode();
                        switch (res) {
                            case SUCCESS: mTurnState = mHand.HasAnyCards() ? TurnState.DONE : TurnState.CARDS_GONE; break;
                            case FAIL: mTurnState = TurnState.PLAY_CARD; break;
                            case SUCCESS_AGAIN: mTurnState = mHand.HasAnyCards() ? TurnState.PLAY_HIDDEN_CARD : TurnState.CARDS_GONE; break;
                            case SUCCESS_BURN: mTurnState = TurnState.BURN; break;
                            default:break;
                        }
                    }
                });
                mChildrenStates.addState(Names.WIN, this, id).setStateListener(new StateListener() {
                    @Override
                    public void onDoneState() {
                        mTurnState = TurnState.DONE;
                    }
                });
                mHand = h;
                break;
            }
        }
    }

    @Override
    protected void OnFirstRun() {
        Logic.log().info(String.format("It is player %d turn", mID));
        Logic.log().info("--------------------------------------");
        mTurnState = TurnState.PLAY_CARD;
        mPlayMode = CheckPlayMode();
        if (mPlayMode == PlayMode.HIDDEN)
            mTurnState = TurnState.PLAY_HIDDEN_CARD;
    }

    protected boolean DoPlayCard() {
        return true;
    }

    protected boolean PlayCard(Card c) {
        return true;
    }

    protected PlayMode CheckPlayMode() {
        if (mHand.GetActiveCards().isEmpty()) {
            if (mHand.HasEndCards()) {//we have at least 1 end card to play
                return PlayMode.END;
            }
            else if (mHand.HasHiddenCards()) {
                return PlayMode.HIDDEN;
            }
            else if (!mHand.HasAnyCards())
                return PlayMode.NO_CARDS;
        }
        return PlayMode.ACTIVE;
    }

    @Override
    protected boolean OnRun() {
        if (mHand == null)
            throw new RuntimeException("PlayTurn need a proper hand!");

        switch (mTurnState) {
            case PLAY_CARD:
                if (DoPlayCard())
                    mTurnState = TurnState.SELECT_CARDS;
                if (!mHand.HasAnyCards()) //we are out of cards
                    mTurnState = TurnState.CARDS_GONE;
                return false;
            case BURN:
                mChildrenStates.getState(Names.BURN_CARDS, getID()).Execute();
                return false;
            case SELECT_CARDS:
                mChildrenStates.getState(Names.SELECT_CARDS_FROM_DECK, getID()).Execute();
                return false;
            case PLAY_HIDDEN_CARD:
                mChildrenStates.getState(Names.PLAY_HIDDEN_CARD, getID()).Execute();
                return false;
            case CARDS_GONE:
                mChildrenStates.getState(Names.WIN, getID()).Execute();
                return false;
            case DONE:
                if (!mTable.GetUnplayableCards().isEmpty()) {
                    mTable.GetUnplayableCards().clear();
                    Logic.get().SortActiveCards(mHand);
                    Director.instance().getEventSystem().Fire(EventSystem.EventType.SORT_HAND, mHand);
                }
                return true;
            default:
                throw new RuntimeException("PlayTurn State is in an unknown state?");
        }
    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();
        StateProtos.PlayTurnState.Builder builder = StateProtos.PlayTurnState.newBuilder();
        builder.setTurnState(mTurnState.ordinal());
        s = s.toBuilder().setExtension(StateProtos.PlayTurnState.state, builder.build()).build();
        return s;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
        StateProtos.PlayTurnState playState = ((StateProtos.State) msg).getExtension(StateProtos.PlayTurnState.state);
        mTurnState = TurnState.values()[playState.getTurnState()];

    }

    @Override
    public void Reset() {
        mTurnState = TurnState.PLAY_CARD;
        mPlayMode = PlayMode.ACTIVE;

        super.Reset();
    }
}
