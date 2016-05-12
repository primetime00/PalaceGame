package com.kegelapps.palace.engine.states;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.utilities.Resettable;
import com.kegelapps.palace.engine.Deck;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.StateProtos;

/**
 * Created by keg45397 on 1/18/2016.
 */
public class Main extends State implements Resettable{

    enum GameState {
        START,
        DEAL,
        PLAY_FIRST_CARD,
        SELECT_END_CARDS,
        PLAY,
        GAME_OVER,
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
        Director.instance().addResetter(this);
    }

    public Main(StateProtos.State s, Table table) {
        super();
        mTable = table;
        createStates();
        ReadBuffer(s);
        Director.instance().addResetter(this);
    }

    public Main() {
        super();
        createStates();
        Director.instance().addResetter(this);
    }

    private void createStates() {
        State s;
        s = mChildrenStates.addState(Names.DEAL, this);
        s.setStateListener(new StateListener() {
            @Override
            public void onDoneState() {
                mState = GameState.PLAY_FIRST_CARD;
            }
        });

        s = mChildrenStates.addState(Names.SELECT_END_CARDS, this);
        s.setStateListener(new StateListener() {
            @Override
            public void onContinueState() {
                mState = GameState.PLAY;
            }
        });

        s = mChildrenStates.addState(Names.DRAW_PLAY_CARD, this);
        s.setStateListener(new StateListener() {
            @Override
            public void onDoneState() {
                mState = GameState.SELECT_END_CARDS;
            }
        });

        s = mChildrenStates.addState(Names.PLAY, this);
        s.setStateListener(new StateListener() {
            @Override
            public void onDoneState() {
                mState = GameState.GAME_OVER;
            }
        });
    }

    @Override
    protected boolean OnRun() {
        if (mTable == null)
            return true;
        if (mPaused)
            return false;
        switch (mState) {
            case START:
                mDeck.Shuffle();
                mState = GameState.DEAL;
                break;
            case DEAL:
                mChildrenStates.getState(Names.DEAL).Execute();
                break;
            case PLAY_FIRST_CARD:
                mChildrenStates.getState(Names.DRAW_PLAY_CARD).Execute();
                break;
            case SELECT_END_CARDS:
                mChildrenStates.getState(Names.SELECT_END_CARDS).Execute();
                break;
            case PLAY:
                mChildrenStates.getState(Names.PLAY).Execute();
                break;
            case GAME_OVER:
                Director.instance().getEventSystem().Fire(EventSystem.EventType.GAME_OVER);
                mPaused = true;
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
        StateProtos.MainState mainState = ((StateProtos.State) msg).getExtension(StateProtos.MainState.state);
        mState = GameState.values()[mainState.getMainState()];
        if (getStatus() == Status.PAUSED)
            setStatus(Status.ACTIVE);

    }

    @Override
    public void Reset(boolean newGame) {
        mPaused = false;
        mState = GameState.START;
        super.Reset();
    }
}
