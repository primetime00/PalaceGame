package com.kegelapps.palace.graphics;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.kegelapps.palace.CardResource;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.Resettable;
import com.kegelapps.palace.audio.SoundEvent;
import com.kegelapps.palace.loaders.types.SoundMap;
import com.kegelapps.palace.graphics.ui.common.StringMap;
import com.kegelapps.palace.scenes.GameScene;
import com.kegelapps.palace.Input;
import com.kegelapps.palace.animations.*;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.dealtasks.TapToStart;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.utils.HandUtils;
import com.kegelapps.palace.input.TablePanListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by keg45397 on 12/9/2015.
 */
public class TableView extends Group implements Input.BoundObject, Resettable, Disposable {

    private Table mTable;
    private DeckView mDeck;
    private InPlayView mPlayView;
    private Array<HandView> mHands;
    private Map<HandUtils.HandSide, HandView> mHandSide;
    private CardCamera mCamera;
    private int mCardHeight, mCardWidth;

    private Array<CardView> mCards;

    private TiledDrawable mBackground;

    final private float mDeckToActiveGap = 0.15f;

    private TextView mHelperText;

    public TableView(Table table, CardCamera cam) {
        mTable = table;
        mDeck = new DeckView(table.getDeck());
        mPlayView = new InPlayView(table.getInPlay());
        mCards = new Array<>();
        mHands = new Array<>();
        mCamera = cam;

        for (Hand h : mTable.getHands()) {
            mHands.add(new HandView(h));
        }
        //create all card views
        for (Card c : Card.GetAllCards()) {
            mCards.add(new CardView(c));
        }

        init();
    }

    private void init() {
        mHelperText = new TextView(Director.instance().getAssets().get("default_font", BitmapFont.class));
        mHelperText.setVerticalPadPercent(0.6f);

        mCardHeight = Director.instance().getAssets().get("cards", CardResource.class).getHeight();
        mCardWidth = Director.instance().getAssets().get("cards", CardResource.class).getWidth();

        mBackground = new TiledDrawable(((TextureAtlas) Director.instance().getAssets().get("ui.pack")).findRegion("tabletop"));

        onScreenSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        for (HandView hView : mHands){
            addActor(hView);
        }
        addActor(mDeck);
        addActor(mPlayView);



        createTableEvents();

        addListener(new TablePanListener(this));

        mHandSide = new HashMap<>();
        for (HandView h : mHands) {
            if (mHandSide.get(HandUtils.HandSide.SIDE_BOTTOM) == null)
                mHandSide.put(HandUtils.HandSide.SIDE_BOTTOM, h);
            else if (mHandSide.get(HandUtils.HandSide.SIDE_LEFT) == null)
                mHandSide.put(HandUtils.HandSide.SIDE_LEFT, h);
            else if (mHandSide.get(HandUtils.HandSide.SIDE_TOP) == null)
                mHandSide.put(HandUtils.HandSide.SIDE_TOP, h);
            else if (mHandSide.get(HandUtils.HandSide.SIDE_RIGHT) == null)
                mHandSide.put(HandUtils.HandSide.SIDE_RIGHT, h);
        }

        float w = (Director.instance().getViewWidth() - mCardWidth)/2.0f;
        float h = (Director.instance().getViewHeight() - mCardHeight)/2.0f;
        mDeck.setPosition(w,h);
        int width = Director.instance().getAssets().get("cards", CardResource.class).getWidth();

        mPlayView.setPosition(mDeck.getX()+width+(width*mDeckToActiveGap), mDeck.getY());
        mPlayView.setReferenceDeck(mDeck);
        Director.instance().addResetter(this);

    }

    @Override
    public void Reset(boolean newGame) {
        float x = Director.instance().getViewWidth() / 2.0f;
        float y = Director.instance().getViewHeight() / 2.0f;
        mHelperText.setText("");
        for (Actor c : getChildren()) {
            if (c instanceof CardView || c instanceof CoinView)
                c.remove();
        }

        mCamera.SetPosition(new Vector2(x, y), 1.0f, CardCamera.CameraSide.CENTER);
        for (HandView h : mHands) {
            h.Reset(false);
        }
        mPlayView.Reset(false);
        mDeck.Reset(false);
        CardView.Reset();
    }

    private void createTableEvents() {
        //Triggered when a card is drawn from the deck to the play card pile
        EventSystem.EventListener mDrawCardEventListener = new EventSystem.EventListener(EventSystem.EventType.DRAW_PLAY_CARD) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 1 || !(params[0] instanceof Card) )
                    throw new IllegalArgumentException("Invalid parameters for DRAW_PLAY_CARD");
                final CardView cardView = CardView.getCardView((Card) params[0]);
                //move this card to the table view first
                HandUtils.Reparent(TableView.this, cardView);
                cardView.setPosition(mDeck.getX(), mDeck.getY());
                cardView.setRotation(0.0f);

                if (cardView.getCard().getRank() == Card.Rank.TEN) {//this is a burn, lets zoom in!
                    CameraAnimation cameraZoomAnimation = (CameraAnimation) AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CAMERA).
                            setPause(true).setDescription("Zoom to play cards").setTable(TableView.this).setCamera(mCamera).
                            setCameraSide(CardCamera.CameraSide.UNKNOWN).setTweenCalculator(new CameraAnimation.ZoomToPlayCards(0.75f, 1.0f)).build();
                    cameraZoomAnimation.Start();
                }

                Director.instance().getAudioManager().QueueSound(new SoundEvent(Director.instance().getAssets().get("sounds", SoundMap.class).getRandom("cardSlideFirstDraw"), 0.35f));

                final AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                builder.setPause(true).setDescription("Drawing from deck to active").setTable(TableView.this).setCard(cardView)
                        /*
                        .addStatusListener(new Animation.AnimationStatusListener() {
                            @Override
                            public void onEnd(Animation animation) {
                                //mPlayView.OrganizeCards();
                            }
                        })*/
                        .setTweenCalculator(new CardAnimation.DrawToActive()).build().Start();

            }
        };
        Director.instance().getEventSystem().RegisterEvent(mDrawCardEventListener);

        //Triggered when a card is dealt from the deck
        EventSystem.EventListener mDealCardEventListener = new EventSystem.EventListener(EventSystem.EventType.DEAL_CARD) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length < 2 || !(params[0] instanceof Card) || !(params[1] instanceof Integer))
                    throw new IllegalArgumentException("Invalid parameters for DEAL_HIDDEN_CARD");
                CardView cardView = CardView.getCardView((Card) params[0]);
                final int id =  (int) params[1];

                float duration = params.length >= 3 && params[2] instanceof Float ? (float)params[2] : 0.5f;
                cardView.setSide(CardView.Side.BACK);

                HandUtils.Reparent(TableView.this, cardView);
                cardView.setPosition(mDeck.getX(), mDeck.getY());
                cardView.setRotation(0.0f);

                final AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                builder.setPause(true).setDescription("Dealing to a hand").setTable(TableView.this).setCard(cardView).setHandID(id)
                        .addStatusListener(new Animation.AnimationStatusListener() {
                            @Override
                            public void onEnd(Animation animation) {
                                HandUtils.Reparent(getHand(id), builder.getCard());
                            }
                        })
                        .setTweenCalculator(new CardAnimation.DealToHand()).build().Start();

                Director.instance().getAudioManager().QueueSound(new SoundEvent(Director.instance().getAssets().get("sounds", SoundMap.class).getRandom("cardSlide"), 0.02f));
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mDealCardEventListener);

        //Triggered when dealing the 7 active cards
        EventSystem.EventListener mDealFirstActiveCard = new EventSystem.EventListener(EventSystem.EventType.DEAL_ACTIVE_CARDS) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 2 || !(params[0] instanceof Integer) || !(params[1] instanceof Integer))
                    throw new IllegalArgumentException("Invalid parameters for DEAL_ACTIVE_CARDS");
                int round = (int) params[0];
                int player = (int) params[1];
                if (round != 3 && player != 0)
                    return;
                float duration = 1.5f;
                AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CAMERA);
                builder.setPause(false).setDescription("Move camera to bottom").setTable(TableView.this).
                        setCameraSide(CardCamera.CameraSide.BOTTOM).setCamera(mCamera).
                        setTweenCalculator(new CameraAnimation.MoveToSide(1.0f, duration)).build().Start();
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mDealFirstActiveCard);

        //Triggered when the state engine changes state
        EventSystem.EventListener mTapDeckEventListener = new EventSystem.EventListener(EventSystem.EventType.STATE_CHANGE) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 1 || !(params[0] instanceof State)) {
                    throw new IllegalArgumentException("Invalid parameters for STATE_CHANGE");
                }
                TapDeckToStart((params[0] instanceof TapToStart));
                mPlayView.OrganizeCards();
                CheckForQuickGame();
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mTapDeckEventListener);

        //Triggered when the player tries to play an invalid card
        EventSystem.EventListener mCardPlayFailed = new EventSystem.EventListener(EventSystem.EventType.CARD_PLAY_FAILED) {
            @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 2 || !(params[0] instanceof Card) || !(params[1] instanceof Hand)) {
                    throw new IllegalArgumentException("Invalid parameters for CARD_PLAY_FAILED");
                }
                Hand hand =  (Hand) params[1];

                CardView cardView = CardView.getCardView((Card) params[0]);
                cardView.toFront();

                final AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                builder.setPause(true).setDescription("Failed card").setTable(TableView.this).setCard(cardView).setHandID(hand.getID()).
                        killPreviousAnimation(cardView).setTweenCalculator(new CardAnimation.PlayFailedCard());

                if (hand.GetPlayCards().GetPendingCards().size() == 0) { //this is the last card?
                    builder.addStatusListener(new Animation.AnimationStatusListener() {
                        @Override
                        public void onEnd(Animation animation) {
                            mHands.get(builder.getHandID()).OrganizeCards(true);
                        }
                    });
                }
                builder.build().Start();
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mCardPlayFailed);

        //Triggered when the player plays a valid card
        EventSystem.EventListener mCardPlaySuccess = new EventSystem.EventListener(EventSystem.EventType.CARD_PLAY_SUCCESS) {
            @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 3 || !(params[0] instanceof Card) || !(params[1] instanceof Hand) || !(params[2] instanceof Boolean)) {
                    throw new IllegalArgumentException("Invalid parameters for CARD_PLAY_SUCCESS");
                }
                Hand hand =  (Hand) params[1];

                Card c = (Card) params[0];
                CardView cardView = CardView.getCardView(c);

                HandUtils.Reparent(TableView.this, cardView);

                //are we playing a burn?
                boolean isBurnPlay = (boolean) params[2];

                final AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                builder.setPause(true).setDescription(String.format("Success card %s", c.toString())).setTable(TableView.this).setCard(cardView).setHandID(hand.getID());
                builder.killPreviousAnimation(cardView);
                Animation cardAnimation = null, cameraZoomAnimation = null;
                final int pending = hand.GetPlayCards().GetPendingCards().size();

                if (!isBurnPlay || !hand.HasAnyCards()) {
                    cardAnimation = builder.setTweenCalculator(new CardAnimation.PlaySuccessCard()).build();
                    cardAnimation.addStatusListener(new Animation.AnimationStatusListener() {
                        @Override
                        public void onEnd(Animation animation) {
                            HandUtils.Reparent(mPlayView, builder.getCard());
                            builder.getTable().getHand(builder.getHandID()).OrganizeCards(true, true, false, false);
                            if (pending == 0) {
                                //mPlayView.OrganizeCards();
                            }
                        }
                    });
                    Director.instance().getAudioManager().QueueSound(new SoundEvent(Director.instance().getAssets().get("sounds", SoundMap.class).getRandom("cardSlide"), 0.1f));
                    cardAnimation.Start();
                }
                else {
                    cardAnimation = builder.setTweenCalculator(new CardAnimation.PlaySuccessBurnCard()).build();
                    cardAnimation.addStatusListener(new Animation.AnimationStatusListener() {
                        @Override
                        public void onEnd(Animation animation) {
                            HandUtils.Reparent(mPlayView, builder.getCard());
                            if (pending == 0) {
                                builder.getTable().getHand(builder.getHandID()).OrganizeCards(true);
                                //mPlayView.OrganizeCards();
                            }
                        }
                    });


                    cameraZoomAnimation = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CAMERA).
                            setPause(true).setDescription("Zoom to play cards").setTable(TableView.this).setCamera(mCamera).
                            setCameraSide(CardCamera.CameraSide.UNKNOWN).setTweenCalculator(new CameraAnimation.ZoomToPlayCards(0.75f, 1.0f)).build();

                    cameraZoomAnimation.Start();
                    Director.instance().getAudioManager().QueueSound(new SoundEvent(Director.instance().getAssets().get("sounds", SoundMap.class).getRandom("cardSlide"), 0.45f));
                    cardAnimation.Start();
                }
                TableView.this.getHand(hand.getID()).CheckDisabledCards();
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mCardPlaySuccess);

        //Triggered when the reparent required
        EventSystem.EventListener mReparentViews = new EventSystem.EventListener(EventSystem.EventType.REPARENT_ALL_VIEWS) {
            @Override
            public void handle(Object[] params) {

                for (HandView hv : mHands) {
                    hv.ReparentAllViews();
                }
                mPlayView.ReparentAllViews();
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mReparentViews);

        //Triggered when the state is loaded
        EventSystem.EventListener mLoadedState = new EventSystem.EventListener(EventSystem.EventType.STATE_LOADED) {
            @Override
            public void handle(Object[] params) {
                State s = Logic.get().GetMainState();
                if (s.containsState(State.Names.SELECT_END_CARDS) ||
                        s.containsState(State.Names.DEAL) ||
                        s.containsState(State.Names.PLAY) ||
                        s.containsState(State.Names.DRAW_PLAY_CARD)) {

                    HandUtils.HandSide side = HandUtils.HandSide.SIDE_UNKNOWN;
                    if (s.getState(State.Names.SELECT_END_CARDS) != null || s.getState(State.Names.DRAW_PLAY_CARD) != null || s.getState(State.Names.DEAL) != null)
                        side = HandUtils.HandSide.SIDE_BOTTOM;
                    else if (s.getState(State.Names.PLAY) != null) {
                        int id = getTable().getCurrentPlayTurn();
                        side = getSideFromHand(id);
                    }
                    Vector2 pos = HandUtils.GetHandPosition(TableView.this, side);
                    mCamera.SetPosition(pos, 1.0f, HandUtils.HandSideToCamera(side));
                }
                TapDeckToStart(s.containsState(State.Names.TAP_DECK_START));
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mLoadedState);

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.CHANGE_TURN) {
            @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 1 || !(params[0] instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid parameters for CHANGE_TURN");
                }

                int id = (int) params[0];
                float duration = 0.7f;

                HandUtils.HandSide side = HandUtils.IDtoSide(id, TableView.this);
                AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CAMERA);
                builder.setPause(true).setDescription("Move camera to current turn").setTable(TableView.this).
                        setCameraSide(HandUtils.HandSideToCamera(side)).setCamera(mCamera).
                        setTweenCalculator(new CameraAnimation.MoveToSide(1.0f, duration)).build().Start();
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.DRAW_TURN_END_CARDS) {
            @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 2 || !(params[0] instanceof Integer) || !(params[1] instanceof ArrayList)) {
                    throw new IllegalArgumentException("Invalid parameters for DRAW_TURN_END_CARDS");
                }
                int id = (int) params[0];
                HandView hand = null;
                for (HandView h : getHands()) {
                    if (h.getHand().getID() == id) {
                        hand = h;
                        break;
                    }
                }
                if (hand == null)
                    return;
                final HandView handView = hand;
                float delay = 0.1f;

                List<Card> cards = (List<Card>) params[1];
                for (int i = 0; i< cards.size(); ++i) {
                    Card c = cards.get(i);
                    CardView cardView = CardView.getCardView(c);
                    cardView.setSide(CardView.Side.BACK);
                    HandUtils.Reparent(TableView.this, cardView);
                    cardView.setPosition(mDeck.getX(), mDeck.getY());
                    cardView.setRotation(0.0f);
                    final AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                    builder.setPause(true).setDescription("Drawing and End card").setTable(TableView.this).setCard(cardView).setHandID(id);
                    builder.setStartDelay(delay, new Runnable() {
                        @Override
                        public void run() {
                            CardView cv = builder.getCard();
                            HandUtils.Reparent(TableView.this, cv);
                            cv.setSide(CardView.Side.BACK);
                            addActor(cv);
                        }
                    });
                    Director.instance().getAudioManager().QueueSound(new SoundEvent(Director.instance().getAssets().get("sounds", SoundMap.class).getRandom("cardPlace"), delay+0.0f));
                    delay+=0.4f; //add a delay of 0.4 seconds before the next card is dealt
                    if (i == cards.size()-1) { //last card
                        builder.addStatusListener(new Animation.AnimationStatusListener() {
                            @Override
                            public void onEnd(Animation animation) {
                                CardView cv = builder.getCard();
                                if (handView.getHand().getType() == Hand.HandType.HUMAN)
                                    cv.setSide(CardView.Side.FRONT);
                                HandUtils.Reparent(handView, cv);
                                handView.OrganizeCards(true, true, false, false);
                            }
                        });
                    } else {
                        builder.addStatusListener(new Animation.AnimationStatusListener() {
                            @Override
                            public void onEnd(Animation animation) {
                                CardView cv = builder.getCard();
                                if (handView.getHand().getType() == Hand.HandType.HUMAN)
                                    cv.setSide(CardView.Side.FRONT);
                                HandUtils.Reparent(handView, cv);
                            }
                        });
                    }
                    builder.setTweenCalculator(new CardAnimation.DrawEndTurnCard());
                    builder.build().Start();

                }
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.PICK_UP_STACK) {
            @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 2 || !(params[0] instanceof Integer) || !(params[1] instanceof ArrayList)) {
                    throw new IllegalArgumentException("Invalid parameters for PICK_UP_STACK");
                }
                int id = (int) params[0];
                HandView hand = null;
                for (HandView h : getHands()) {
                    if (h.getHand().getID() == id)
                        hand = h;
                }
                if (hand == null)
                    return;

                final HandView handView = hand;
                float delay = 0.05f;

                mPlayView.setHighlight(false);

                List<Card> cards = (List<Card>) params[1];
                //Collections.reverse(cards);
                if (cards.size() > 4) {
                    Director.instance().getAudioManager().QueueSound(new SoundEvent(Director.instance().getAssets().get("sounds", SoundMap.class).getRandom("pickup"), 0.2f));
                }
                for (int i = 0; i< cards.size(); ++i) {
                    Card c = cards.get(i);
                    CardView cardView = CardView.getCardView(c);
                    final AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                    builder.setPause(true).setDescription(String.format("Picking up the stack %d", i)).setTable(TableView.this).setCard(cardView).setHandID(id);
                    builder.setStartDelay(delay, new Runnable() {
                        @Override
                        public void run() {
                            CardView cv = builder.getCard();
                            HandUtils.Reparent(TableView.this, cv);
                        }
                    });
                    Director.instance().getAudioManager().QueueSound(new SoundEvent(Director.instance().getAssets().get("sounds", SoundMap.class).getRandom("cardSlide"), delay+0.1f));
                    delay+=0.1f; //add a delay of 0.1 seconds before the next card is dealt
                    if (i == cards.size()-1) { //last card
                        builder.addStatusListener(new Animation.AnimationStatusListener() {
                            @Override
                            public void onEnd(Animation animation) {
                                CardView cv = builder.getCard();
                                HandUtils.Reparent(handView, cv);
                                if (handView.getHand().getType() == Hand.HandType.HUMAN)
                                    cv.setSide(CardView.Side.FRONT);
                                else
                                    cv.setSide(CardView.Side.BACK);
                                handView.OrganizeCards(true, true, false, false, true);
                                //mPlayView.OrganizeCards();
                            }
                        });
                    } else {
                        builder.addStatusListener(new Animation.AnimationStatusListener() {
                            @Override
                            public void onEnd(Animation animation) {
                                CardView cv = builder.getCard();
                                HandUtils.Reparent(handView, cv);
                                if (handView.getHand().getType() == Hand.HandType.HUMAN)
                                    cv.setSide(CardView.Side.FRONT);
                            }
                        });
                    }
                    builder.setTweenCalculator(new CardAnimation.PickUpStack());
                    builder.build().Start();
                }
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.ATTEMPT_HIDDEN_PLAY) {
            @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 3 || !(params[0] instanceof Integer) || !(params[1] instanceof Card) || !(params[2] instanceof Boolean)) {
                    throw new IllegalArgumentException("Invalid parameters for ATTEMPT_HIDDEN_PLAY");
                }
                final float startDelay = 1.5f;
                int id = (int) params[0];
                Card card = (Card) params[1];
                HandView hand = null;
                for (HandView h : getHands()) {
                    if (h.getHand().getID() == id)
                        hand = h;
                }
                if (hand == null)
                    return;
                //find the hidden card we are playing
                Rectangle hiddenRect = null;
                for (int i=0; i<hand.getHand().GetHiddenCards().size(); ++i) {
                    Card c = hand.getHand().GetHiddenCards().get(i);
                    if (c == card)
                        hiddenRect = hand.getHiddenPosition(i);
                }

                CardView cardView = CardView.getCardView(card);
                Vector2 cardCenter = hiddenRect.getCenter(new Vector2());
                Vector2 pos = mPlayView.GetAbsoluteNextCardPosition();
                Vector2 playCenter = new Rectangle(pos.x, pos.y, cardView.getWidth(), cardView.getHeight()).getCenter(new Vector2());
                Vector2 nextPos = mPlayView.GetAbsoluteNextCardPosition();

                //lets bring the hand zindex to the front.
                mPlayView.toBack();
                boolean dramatic = (boolean) params[2];

                if (dramatic) {
                    Director.instance().getAudioManager().QueueSound(new SoundEvent(Director.instance().getAssets().get("sounds", SoundMap.class).getRandom("drama"),0.3f));
                    AnimationBuilder deckBuilder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CAMERA);
                    deckBuilder.setPause(true).setDescription("Zoom to in play").setTable(TableView.this).setCard(cardView).setHandID(id);
                    deckBuilder.setCamera(getCamera()).setCameraSide(CardCamera.CameraSide.UNKNOWN);
                    deckBuilder.setTweenCalculator(new CameraAnimation.MoveCamera(playCenter.x, playCenter.y, 0.6f, 1.5f));

                    AnimationBuilder zoomBuilder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CAMERA);
                    zoomBuilder.setPause(true).setDescription("Zoom to hidden card").setTable(TableView.this).setCard(cardView).setHandID(id);
                    zoomBuilder.setCamera(getCamera()).setCameraSide(CardCamera.CameraSide.UNKNOWN);
                    zoomBuilder.setTweenCalculator(new CameraAnimation.MoveCamera(cardCenter.x, cardCenter.y, 0.6f, startDelay));

                    zoomBuilder.setNextAnimation(deckBuilder.build());
                    zoomBuilder.build().Start();
                }


                //lets bring the card to the table
                HandUtils.Reparent(TableView.this, cardView);

                final AnimationBuilder cardBuilder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                cardBuilder.setPause(true).setDescription("Move card to play pile").setTable(TableView.this).setCard(cardView).setHandID(id);
                if (dramatic) {
                    cardBuilder.setTweenCalculator(new CardAnimation.MoveCard(nextPos.x, nextPos.y, 1.5f, true));
                    cardBuilder.setStartDelay(startDelay);
                    cardBuilder.setEndDelay(0.5f).addStatusListener(new Animation.AnimationStatusListener() {
                        @Override
                        public void onEnd(Animation animation) {
                            cardBuilder.getCard().setSide(CardView.Side.FRONT);
                        }
                    });
                }
                else {
                    Director.instance().getAudioManager().QueueSound(new SoundEvent(Director.instance().getAssets().get("sounds", SoundMap.class).getRandom("cardSlide"), 0.02f));
                    cardBuilder.setTweenCalculator(new CardAnimation.PlaySuccessCard());
                    cardBuilder.addStatusListener(new Animation.AnimationStatusListener() {
                        @Override
                        public void onEnd(Animation animation) {
                        }
                    });
                }
                cardBuilder.build().Start();
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.SUCCESS_HIDDEN_PLAY) {
            @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 2 || !(params[1] instanceof Card) || !(params[0] instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid parameters for SUCCESS_HIDDEN_PLAY");
                }
                if ( !(getStage() instanceof GameScene) ) {
                    throw new RuntimeException("Stage is not instance of GameScene.");
                }
                final float startDelay = 0.5f;
                int id = (int) params[0];
                Card card = (Card) params[1];
                HandView hand = null;
                for (HandView h : getHands()) {
                    if (h.getHand().getID() == id)
                        hand = h;
                }
                if (hand == null)
                    return;

                final CardView cardView = CardView.getCardView(card);

                //display a message of some sort depending on how successful
                if (!mPlayView.mInPlayCards.GetCards().isEmpty()) {
                    Card top = mPlayView.mInPlayCards.GetTopCard();
                    if (top.getRank() == card.getRank() && card.getRank() != Card.Rank.TWO) {
                        Director.instance().getAudioManager().QueueSound(new SoundEvent(Director.instance().getAssets().get("sounds", SoundMap.class).getRandom("success"),0.0f));
                        ((GameScene) getStage()).ShowMessage(StringMap.getString("lucky"), startDelay, Color.GREEN);
                    } else if (card.getRank() == Card.Rank.TWO && top.getRank() != Card.Rank.TWO) {
                        Director.instance().getAudioManager().QueueSound(new SoundEvent(Director.instance().getAssets().get("sounds", SoundMap.class).getRandom("success"),0.0f));
                        ((GameScene) getStage()).ShowMessage(StringMap.getString("thats_it"), startDelay, Color.GREEN);
                    } else if (card.getRank() != Card.Rank.TEN && top.getRank() != Card.Rank.TWO) {
                        Director.instance().getAudioManager().QueueSound(new SoundEvent(Director.instance().getAssets().get("sounds", SoundMap.class).getRandom("success"),0.0f));
                        ((GameScene) getStage()).ShowMessage(StringMap.getString("whew"), startDelay, Color.GREEN);
                    }
                }

                //no need to zoom back to turn if this is a burn!
                if (card.getRank() == Card.Rank.TEN) {
                    //mPlayView.toFront();
                    return;
                }

                if (!mPlayView.mInPlayCards.GetCards().isEmpty() && mPlayView.mInPlayCards.GetTopCard().getRank() != Card.Rank.TWO) {
                    AnimationBuilder zoomBuilder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CAMERA);
                    zoomBuilder.setPause(true).setDescription("Zoom back to turn").setTable(TableView.this).setCard(cardView).setHandID(id);
                    zoomBuilder.setCamera(getCamera()).setCameraSide(HandUtils.HandSideToCamera(HandUtils.IDtoSide(id, TableView.this)));
                    zoomBuilder.setTweenCalculator(new CameraAnimation.MoveToSide(1.0f, 1.0f));
                    zoomBuilder.setStartDelay(startDelay);
                    zoomBuilder.addStatusListener(new Animation.AnimationStatusListener() {
                        @Override
                        public void onEnd(Animation animation) {
                            mPlayView.OrganizeCards();
                            //mPlayView.toFront();
                        }
                    });
                    zoomBuilder.build().Start();
                }
                else
                {
                    AnimationBuilder pauseBuilder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.PAUSE);
                    pauseBuilder.setTweenCalculator(new Animation.PauseAnimation(0.3f)).setPause(true).
                    setDescription("Just pausing").
                    addStatusListener(new Animation.AnimationStatusListener() {
                        @Override
                        public void onEnd(Animation animation) {
                           HandUtils.Reparent(mPlayView, cardView);
                            //mPlayView.toFront();
                        }
                    });
                    pauseBuilder.build().Start();
                }
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.FAILED_HIDDEN_PLAY) {
            @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 2 || !(params[1] instanceof Card) || !(params[0] instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid parameters for FAILED_HIDDEN_PLAY");
                }
                if ( !(getStage() instanceof GameScene) ) {
                    throw new RuntimeException("Stage is not instance of GameScene.");
                }
                final float startDelay = 0.5f;
                int id = (int) params[0];
                Card card = (Card) params[1];
                HandView hand = null;
                for (HandView h : getHands()) {
                    if (h.getHand().getID() == id)
                        hand = h;
                }
                if (hand == null)
                    return;
                //display a message of failure
                Director.instance().getAudioManager().QueueSound(new SoundEvent(Director.instance().getAssets().get("sounds", SoundMap.class).getRandom("fail"),0.0f));
                ((GameScene)getStage()).ShowMessage(StringMap.getString("oh_no"), startDelay, Color.RED);

                CardView cardView = CardView.getCardView(card);
                AnimationBuilder zoomBuilder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CAMERA);
                zoomBuilder.setPause(true).setDescription("Zoom back to turn").setTable(TableView.this).setCard(cardView).setHandID(id);
                zoomBuilder.setCamera(getCamera()).setCameraSide(HandUtils.HandSideToCamera(HandUtils.IDtoSide(id, TableView.this)));
                zoomBuilder.setTweenCalculator(new CameraAnimation.MoveToSide(1.0f, 1.0f));
                zoomBuilder.setStartDelay(startDelay);
                zoomBuilder.addStatusListener(new Animation.AnimationStatusListener() {
                    @Override
                    public void onEnd(Animation animation) {
                        mPlayView.toFront();
                    }
                });
                zoomBuilder.build().Start();
            }
        });



    }

    public void onScreenSize(int w, int h) {
        setBounds(0,0,w,h);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setColor(getColor().r, getColor().g, getColor().b, getColor().a * parentAlpha);
        float x = -(Director.instance().getVirtualWidth()/2.0f - (mDeck.getX() + mDeck.getWidth()/2.0f));
        float y = -(Director.instance().getVirtualHeight()/2.0f - (mDeck.getY() + mDeck.getHeight()/2.0f));
        mBackground.draw(batch,x, y, Director.instance().getVirtualWidth(), Director.instance().getVirtualHeight());
        super.draw(batch, parentAlpha);
        if (mHelperText.getText().length() > 0)
            mHelperText.draw(batch, parentAlpha);
    }





    @Override
    public Rectangle getBounds() {
        return new Rectangle(0,0,800,480);
    }

    public Array<HandView> getHands() {
        return mHands;
    }

    public DeckView getDeck() {
        return mDeck;
    }

    public Table getTable() {
        return mTable;
    }

    public HandView getHandFromSide(HandUtils.HandSide side) {
        return mHandSide.get(side);
    }
    public HandUtils.HandSide getSideFromHand(int id) {
        for (Map.Entry s :mHandSide.entrySet()) {
            if ( ((HandView)s.getValue()).getHand().getID() == id)
                return (HandUtils.HandSide) s.getKey();
        }
        throw new RuntimeException("Hand side is unknown!");
    }

    public CardCamera getCamera() {
        return mCamera;
    }


    public InPlayView getPlayView() {
        return mPlayView;
    }

    private void TapDeckToStart(boolean enable) {
        if (enable) {
            mHelperText.setText(StringMap.getString("tap_start"));
            float x = mDeck.getX() - (mHelperText.getWidth() + mDeck.getWidth()) / 4.0f;
            float y = mDeck.getY() - mHelperText.getHeight() - (mDeck.getHeight() * 0.05f);
            mHelperText.setX(x);
            mHelperText.setY(y);
            mHelperText.setColor(Color.RED);
            return;
        }
        mHelperText.setText("");
    }

    public void PanCamera(float x, HandView hand) {
        if (Logic.get().GetMainState() == null)
            return;
        if (!Logic.get().GetMainState().containsState(State.Names.PLAY_HUMAN_TURN))
            return;
        x*=-0.5f;
        float left = hand.getActivePosition().getX();
        float right = hand.getActivePosition().getWidth() + left;
        CardCamera cam = getCamera();
        if (cam.position.x - (cam.viewportWidth /2.0f) < left && cam.position.x + (cam.viewportWidth /2.0f) > right)
            return;

        if (x < 0 && cam.position.x - (cam.viewportWidth/2.0f) > left)
            getCamera().position.add(x, 0, 0);
        else if (x > 0 && cam.position.x + (cam.viewportWidth/2.0f) < right)
            getCamera().position.add(x, 0, 0);

        if (cam.position.x - (cam.viewportWidth/2.0f) < left)
            cam.position.x = left +  (cam.viewportWidth/2.0f);
        else if (cam.position.x + (cam.viewportWidth/2.0f) > right)
            cam.position.x = right -  (cam.viewportWidth/2.0f);


    }

    @Override
    public String toString() {
        return "TableView";
    }

    public HandView getHand(int mHandID) {
        for (HandView h : getHands() ) {
            if (h.getHand().getID() == mHandID)
                return h;
        }
        return null;
    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        super.drawDebug(shapes);
        if (mHelperText.getText().length() > 0)
            mHelperText.drawDebug(shapes);

    }

    @Override
    public void dispose() {
        for (HandView h : getHands()) {
            h.dispose();
        }
    }

    public void SimulateGame(Hand hand, float duration) {
        if (!Director.instance().getOptions().getQuick())
            return;
        CenterCamera(duration);
        getHand(hand.getID()).ReparentAllViews();
/*        if (hand.getType() == Hand.HandType.HUMAN) {
            CenterCamera(duration);
        } else {
            getHand(hand.getID()).ReparentAllViews();
        }*/
    }

    private void CenterCamera(float duration) {
        float x = Director.instance().getViewWidth() / 2.0f;
        float y = Director.instance().getViewHeight() / 2.0f;
        CameraAnimation cameraCenter = (CameraAnimation) AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CAMERA).
                setPause(true).setDescription("Moving to center").setTable(TableView.this).setCamera(mCamera).
                setCameraSide(CardCamera.CameraSide.CENTER).setTweenCalculator(new CameraAnimation.MoveCamera(x, y, 1.0f, 0.5f)).
                setStartDelay(duration).killPreviousAnimation(mCamera).
                addStatusListener(new Animation.AnimationStatusListener() {
                    @Override
                    public void onEnd(Animation animation) {
                        Logic.get().setSimulate(true);
                        Director.instance().getTweenManager().update(100.0f);
                        for (BaseTween tm : Director.instance().getTweenManager().getObjects()) {
                            tm.kill();
                            AnimationFactory.get().pauseDecrement();
                        }
                    }
                }).build();
        cameraCenter.Start();
    }


    public void CheckForQuickGame() {
        Hand hand = null;
        if (!Logic.get().GetMainState().containsState(State.Names.PLAY)) {
            Logic.get().setSimulate(false);
            return;
        }
        if (mTable.isEveryPlayerCPU())
            hand = mHands.get(0).getHand();
        else {
            for (HandView h : getHands()) {
                if (h.getHand().getType() == Hand.HandType.HUMAN) {
                    if (!h.getHand().HasAnyCards()) {
                        hand = h.getHand();
                        break;
                    }
                }
            }
        }
        if (hand == null)
            return;
        if (!Director.instance().getOptions().getQuick())
            Logic.get().setSimulate(false);
        else {
            SimulateGame(hand, 0.1f);
/*            if (mCamera.GetSide() != CardCamera.CameraSide.CENTER)
                SimulateGame(hand, 0.1f);
            else
                Logic.get().setSimulate(true);*/
        }
    }
}
