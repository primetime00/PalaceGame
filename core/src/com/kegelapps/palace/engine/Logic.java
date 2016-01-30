package com.kegelapps.palace.engine;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.states.*;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.CardsProtos;
import com.kegelapps.palace.protos.StateProtos;
import com.kegelapps.palace.protos.StatusProtos;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Ryan on 12/5/2015.
 */
public class Logic {

    private static Logic mLogic;

    public enum LogicRequest {
        PLAY_START;
    }

    private int mNumberOfPlayers = 0;

    private boolean mFastDeal = false;

    //states
    private Main mMainState;

    private Table mTable;

    private boolean mPaused;

    public Logic() {
        mPaused = false;
    }

    static public Logic get() {
        if (mLogic == null)
            mLogic = new Logic();
        return mLogic;
    }

    public void LoadStatus(StatusProtos.Status s) {
        mTable = new Table(s.getTable());
        StateFactory.get().SetTable(mTable);
        mMainState = (Main) StateFactory.get().createState(State.Names.MAIN, null);
        StateFactory.get().ParseState(s.getMainState(), GetMainState());
    }

    public void Pause(boolean pause) {
        //System.out.print("Logic system is " + (pause ? "Paused" : "UnPaused") + "\n");
        if (mMainState != null) {
            if (pause)
                mMainState.pause();
            else
                mMainState.resume();
        }
        mPaused = pause;
    }

    public void Poll() {
        if (mMainState == null)
            return;
        if (mMainState.getStatus() == State.Status.PAUSED)
            return;
        mMainState.Execute();
    }


    public void PlayerSelectCard(Hand h, Card c) {
        if (mMainState == null)
            return;
        if (mMainState.containsState(State.Names.PLACE_END_CARD))
            h.SelectEndCard(c);
        else if (mMainState.containsState(State.Names.PLAY))
            h.SelectPlayCard(c);
    }

    public void PlayerUnselectCard(Hand h, Card c) {
        if (mMainState != null)
            h.DeselectEndCard(c);
    }

    public void Request(LogicRequest req, State.Names stateName) {
        switch (req) {
            default:
            case PLAY_START:
                if (mMainState.containsState(stateName))
                    mMainState.getState(stateName).UserSignal();
                break;
        }
    }

    public boolean isFastDeal() {
            return mFastDeal;
    }

    public void setFastDeal(boolean mFastDeal) {
        this.mFastDeal = mFastDeal;
    }

    public boolean TestCard(Card card) {
        Card top = mTable.GetTopPlayCard();
        if (top == null)
            return false;
        return card.compareTo(top) > -1;
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
        return true;
    }

    public void SetNumberOfPlayers(int num) {
        if (mTable == null)
            mNumberOfPlayers = num;
    }


}
