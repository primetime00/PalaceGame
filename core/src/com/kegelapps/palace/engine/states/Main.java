package com.kegelapps.palace.engine.states;

import com.google.protobuf.Message;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Deck;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.protos.StateProtos;

/**
 * Created by keg45397 on 1/18/2016.
 */
public class Main extends State {

    enum GameState {
        START,
        DEAL,
        PLAY_FIRST_CARD,
        SELECT_END_CARDS,
        PLAY,
        MAX
    }

    private GameState mState;

    private Table mTable;
    private Deck mDeck;


    public Main(Table table) {
        super();
        mTable = table;
        mState = GameState.START;
        mDeck = mTable.getDeck();

        createStates();

    }

    private void createStates() {
        State s;
        s = mChildrenStates.add(Names.DEAL, this);
        s.setStateListener(new StateListener() {
            @Override
            public void onDoneState() {
                mState = GameState.PLAY_FIRST_CARD;
            }
        });

        s = mChildrenStates.add(Names.SELECT_END_CARDS, this);
        s.setStateListener(new StateListener() {
            @Override
            public void onContinueState() {
                mState = GameState.PLAY;
            }
        });

        s = mChildrenStates.add(Names.PLAY, this);
    }

    @Override
    protected boolean OnRun() {
        if (mTable == null)
            return true;
        if (mPaused)
            return false;
        switch (mState) {
            case START:
                Logic.get().setFastDeal(true);
                mDeck.Shuffle();
                mState = GameState.DEAL;
                break;
            case DEAL:
                mChildrenStates.get(Names.DEAL).Execute();
                break;
            case PLAY_FIRST_CARD:
                Logic.get().setFastDeal(false);
                mTable.DrawCard();
                if (mTable.GetTopPlayCard().getRank() == Card.Rank.TWO && mTable.getDeck().GetCards().size() > 0) {//we need to draw again!
                    mState = GameState.PLAY_FIRST_CARD;
                }
                else {
                    mState = GameState.SELECT_END_CARDS;
                }
                break;
            case SELECT_END_CARDS:
                mChildrenStates.get(Names.SELECT_END_CARDS).Execute();
                break;
            case PLAY:
                mChildrenStates.get(Names.PLAY).Execute();
                break;
        }
        return false;
    }

    @Override
    public Names getStateName() {
        return Names.MAIN;
    }

    @Override
    public Message WriteBuffer() {
        StateProtos.State stateProto = (StateProtos.State) super.WriteBuffer();
        StateProtos.MainState.Builder builder = StateProtos.MainState.newBuilder();
        builder.setMainState(mState.ordinal());
        stateProto = stateProto.toBuilder().setExtension(StateProtos.MainState.state, builder.build()).build();
        return stateProto;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
        StateProtos.State state = (StateProtos.State) msg;
        StateProtos.MainState mainState = ((StateProtos.State) msg).getExtension(StateProtos.MainState.state);
        mState = GameState.values()[mainState.getMainState()];
    }
}
