package com.kegelapps.palace.engine.states.playtasks;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.events.EventSystem;

/**
 * Created by Ryan on 2/23/2016.
 */
public class Win extends State {
    Table mTable;

    public Win(State parent, Table table) {
        super(parent);
        this.mTable = table;
    }

    @Override
    protected void OnFirstRun() {

    }

    @Override
    protected boolean OnRun() {
        Logic.get().getStats().DefineWinner(getID());
        Director.instance().getEventSystem().Fire(EventSystem.EventType.CARDS_GONE, getID());
        return true;
    }

    @Override
    public Names getStateName() {
        return Names.WIN;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
    }

    @Override
    public Message WriteBuffer() {
        return super.WriteBuffer();
    }

}
