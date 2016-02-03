package com.kegelapps.palace.engine.states.tasks;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.events.EventSystem;

/**
 * Created by keg45397 on 2/2/2016.
 */
public class PlayTurn extends State {

    protected Table mTable;
    protected Hand mHand;

    public PlayTurn(State parent, Table table) {
        super(parent);
        mTable = table;
    }

    @Override
    public void setID(int id) {
        super.setID(id);
        mHand = null;
        for (Hand h: mTable.getHands()) {
            if (h.getID() == id) {
                mHand = h;
                break;
            }
        }
    }

    @Override
    protected void OnFirstRun() {
        Director.instance().getEventSystem().Fire(EventSystem.EventType.CHANGE_TURN, mHand.getID());
    }

    @Override
    public Message WriteBuffer() {
        return super.WriteBuffer();
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
    }



}
