package com.kegelapps.palace.engine;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.Resettable;
import com.kegelapps.palace.engine.states.Main;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.StateFactory;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.CardsProtos;
import com.kegelapps.palace.protos.LogicProtos;
import com.kegelapps.palace.protos.StateProtos;
import com.kegelapps.palace.protos.StatusProtos;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Ryan on 12/5/2015.
 */
public class Logic implements Serializer, Resettable{

    private static Logic mLogic;

    public enum ChallengeResult {
        FAIL,
        SUCCESS,
        SUCCESS_AGAIN,
        SUCCESS_BURN
    }

    public enum RequestType {
        SELECT_DECK,
        SELECT_PLAYCARDS
    }

    private int mNumberOfPlayers = 0;
    private boolean mSimulate = false;
    private Stats mStats;

    private boolean mFastDeal = false;

    //states
    private Main mMainState;

    private Table mTable;

    public Logic() {
        mStats = new Stats();
    }

    static public Logic get() {
        if (mLogic == null)
            mLogic = new Logic();
        return mLogic;
    }

    public void LoadStatus(StatusProtos.Status s) {
        if (mTable == null)
            mTable = new Table(s.getTable());
        else
            mTable.Load(s.getTable());
        StateFactory.get().SetTable(mTable);
        if (mMainState == null)
            mMainState = (Main) StateFactory.get().createState(State.Names.MAIN, null);
        else
            mMainState.Reset(false);
        StateFactory.get().ParseState(s.getMainState(), GetMainState());
        ReadBuffer(s.getLogic());
    }

    public void Pause(boolean pause) {
        //System.out.print("Logic system is " + (pause ? "Paused" : "UnPaused") + "\n");
        if (mMainState != null) {
            if (pause) {
                mMainState.pause();

            }
            else {
                mMainState.resume();
            }
        }
    }

    public void Poll() {
        if (mMainState == null)
            throw new RuntimeException("Logic need a MainState to continue.");
        if (mMainState.getStatus() == State.Status.PAUSED)
            return;
        mMainState.Execute();
    }


    public void PlayerSelectCard(Hand h, Card c) {
        if (mMainState == null)
            throw new RuntimeException("Logic need a MainState to continue.");
        if (mMainState.containsState(State.Names.PLACE_END_CARD))
            h.SelectEndCard(c);
        else if (mMainState.containsState(State.Names.PLAY))
            h.SelectPlayCard(c);
    }

    public void PlayerSelectEndCard(Hand hand, Card c) {
        if (mMainState == null)
            throw new RuntimeException("Logic need a MainState to continue.");
        if (mMainState.containsState(State.Names.PLAY))
            hand.SelectEndPlayCard(c);
    }

    public void PlayerSelectHiddenCard(Hand hand, Card c) {
        if (mMainState == null)
            throw new RuntimeException("Logic need a MainState to continue.");
        if (mMainState.containsState(State.Names.PLAY_HIDDEN_CARD))
            hand.SelectHiddenPlayCard(c);
    }



    public void PlayerSelectAllCards(Hand h, Card c) {
        if (mMainState == null)
            throw new RuntimeException("Logic need a MainState to continue.");
        else if (mMainState.containsState(State.Names.PLAY))
            h.SelectAllPlayCard(c);
    }

    public void PlayerUnSelectAllCards(Hand h) {
        if (mMainState == null)
            throw new RuntimeException("Logic need a MainState to continue.");
        else if (mMainState.containsState(State.Names.PLAY))
            h.UnSelectAllPlayCard();
    }



    public void PlayerUnselectCard(Hand h, Card c) {
        if (mMainState == null)
            throw new RuntimeException("Logic need a MainState to continue.");
        h.DeselectEndCard(c);
    }

    public void Request(State.Names stateName, RequestType type) {
        if (mMainState.containsState(stateName))
            mMainState.getState(stateName).UserSignal(type);
    }

    public boolean isFastDeal() {
            return mFastDeal;
    }

    public void setFastDeal(boolean mFastDeal) {
        this.mFastDeal = mFastDeal;
    }

    public ChallengeResult ChallengeCard(Card card) {
        Card top = mTable.GetTopPlayCard();
        switch (card.getRank()) {
            case TWO: return ChallengeResult.SUCCESS_AGAIN; //this card wins all the time and you get to go again!
            case TEN: return ChallengeResult.SUCCESS_BURN; //you burn the pile and get to go again!
            default:  return (top == null || top.getRank() == Card.Rank.TWO || card.compareTo(top) > -1) ? ChallengeResult.SUCCESS : ChallengeResult.FAIL;
        }
    }

    public ChallengeResult TestCard(Card card1, Card card2) {
        Card top = card2;
        switch (card1.getRank()) {
            case TWO: return ChallengeResult.SUCCESS_AGAIN; //this card wins all the time and you get to go again!
            case TEN: return ChallengeResult.SUCCESS_BURN; //you burn the pile and get to go again!
            default:  return (top == null || top.getRank() == Card.Rank.TWO || card1.compareTo(top) > -1) ? ChallengeResult.SUCCESS : ChallengeResult.FAIL;
        }
    }


    public State GetMainState() {
        return mMainState;
    }

    public Table GetTable() {
        return mTable;
    }

    public boolean CheckForSave() {
        StatusProtos.Status st;
        try {
            FileInputStream fs = new FileInputStream("test.dat");
            CodedInputStream istream = CodedInputStream.newInstance(fs);
            st = StatusProtos.Status.parseFrom(istream, StateFactory.get().getRegistry());
        } catch (Exception e) {
            return false;
        }
        LoadStatus(st);
        Director.instance().getEventSystem().FireLater(EventSystem.EventType.REPARENT_ALL_VIEWS);
        Director.instance().getEventSystem().FireLater(EventSystem.EventType.STATE_LOADED);
        return true;
    }

    public void Initialize() {
        if (mStats != null)
            mStats.Reset(false);
        mSimulate = false;
        //if we are rematching, don't load save
        if (mTable == null && mMainState == null)
            CheckForSave();
        if (mTable == null)
            mTable = new Table(new Deck(), mNumberOfPlayers);
        StateFactory.get().SetTable(mTable);
        if (mMainState == null)
            mMainState = (Main) StateFactory.get().createState(State.Names.MAIN, null);
    }

    public boolean SaveState() {
        if (GetTable() == null || GetMainState() == null)
            return false;
        StatusProtos.Status.Builder statBuilder = StatusProtos.Status.newBuilder();
        statBuilder.setTable((CardsProtos.Table) GetTable().WriteBuffer());
        statBuilder.setMainState((StateProtos.State) GetMainState().WriteBuffer());
        statBuilder.setLogic((LogicProtos.Logic) Logic.get().WriteBuffer());
        StatusProtos.Status st = statBuilder.build();
        try {
            FileOutputStream bs = new FileOutputStream("test.dat");
            CodedOutputStream output = CodedOutputStream.newInstance(bs);
            st.writeTo(output);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        System.out.print("Saved State!\n");
        return true;
    }

    public void SetNumberOfPlayers(int num) {
        if (mTable == null)
            mNumberOfPlayers = num;
    }

    public Stats getStats() {
        return mStats;
    }

    @Override
    public void ReadBuffer(Message msg) {
        LogicProtos.Logic logic = (LogicProtos.Logic) msg;
        mNumberOfPlayers = logic.getNumberOfPlayer();
        mStats.ReadBuffer(logic.getStats());
    }

    @Override
    public Message WriteBuffer() {
        LogicProtos.Logic.Builder logicBuilder = LogicProtos.Logic.newBuilder();
        logicBuilder.setNumberOfPlayer(mNumberOfPlayers);
        logicBuilder.setStats((LogicProtos.Stats) mStats.WriteBuffer());
        return logicBuilder.build();
    }

    @Override
    public void Reset(boolean newGame) {
        Initialize();
        if (newGame)
            mTable.generateNewIdentities();
    }

    public boolean isSimulate() {
        return mSimulate;
    }

    public void setSimulate(boolean mSimulate) {
        this.mSimulate = mSimulate;
    }
}
