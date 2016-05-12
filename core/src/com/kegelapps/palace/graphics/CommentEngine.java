package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.Color;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.states.SelectEndCards;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.dealtasks.DealCard;
import com.kegelapps.palace.engine.states.dealtasks.TapToStart;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.ChatBoxView;
import com.kegelapps.palace.graphics.TableView;
import com.kegelapps.palace.graphics.utils.HandUtils;
import com.kegelapps.palace.loaders.types.CoinResource;
import com.kegelapps.palace.loaders.types.PlayerData;
import com.kegelapps.palace.protos.LogicProtos;
import com.kegelapps.palace.utilities.Resettable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by keg45397 on 5/5/2016.
 */
public class CommentEngine implements ChatBoxView.ChatBoxStatusListener, Resettable {

    private TableView mTable;
    private HandUtils.HandSide mHandSide;
    private ChatBoxView mChatbox;

    final private float duration_tap = 2.5f;
    final private float duration_burn = 2.0f;
    final private float duration_pickup = 2.5f;
    final private float duration_win = 2.5f;
    final private float duration_select_end = 2.5f;
    final private float duration_dealing = 2.5f;
    final private float duration_fail = 2.0f;
    final private float duration_hidden = 2.5f;
    final private int max_rand = 10;


    private State mCurrentState;
    private boolean mChatShown, mIsChatting;
    private int mRandomValue;

    public enum CommentType {
        TAP_DECK,
        BURN,
        PICKUP,
        WIN_1ST,
        WIN_2ND,
        WIN_3RD,
        UNPLAYABLE_FAIL,
        SELECT_END_CARDS, DEALING, UNPLAYABLE_LOW, PLAY_HIDDEN_HARD, PLAY_HIDDEN,
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
    private float mStart, mDuration, mRepeat;
    private CommentType mType;

    public CommentEngine(TableView table, ChatBoxView cb) {
        this.mTable = table;
        mChatbox = cb;
        mCurrentState = null;
        cb.setChatStatusListener(this);
        mChatShown = false;
        mIsChatting = false;
        mState = CommentState.IDLE;
        mRandomValue = new Random().nextInt(max_rand);

        Director.instance().addResetter(this);

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
                if (s instanceof SelectEndCards) {
                    PostComment(CommentType.SELECT_END_CARDS);
                }
                if (s instanceof DealCard) {
                    Hand dealer = mTable.getTable().GetFirstDealHand();
                    Hand current = mTable.getTable().getHands().get(mTable.getTable().getCurrentDealTurn());
                    if (current.getID() == dealer.getID() && dealer.GetAvailableHiddenCardPosition() == -1) {
                        int sz = 0;
                        if (mRandomValue < 3) //0, 1, 2
                            sz = 0;
                        else if (mRandomValue < 6) //3, 4, 5
                            sz = 1;
                        else  //6, 7, 8, 9
                            sz = 2;
                        if (dealer.GetActiveCards().size() == sz)
                            PostComment(CommentType.DEALING);
                    }
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
                else
                    PostComment(CommentType.UNPLAYABLE_LOW);
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.ATTEMPT_HIDDEN_PLAY) {
            @Override
            public void handle(Object[] params) {
                final String ename = "ATTEMPT_HIDDEN_PLAY";
                EventSystem.CheckParams(params, 3, ename);
                int id = (int) EventSystem.CheckParam(params[0], Integer.class, ename);
                Card card = (Card) EventSystem.CheckParam(params[1], Card.class, ename);
                boolean dramatic = (boolean) EventSystem.CheckParam(params[2], Boolean.class, ename);
                if (!dramatic)
                    return;
                Card topCard = mTable.getPlayView().getInPlay().GetTopCard();
                if (topCard == null)
                    return;
                if (topCard.getRank().compareTo(Card.Rank.NINE) > 0)
                    PostComment(CommentType.PLAY_HIDDEN_HARD);
                else
                    PostComment(CommentType.PLAY_HIDDEN);
            }
        });





    }

    PlayerData.CommentData getData(Hand h, String key) {
        if (h.getIdentity().get().getComments().containsKey(key))
            return h.getIdentity().get().getComments().get(key);
        ArrayList<String> keyList = new ArrayList<>();
        for (String k : h.getIdentity().get().getComments().keys()) {
            if (k.startsWith(key))
                keyList.add(k);
        }
        if (keyList.isEmpty())
            return null;
        int pick = (new Random()).nextInt(keyList.size());
        return h.getIdentity().get().getComments().get(keyList.get(pick));

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
            case UNPLAYABLE_LOW:
                data = getData(mTable.getTable().GetHand(randomOtherId), "play_low");
                mHandSide = mTable.getSideFromHand(randomOtherId);
                break;
            case SELECT_END_CARDS:
                data = getData(mTable.getTable().GetHand(randomId), "select_end");
                mHandSide = mTable.getSideFromHand(randomId);
                break;
            case DEALING:
                data = getData(mTable.getTable().GetHand(randomId), "dealing");
                mHandSide = mTable.getSideFromHand(randomId);
                break;
            case PLAY_HIDDEN_HARD:
            case PLAY_HIDDEN:
                if (h.getType() != Hand.HandType.CPU) {
                    mComment = "";
                    return;
                }
                if (type == CommentType.PLAY_HIDDEN_HARD)
                    data = getData(mTable.getTable().GetHand(currentId), "hidden_hard");
                else
                    data = getData(mTable.getTable().GetHand(currentId), "hidden_mid");
                mHandSide = mTable.getSideFromHand(currentId);
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
                mDuration = duration_tap;
                mRepeat = 10f;
                mCurrentState = Logic.get().getCurrentState();
                break;
            case BURN:
                mStart = 0f;
                mDuration = duration_burn;
                mRepeat = 0f;
                mCurrentState = null;
                break;
            case PICKUP:
                mStart = 0f;
                mDuration = duration_pickup;
                mRepeat = 0f;
                mCurrentState = null;
                break;
            case WIN_1ST:
            case WIN_2ND:
            case WIN_3RD:
                mStart = 0f;
                mDuration = duration_win;
                mRepeat = 0f;
                mCurrentState = null;
                break;
            case UNPLAYABLE_FAIL:
            case UNPLAYABLE_LOW:
                mStart = 0f;
                mDuration = duration_fail;
                mRepeat = 0f;
                mCurrentState = null;
                break;
            case SELECT_END_CARDS:
                mStart = 3f;
                mDuration = duration_select_end;
                mRepeat = 0f;
                mCurrentState = null;
                break;
            case DEALING:
                mStart = 1f;
                mDuration = duration_dealing;
                mRepeat = 0f;
                mCurrentState = null;
                break;
            case PLAY_HIDDEN:
            case PLAY_HIDDEN_HARD:
                mStart = 0.5f;
                mDuration = duration_hidden;
                mRepeat = 0f;
                mCurrentState = null;
                break;
        }
    }


    public boolean HasComment() {
        return hasComment;
    }
    public String GetComment() {
        return mComment;
    }

    public boolean PostComment(CommentType type) {
        if (Logic.get().isSimulate())
            return false;
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
                if (mCurrentTime > mDuration +mStart)
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
                if (mCurrentTime >= mRepeat+ mDuration +mStart) {
                    Logic.log().info(String.format("Repeat %f, current %f, end %f", mRepeat, mCurrentTime, mDuration));
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

    @Override
    public void Reset(boolean newGame) {
        mChatbox.closeChat(true);
        mRandomValue = new Random().nextInt(max_rand);
    }



}
