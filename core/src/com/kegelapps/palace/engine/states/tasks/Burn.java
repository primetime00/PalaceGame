package com.kegelapps.palace.engine.states.tasks;

import com.google.protobuf.Message;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.graphics.TableView;
import com.kegelapps.palace.protos.TableProtos;

/**
 * Created by keg45397 on 2/3/2016.
 */
public class Burn extends State {
    Table mTable;

    public Burn(State parent, Table table) {
        super(parent);
        this.mTable = table;
    }

    @Override
    protected void OnFirstRun() {
        mTable.Burn();
    }

    @Override
    protected boolean OnRun() {
        return true;
    }

    @Override
    public Names getStateName() {
        return Names.BURN_CARDS;
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
