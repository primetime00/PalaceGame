package com.kegelapps.palace;

import com.badlogic.gdx.graphics.Color;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.dealtasks.TapToStart;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.ChatBoxView;
import com.kegelapps.palace.graphics.TableView;
import com.kegelapps.palace.graphics.utils.HandUtils;
import com.kegelapps.palace.protos.LogicProtos;
import sun.rmi.runtime.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by keg45397 on 5/5/2016.
 */
public class CommentEngine implements ChatBoxView.ChatBoxStatusListener {

    private TableView mTable;
    private HandUtils.HandSide mHandSide;
    private ChatBoxView mChatbox;

    final private float duration_tap = 2.5f;
    final private float duration_burn = 2.0f;
    final private float duration_pickup = 2.5f;
    final private float duration_win = 2.5f;
    final private float duration_fail = 2.0f;


    private State mCurrentState;
    private boolean mChatShown, mIsChatting;

    public enum CommentType {
        TAP_DECK,
        BURN,
        PICKUP,
        WIN_1ST,
        WIN_2ND,
        WIN_3RD,
        UNPLAYABLE_FAIL,
    }

    enum CommentState {
        IDLE,
        POST,
        SHOW,
        SHOWING,
        HIDE,
        HIDING,
        REPEAT
    }
    private CommentState mState;

    private String mComment;
    private boolean hasComment;
    private float mCurrentTime;
    private float mStart, mEnd, mRepeat;
    private CommentType mType;

    public CommentEngine(TableView table, ChatBoxView cb) {
        this.mTable = table;
        mChatbox = cb;
        mCurrentState = null;
        cb.setChatStatusListener(this);
        mChatShown = false;
        mIsChatting = false;
        mState = CommentState.IDLE;

        createEvents();
    }

    private void createEvents() {
        //Triggered when the state engine changes state
        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.STATE_CHANGE) {
            @Override
            public void handle(Object params[]) {
                final String ename = "STATE_CHANGE";
                EventSystem.CheckParams(params, 1, ename);
                State s = (State) EventSystem.CheckParam(params[0], State.class, ename);
                if (s instanceof TapToStart) {
                    PostComment(CommentType.TAP_DECK);
                }
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.CARD_PLAY_SUCCESS) {
            @Override
            public void handle(Object[] params) {
                final String ename = "CARD_PLAY_SUCCESS";
                EventSystem.CheckParams(params, 3, ename);
                //are we playing a burn?
                boolean isBurnPlay = (boolean) EventSystem.CheckParam(params[2], Boolean.class, ename);
                if (isBurnPlay)
                    PostComment(CommentType.BURN);
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.PICK_UP_STACK) {
            @Override
            public void handle(Object[] params) {
                final String ename = "PICK_UP_STACK";
                EventSystem.CheckParams(params, 2, ename);
                int id = (int) EventSystem.CheckParam(params[0], Integer.class, ename);
                List<Card> cards = (List<Card>) EventSystem.CheckParam(params[1], List.class, ename);
                PostComment(CommentType.PICKUP);
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.CARDS_GONE) {
            @Override
            public void handle(Object[] params) {
                final String ename = "CARDS_GONE";
                EventSystem.CheckParams(params, 1, ename);
                int id = (int) EventSystem.CheckParam(params[0], Integer.class, ename);
                Hand h = mTable.getTable().GetHand(id);
                if (h.getType() == Hand.HandType.HUMAN)
                    return;
                LogicProtos.Placement gold = Logic.get().getStats().GetStats(CoinResource.CoinType.GOLD);
                LogicProtos.Placement silver = Logic.get().getStats().GetStats(CoinResource.CoinType.SILVER);
                LogicProtos.Placement bronze = Logic.get().getStats().GetStats(CoinResource.CoinType.BRONZE);
                if (!gold.hasHandID() || gold.getHandID() == id) {
                    PostComment(CommentType.WIN_1ST);
                    return;
                }
                if (!silver.hasHandID() || silver.getHandID() == id) {
                    PostComment(CommentType.WIN_2ND);
                    return;
                }
                if (!bronze.hasHandID() || bronze.getHandID() == id) {
                    PostComment(CommentType.WIN_3RD);
                    return;
                }
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.CARD_PLAY_FAILED) {
            @Override
            public void handle(Object[] params) {
                final String ename = "CARD_PLAY_FAILED";
                EventSystem.CheckParams(params, 2, ename);
                Hand hand =  (Hand) EventSystem.CheckParam(params[1], Hand.class, ename);
                Card card = (Card) EventSystem.CheckParam(params[0], Card.class, ename);
                if (mTable.getTable().GetUnplayableCards().contains(card)) //we tried to play and unplayable card
                    PostComment(CommentType.UNPLAYABLE_FAIL);
            }
        });


    }

    PlayerData.CommentData getData(Hand h, String key) {
        if (h.getIdentity().get().comments.containsKey(key))
            return h.getIdentity().get().comments.get(key);
        ArrayList<String> keyList = new ArrayList<>();
        for (String k : h.getIdentity().get().comments.keys()) {
            if (k.startsWith(key))
                keyList.add(k);
        }
        if (keyList.isEmpty())
            return null;
        int pick = (new Random()).nextInt(keyList.size());
        return h.getIdentity().get().comments.get(keyList.get(pick));

    }

    private void generateComment(CommentType type) {
        int currentId = mTable.getTable().GetCurrentPlayerId();
        int randomId = mTable.getTable().GetRandomCPUHandID();
        int randomOtherId = mTable.getTable().GetRandomCPUHandID(currentId);
        PlayerData.CommentData data;
        float randomValue = (float)Math.random();
        Hand h = mTable.getTable().GetHand(currentId);
        mHandSide = mTable.getSideFromHand(currentId);
        switch (type) {
            default:
            case TAP_DECK:
                data = getData(mTable.getTable().GetHand(randomId), "tap_deck");
                mHandSide = mTable.getSideFromHand(randomId);
                break;
            case BURN:
                if (h.getType() != Hand.HandType.CPU) {
                    mComment = "";
                    return;
                }
                data = getData(h, "burn");
                break;
            case PICKUP:
                boolean self = (new Random()).nextInt(10) >= 2;
                if (h.getType() != Hand.HandType.CPU) {
                    self = false;
                }
                if (self)
                    data = getData(h, "pickup_self" );
                else {
                    data = getData(mTable.getTable().GetHand(randomOtherId), "pickup_other");
                    mHandSide = mTable.getSideFromHand(randomOtherId);
                }
                break;
            case WIN_1ST:
                if (h.getType() != Hand.HandType.CPU) {
                    mComment = "";
                    return;
                }
                data = getData(h, "win_first");
                break;
            case WIN_2ND:
                if (h.getType() != Hand.HandType.CPU) {
                    mComment = "";
                    return;
                }
                data = getData(h, "win_second");
                break;
            case WIN_3RD:
                if (h.getType() != Hand.HandType.CPU) {
                    mComment = "";
                    return;
                }
                data = getData(h, "win_third");
                break;
            case UNPLAYABLE_FAIL:
                data = getData(mTable.getTable().GetHand(randomOtherId), "play_unable");
                mHandSide = mTable.getSideFromHand(randomOtherId);
                break;

        }
        mType = type;
        hasComment = data.rate >= randomValue;
        mComment = hasComment ? data.comment : "";
    }

    private void generateTimes(CommentType type) {
        switch (type) {
            default:
            case TAP_DECK:
                mStart = 0f;
                mEnd = duration_tap;
                mRepeat = 10f;
                mCurrentState = Logic.get().getCurrentState();
                break;
            case BURN:
                mStart = 0f;
                mEnd = duration_burn;
                mRepeat = 0f;
                mCurrentState = null;
                break;
            case PICKUP:
                mStart = 0f;
                mEnd = duration_pickup;
                mRepeat = 0f;
                mCurrentState = null;
                break;
            case WIN_1ST:
            case WIN_2ND:
            case WIN_3RD:
                mStart = 0f;
                mEnd = duration_win;
                mRepeat = 0f;
                mCurrentState = null;
                break;
            case UNPLAYABLE_FAIL:
                mStart = 0f;
                mEnd = duration_fail;
                mRepeat = 0f;
                mCurrentState = null;
        }
    }


    public boolean HasComment() {
        return hasComment;
    }
    public String GetComment() {
        return mComment;
    }

    public boolean PostComment(CommentType type) {
        generateComment(type);
        if (!HasComment())
            return false;
        generateTimes(type);
        mCurrentTime = 0;
        mState = CommentState.POST;
        return true;
    }


    public void update(float delta) {
        mCurrentTime += delta;
        switch (mState) {
            case IDLE:
                break;
            case POST:
                if (mCurrentTime >= mStart)
                    mState = CommentState.SHOW;
                break;
            case SHOW:
                mChatbox.showChat(mComment, mHandSide, Color.WHITE, false);
                mState = CommentState.SHOWING;
                break;
            case SHOWING:
                if (mCurrentTime > mEnd)
                    mState = CommentState.HIDE;
                else if (mCurrentState != null && Logic.get().getCurrentState() != mCurrentState) {
                    mState = CommentState.HIDE;
                    mRepeat = 0f;
                }
                break;
            case HIDE:
                mChatbox.closeChat(false);
                mState = CommentState.HIDING;
                break;
            case HIDING:
                if (mChatShown == false)
                    mState = CommentState.REPEAT;
                break;
            case REPEAT:
                if (mRepeat == 0f) {
                    mState = CommentState.IDLE;
                    break;
                }
                else if (mCurrentState != null && Logic.get().getCurrentState() != mCurrentState) {
                    mState = CommentState.IDLE;
                    mRepeat = 0f;
                    break;
                }
                if (mCurrentTime >= mRepeat+mEnd) {
                    Logic.log().info(String.format("Repeat %f, current %f, end %f", mRepeat, mCurrentTime, mEnd));
                    PostComment(mType);
                }
                break;
        }
    }

    @Override
    public void onOpened() {
        mChatShown = true;
    }

    @Override
    public void onClosed() {
        mChatShown = false;
    }


}
