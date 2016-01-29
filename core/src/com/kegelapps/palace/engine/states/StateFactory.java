package com.kegelapps.palace.engine.states;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.tasks.*;
import com.kegelapps.palace.protos.StateProtos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by keg45397 on 1/28/2016.
 */
public class StateFactory {

    private static StateFactory instance;

    private ExtensionRegistry mRegistry;
    private Table mTable;
    private Map<State.Names, ArrayList<State>> mStateMap;


    public StateFactory() {
        mRegistry = ExtensionRegistry.newInstance();
        mRegistry.add(StateProtos.MainState.state);
        mRegistry.add(StateProtos.DealState.state);
        mStateMap = new HashMap<>();
    }

    public void SetTable(Table t) {
        mTable = t;
    }

    public State createState(State.Names stateName, State parent, int id) {
        State s = null;
        if (mTable == null)
            return null;

        if (mStateMap.get(stateName) != null) {
            for (State item : mStateMap.get(stateName)) {
                if (item.getID() == id) {
                    return item;
                }
            }
        }

        switch (stateName) {
            case GENERIC:
                break;
            case MAIN:
                s = new Main(mTable);
            case DEAL:
                s = new Deal(parent, mTable);
            case DEAL_CARD:
                s = new DealCard(parent, mTable);
            case SELECT_END_CARDS:
                s = new SelectEndCards(parent, mTable);
            case PLACE_END_CARD:
                s = new PlaceEndCard(parent, mTable);
            case PLAY:
                s = new Play(parent, mTable);
            case PLAY_HUMAN_TURN:
                s = new PlayHumanTurn(parent, mTable);
            case PLAY_CPU_TURN:
                s = new PlayCPUTurn(parent, mTable);
            case TAP_DECK_START:
                s = new TapToStart(parent, mTable);
            default: break;
        }
        if (s != null)
            s.setID(id);
        return s;
    }

    public State createState(State.Names stateName, State parent) {
        return createState(stateName, parent, 0);
    }

    public static StateFactory get() {
        if (instance == null) {
            instance = new StateFactory();
        }
        return instance;
    }

    public ExtensionRegistry getRegistry() {
        return mRegistry;
    }

    public StateList ParseStateList(StateProtos.State parentState, State parent) {
        if (parentState.getChildrenStatesCount() == 0)
            return null;
        StateList sl = new StateList();
        for (StateProtos.State proto : parentState.getChildrenStatesList()) {
            State s = sl.add(State.Names.values()[proto.getType()], parent, proto.getId());
            s.ReadBuffer(proto);
            if (s.getStatus() != State.Status.DONE && s.getStatus() != State.Status.NOT_STARTED && parent != null)
                parent.addChild(s);
        }
        return sl;
    }

    public StateProtos.State WriteStateList(StateList mList, StateProtos.State parentState, State parent) {
        if (mList == null || mList.values().size() == 0)
            return parentState;
        StateProtos.State.Builder childBuilder = parentState.toBuilder();
        for (State state : mList.values()) {
            childBuilder.addChildrenStates((StateProtos.State) state.WriteBuffer());
        }
        parentState = childBuilder.build();
        return parentState;
    }




    public static class StateList extends HashMap<State.Names, State> {

        public StateList() {
            super();
        }

        public State add(State.Names stateName, State parent, int id) {
            State s = StateFactory.get().createState(stateName, parent, id);
            put(stateName, StateFactory.get().createState(stateName, parent, id));
            return s;
        }
        public State add(State.Names stateName, State parent) {
            return add(stateName, parent, 0);
        }
    }

    public void ParseState(Message msg, State parent, State instance, Table table) {
        StateProtos.State stateProto = (StateProtos.State) msg;
        State state;
        State.Names type = State.Names.values()[stateProto.getType()];
        switch (type) {
            case MAIN:
                instance.ReadBuffer(msg);
                break;
            case DEAL:
                instance.ReadBuffer(msg);
                break;
        }
        if (parent != null)
            parent.addChild(instance);
    }
}
