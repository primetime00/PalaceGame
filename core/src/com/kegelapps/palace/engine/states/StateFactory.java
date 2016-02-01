package com.kegelapps.palace.engine.states;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.tasks.*;
import com.kegelapps.palace.protos.StateProtos;

import java.util.ArrayList;
import java.util.HashMap;
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
        mRegistry.add(StateProtos.SelectEndCardState.state);
        mRegistry.add(StateProtos.PlayState.state);
        mRegistry.add(StateProtos.DealCardState.state);
        mRegistry.add(StateProtos.PlaceEndCardState.state);
        mRegistry.add(StateProtos.PlayCPUTurnState.state);
        mRegistry.add(StateProtos.PlayHumanTurnState.state);
        mRegistry.add(StateProtos.TapToStartState.state);
        mStateMap = new HashMap<>();
    }

    public void SetTable(Table t) {
        mTable = t;
    }

    public State createState(State.Names stateName, State parent, int id) {
        State s = null;
        if (mTable == null)
            return null;

        ArrayList<State> stateList = mStateMap.get(stateName);
        if (stateList == null)
            stateList = new ArrayList<>();

        for (State item : stateList) {
            if (item.getID() == id) {
                return item;
            }
        }

        switch (stateName) {
            case GENERIC:
                break;
            case MAIN:
                s = new Main(mTable);
                break;
            case DEAL:
                s = new Deal(parent, mTable);
                break;
            case DEAL_HIDDEN_CARD:
                s = new DealCard(parent, mTable);
                ((DealCard)s).setHidden(true);
                break;
            case DEAL_SHOWN_CARD:
                s = new DealCard(parent, mTable);
                ((DealCard)s).setHidden(false);
                break;
            case SELECT_END_CARDS:
                s = new SelectEndCards(parent, mTable);
                break;
            case PLACE_END_CARD:
                s = new PlaceEndCard(parent, mTable);
                break;
            case PLAY:
                s = new Play(parent, mTable);
                break;
            case PLAY_HUMAN_TURN:
                s = new PlayHumanTurn(parent, mTable);
                break;
            case PLAY_CPU_TURN:
                s = new PlayCPUTurn(parent, mTable);
                break;
            case TAP_DECK_START:
                s = new TapToStart(parent, mTable);
                break;
            default: break;
        }
        if (s == null)
            return null;
        s.setID(id);
        stateList.add(s);
        mStateMap.put(stateName, stateList);
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
            State s = sl.addState(State.Names.values()[proto.getType()], parent, proto.getId());
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
        for (ArrayList<State> list: mList.values()) {
            for (State state : list) {
                childBuilder.addChildrenStates((StateProtos.State) state.WriteBuffer());
            }
        }
        parentState = childBuilder.build();
        return parentState;
    }




    public static class StateList extends HashMap<State.Names, ArrayList<State>> {

        public StateList() {
            super();
        }

        public State addState(State.Names stateName, State parent, int id) {
            State s = StateFactory.get().createState(stateName, parent, id);
            ArrayList<State> list = get(stateName);
            if (list == null)
                list = new ArrayList<>();
            boolean found = false;
            for (State item : list) {
                if (item.getID() == id) { //this item is already in the list
                    found = true;
                    break;
                }
            }
            if (!found)
                list.add(StateFactory.get().createState(stateName, parent, id));
            put(stateName, list);
            return s;
        }
        public State addState(State.Names stateName, State parent) {
            return addState(stateName, parent, 0);
        }

        public State getState(State.Names stateName, int id) {
            ArrayList<State> list = get(stateName);
            if (list == null)
                return null;
            for (State item : list) {
                if (item.getID() == id)
                    return item;
            }
            return null;
        }

        public State getState(State.Names stateName) { return getState(stateName, 0);}

    }

    public void ParseState(Message msg, State instance) {
        StateProtos.State stateProto = (StateProtos.State) msg;
        instance.ReadBuffer(msg);
    }
}