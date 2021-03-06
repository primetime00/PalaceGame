package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kegelapps.palace.loaders.types.CardResource;
import com.kegelapps.palace.loaders.types.CoinResource;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.utilities.Resettable;
import com.kegelapps.palace.audio.SoundEvent;
import com.kegelapps.palace.loaders.types.SoundMap;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.graphics.ui.common.StringMap;
import com.kegelapps.palace.scenes.GameScene;
import com.kegelapps.palace.animations.*;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.utils.HandUtils;
import com.kegelapps.palace.input.CardGestureListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keg45397 on 12/9/2015.
 */
public class HandView extends Group implements ReparentViews, Resettable, Disposable {

    private Hand mHand;
    private Rectangle mHiddenPositions[];
    private Rectangle mActivePosition;

    private TextView mPlayerName;

    private int mCardWidth, mCardHeight;

    private float mCardOverlapPercent;
    private float mEndCardOverlapPercent;

    private ActorGestureListener mGestureListener;

    public HandView(Hand hand) {
        super();
        assert(hand != null);
        mHand = hand;

        init();
    }

    private void init() {
        mCardHeight = Director.instance().getAssets().get("cards", CardResource.class).getHeight();
        mCardWidth = Director.instance().getAssets().get("cards", CardResource.class).getWidth();

        if (mHand.getType() == Hand.HandType.HUMAN)
            mCardOverlapPercent = 0.75f;
        else
            mCardOverlapPercent = 0.4f;
        mEndCardOverlapPercent = 0.05f;

        setupHiddenLayout();
        setupActiveLayout();


        createHandEvents();


        setupName();

        if (getHand().getType() == Hand.HandType.HUMAN)
            addListener(new CardGestureListener(this));
    }

    private void setupName() {
        if (getHand().getType() == Hand.HandType.CPU) {
            if (mPlayerName == null)
                mPlayerName = new TextView(Director.instance().getAssets().get("default_font", BitmapFont.class));
            mPlayerName.setColor(1,1,1,0.7f);
            mPlayerName.setScale(0.8f);
            mPlayerName.setText(getHand().getIdentity().get().getName());
            addActor(mPlayerName);
            mPlayerName.setOrigin(Align.center);
            switch (mHand.getID()) {
                default:
                case 0: //bottom
                    mPlayerName.setX(mHiddenPositions[1].getX() + mCardWidth + mPlayerName.getOriginX() + mPlayerName.getHeight());
                    mPlayerName.setY(mHiddenPositions[1].getY());
                    break;
                case 1: //left
                    mPlayerName.setX(mHiddenPositions[1].getX() + mCardWidth + mPlayerName.getHeight() + mPlayerName.getOriginX());
                    mPlayerName.setY(mHiddenPositions[1].getY() - mPlayerName.getHeight() /2.0f + mCardWidth/2.0f );
                    mPlayerName.setRotation(-90);
                    break;
                case 2: //top
                    mPlayerName.setX(mHiddenPositions[1].getX() + (mCardWidth - mPlayerName.getWidth()) /2.0f);
                    mPlayerName.setY(mHiddenPositions[1].getY() - (mPlayerName.getHeight() + (mCardHeight * 0.1f)));
                    break;
                case 3: //right
                    mPlayerName.setX(mHiddenPositions[1].getX() - mPlayerName.getOriginX() - mPlayerName.getHeight());
                    mPlayerName.setY(mHiddenPositions[1].getY() - mPlayerName.getHeight() /2.0f + mCardWidth/2.0f );
                    mPlayerName.setRotation(90);
                    break;
            }
        }
    }

    public float getCardOverlapPercent() {
        return mCardOverlapPercent;
    }
    public float getEndCardOverlapPercent() { return mEndCardOverlapPercent; }

    private void setupActiveLayout() {
        float w = Director.instance().getViewWidth();
        float h = Director.instance().getViewHeight();
        switch (mHand.getID()) {
            default:
            case 0: //bottom
                mActivePosition = new Rectangle(0, 0, w, mCardHeight);
                mActivePosition.setY(-mCardHeight - (mCardHeight*0.10f));
                break;
            case 1: //left
                mActivePosition = new Rectangle(0, 0, mCardHeight, h);
                mActivePosition.setX(-mCardHeight - (mCardHeight*0.10f));
                break;
            case 2: //top
                mActivePosition = new Rectangle(0, h-mCardHeight, w, mCardHeight);
                mActivePosition.setY(h+ (mCardHeight*0.10f));
                break;
            case 3: //right
                mActivePosition = new Rectangle(w-mCardHeight, 0, mCardHeight, h);
                mActivePosition.setX(w+ (mCardHeight*0.10f));
                break;
        }
    }

    public Hand getHand() {
        return mHand;
    }

    private void setupHiddenLayout() {
        mHiddenPositions = new Rectangle[3];
        float screenWidth = Director.instance().getViewWidth();
        float screenHeight = Director.instance().getViewHeight();
        float cardGap = mCardWidth * 0.1f;
        float hiddenWidth = mCardWidth * 3 + cardGap * 2;
        float startX = (screenWidth - hiddenWidth) / 2.0f;
        float startY = (screenHeight - hiddenWidth) / 2.0f;
        float nextX = mCardWidth + cardGap;
        for (int i=0; i<mHiddenPositions.length; ++i) {
            if (i == 0 || i == 2)
                mHiddenPositions[i] = new Rectangle(0, 0, mCardWidth, mCardHeight);
            else
                mHiddenPositions[i] = new Rectangle(0, 0, mCardHeight, mCardWidth);
        }
        switch (mHand.getID()) {
            default:
            case 0: //bottom
                mHiddenPositions[0].setPosition(startX, 0);
                mHiddenPositions[1].setPosition(startX + nextX, 0);
                mHiddenPositions[2].setPosition(startX + nextX + nextX, 0);
                break;
            case 1: //left
                mHiddenPositions[0].setPosition(0, startY);
                mHiddenPositions[1].setPosition(0, startY + nextX);
                mHiddenPositions[2].setPosition(0, startY + nextX + nextX);
                break;
            case 2: //top
                mHiddenPositions[0].setPosition(startX, screenHeight-mCardHeight);
                mHiddenPositions[1].setPosition(startX + nextX, screenHeight-mCardHeight);
                mHiddenPositions[2].setPosition(startX + nextX + nextX, screenHeight-mCardHeight);
                break;
            case 3: //right
                mHiddenPositions[0].setPosition(screenWidth - mCardHeight, startY);
                mHiddenPositions[1].setPosition(screenWidth - mCardHeight, startY + nextX);
                mHiddenPositions[2].setPosition(screenWidth - mCardHeight, startY + nextX + nextX);
                break;
        }
    }

    public Rectangle getHiddenPosition(int index) {
        if (index > 2 || index < 0)
            index = 0;
        return mHiddenPositions[index];
    }

    public Rectangle getActivePosition() {
        return mActivePosition;
    }


    private void createHandEvents() {

        EventSystem.EventListener mLayoutHiddenCardEventListener = new EventSystem.EventListener(EventSystem.EventType.LAYOUT_HIDDEN_CARD) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 3 || !(params[0] instanceof Card) || !(params[1] instanceof Integer) || !(params[2] instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid parameters for LAYOUT_HIDDEN_CARD");
                }

                int id = (int) params[1];
                if (getHand().getID() != id)
                    return;
                int pos = (int) params[2];

                CardView cardView = CardView.getCardView((Card) params[0]);

                if (!cardView.hasParent()) { //this card should have a parent, if not, we may have loaded the state
                    cardView.setSide(CardView.Side.BACK);
                    cardView.setPosition(mHiddenPositions[0].getX(), mHiddenPositions[0].getY());
                }
                HandUtils.Reparent(HandView.this, cardView);

                if (getParent() instanceof TableView) {
                    AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                    builder.setPause(false).setDescription(String.format("Lining up hidden card %s", cardView.getCard().toString())).setTable((TableView) getParent()).setCard(cardView).setHandID(getHand().getID())
                            .setTweenCalculator(new CardAnimation.LineUpHiddenCards(pos)).build().Start();
                }
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mLayoutHiddenCardEventListener);

        final EventSystem.EventListener mLayoutActiveCardEventListener = new EventSystem.EventListener(EventSystem.EventType.LAYOUT_ACTIVE_CARD) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 2 || !(params[0] instanceof Card) || !(params[1] instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid parameters for LAYOUT_ACTIVE_CARD");
                }

                int id = (int) params[1];
                if (getHand().getID() != id)
                    return;

                CardView cardView = CardView.getCardView((Card) params[0]);
                if (cardView.getParent() == null)
                    cardView.setPosition(mHiddenPositions[0].getX(), mHiddenPositions[0].getY());
                HandUtils.Reparent(HandView.this, cardView);
                cardView.setSide(mHand.getType() == Hand.HandType.HUMAN ? CardView.Side.FRONT : CardView.Side.BACK);
                //OrganizeCards(true);
                if (Logic.get().GetMainState().containsState(State.Names.SELECT_END_CARDS) && mHand.getType() == Hand.HandType.HUMAN)
                    Director.instance().getAudioManager().QueueSound(new SoundEvent(Director.instance().getAssets().get("sounds", SoundMap.class).getRandom("cardSlideFirstDraw"), 0.0f));
                OrganizeCards(true, true, false, false, true);
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mLayoutActiveCardEventListener);

        EventSystem.EventListener mSelectEndCardEventListener = new EventSystem.EventListener(EventSystem.EventType.SELECT_END_CARD) {
            @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 3 || !(params[0] instanceof Card) || !(params[1] instanceof Integer) || !(params[2] instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid parameters for SELECT_END_CARD");
                }
                int id = (int) params[1];
                if (getHand().getID() != id)
                    return;

                if ( !(getParent() instanceof TableView) ) {
                    throw new RuntimeException("SELECT_END_CARD requires a TableView parent.");
                }
                TableView table = (TableView)getParent();


                int pos = (int) params[2];

                CardView cardView = CardView.getCardView((Card) params[0]);
                HandUtils.Reparent(table, cardView);


                final AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                builder.setPause(false).setDescription("Selecting end cards").setTable(table).setCard(cardView).setHandID(getHand().getID());
                builder.addStatusListener(new Animation.AnimationStatusListener() {
                    @Override
                    public void onEnd(Animation animation) {
                        HandUtils.Reparent(builder.getTable().getHand(builder.getHandID()), builder.getCard());
                    }
                });

                builder.setTweenCalculator(new CardAnimation.SelectEndCard(pos)).build().Start();
                Director.instance().getAudioManager().QueueSound(new SoundEvent(Director.instance().getAssets().get("sounds", SoundMap.class).getRandom("cardSlideFirstDraw"), 0.00f));


                OrganizeCards(true, true, false, false, true);
                cardView.toFront();
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mSelectEndCardEventListener);

        EventSystem.EventListener mSelectMultipleCards = new EventSystem.EventListener(EventSystem.EventType.SELECT_MULTIPLE_CARDS) {
                @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 1 || !(params[0] instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid parameters for SELECT_MULTIPLE_CARDS");
                }
                if ( !(getParent() instanceof TableView) ) {
                    throw new RuntimeException("SELECT_MULTIPLE_CARDS requires a TableView parent.");
                }
                TableView table = (TableView)getParent();
                int id = (int) params[0];
                if (getHand().getID() != id)
                    return;

                SelectMultipleCards(table);
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mSelectMultipleCards);

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.UNSELECT_MULTIPLE_CARDS) {
            @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 1 || !(params[0] instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid parameters for SELECT_MULTIPLE_CARDS");
                }
                int id = (int) params[0];
                if (getHand().getID() != id)
                    return;

                if (getParent() instanceof TableView) {
                    for (Card c : mHand.GetPlayCards().GetAllCards()) {
                        CardView cardView = CardView.getCardView(c);
                        Director.instance().getTweenManager().killTarget(cardView);
                    }
                }
                OrganizeCards(true, true, true, true);
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.STATE_LOADED) {
            @Override
            public void handle(Object[] params) {
                if ( !(getParent() instanceof TableView) ) {
                    return;
                }
                SelectMultipleCards((TableView) getParent());

            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.CARDS_GONE){
            @Override
            public void handle(Object[] params) {
                final String ename = "CARDS_GONE";
                EventSystem.CheckParams(params, 1, ename);
                if ( !(getParent() instanceof TableView) ) {
                    throw new RuntimeException("CARDS_GONE requires a TableView parent.");
                }
                TableView table = (TableView)getParent();
                int id = (int) EventSystem.CheckParam(params[0], Integer.class, ename);
                if (getHand().getID() != id)
                    return;
                CoinView cv = createCoin();
                if (cv == null) {
                    throw new RuntimeException("Could not create a coin for hand " + mHand.getID());
                }
                CardCamera cam = table.getCamera();
                switch ((int)(Math.random() * 4)) {
                    case 0:
                        cv.setX( (cam.position.x - cam.viewportWidth/2.0f) - cv.getWidth());
                        cv.setY( (cam.position.y) );
                        break;
                    case 1:
                        cv.setX( (cam.position.x + cam.viewportWidth/2.0f) + cv.getWidth());
                        cv.setY( (cam.position.y) );
                        break;
                    case 2:
                        cv.setY( (cam.position.y - cam.viewportHeight/2.0f) - cv.getHeight());
                        cv.setX( (cam.position.x) );
                        break;
                    default:
                    case 3:
                        cv.setY( (cam.position.y + cam.viewportHeight/2.0f) + cv.getHeight());
                        cv.setX( (cam.position.x) );
                        break;
                }
                table.addActor(cv);
                Director.instance().getAudioManager().QueueSound(new SoundEvent(Director.instance().getAssets().get("sounds", SoundMap.class).getRandom("coin"),0.4f));
                final AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.COIN);
                builder.setTable(table).setCamera(table.getCamera()).setPause(true).setCoin(cv).setDescription("Flying in coin.");
                builder.setHandID(id).setTweenCalculator(new CoinAnimation.FlyInCoin(1.5f, true));
                builder.addStatusListener(new Animation.AnimationStatusListener() {
                    @Override
                    public void onEnd(Animation animation) {
                        Director.instance().getAudioManager().QueueSound(new SoundEvent(Director.instance().getAssets().get("sounds", SoundMap.class).getRandom("applause"),0.0f));
                        String name = mHand.GetAI() != null ? mHand.getIdentity().get().getName() : StringMap.getString("you");
                        HandUtils.Reparent(HandView.this, builder.getCoin());
                        switch (builder.getCoin().getType()) {
                            case GOLD:
                                if (mHand.getType() == Hand.HandType.CPU)
                                    ((GameScene)getStage()).ShowMessage(String.format("%s %s", mHand.getIdentity().get().getName(), StringMap.getString("wins")), 1.0f, Color.GOLD, true);
                                else
                                    ((GameScene)getStage()).ShowMessage(StringMap.getString("you_win"), 1.0f, Color.GOLD, true);
                                break;
                            case SILVER: ((GameScene)getStage()).ShowMessage(String.format("%s: %s", StringMap.getString("2nd_place"), name), 1.0f, Color.GRAY, true); break;
                            case BRONZE: ((GameScene)getStage()).ShowMessage(String.format("%s: %s", StringMap.getString("3rd_place"), name), 1.0f, Color.BROWN, true); break;
                        }
                    }
                });

                builder.build().Start();
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.SORT_HAND){
            @Override
            public void handle(Object[] params) {
                final String ename = "SORT_HAND";
                EventSystem.CheckParams(params, 1, ename);
                Hand h = (Hand) EventSystem.CheckParam(params[0], Hand.class, ename);
                if (getHand().getID() != h.getID())
                    return;
                if ( !(getParent() instanceof TableView) ) {
                    throw new RuntimeException("SORT_HAND requires a TableView parent.");
                }
                TableView table = (TableView)getParent();
                OrganizeCards(true, true, false, false, true);
            }
        });

    }

    private CoinView createCoin() {
        final CoinResource.CoinType ct = Logic.get().getStats().GetCoinType(mHand.getID());
        if (ct == null)
            return null;
        CoinView cv = new CoinView(ct);
        Vector2 center = mHiddenPositions[1].getCenter(new Vector2());
        cv.setPosition(center.x - cv.getWidth()/2.0f, center.y- cv.getHeight()/2.0f);
        cv.setScale(0.5f);
        return cv;
    }

    private void SelectMultipleCards(TableView table) {
        for (Card c : mHand.GetPlayCards().GetAllCards()) {
            CardView cardView = CardView.getCardView(c);
            AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
            builder.setPause(false).setDescription("Selecting multiple cards").setTable(table).setCard(cardView).setHandID(getHand().getID())
                    .setTweenCalculator(new CardAnimation.SelectPendingCard()).build().Start();
        }
    }

    public void OrganizeCards(boolean animation) {
        OrganizeCards(animation, true, true, true, false);
    }
    public void OrganizeCards(boolean animation, boolean pause) {
        OrganizeCards(animation, true, true, true, pause);
    }
    public void OrganizeCards(boolean animation, boolean active, boolean hidden, boolean end) {OrganizeCards(animation, active, hidden, end, false);}

    public void OrganizeCards(boolean animation, boolean active, boolean hidden, boolean end, boolean pause) {
        int zIndex = 0;
        int size = getHand().GetActiveCards().size();
        Rectangle r = getActivePosition();
        float width;
        if (Logic.get().isSimulate()) {
            animation = false;
            active = true;
            hidden = true;
            end = true;
            pause = false;
        }
        if (size > 0) {
            CardView c = CardView.getCardView(getHand().GetActiveCards().get(0));
            width = (((size - 1) * c.getWidth()) * mCardOverlapPercent) + c.getWidth();
        } else {
            width = 0;
        }
        if (getHand().getID() == 0 || getHand().getID() == 2) {
            r.setWidth(width);
            r.setX( (Director.instance().getViewWidth() - r.getWidth()) /2.0f);
        }
        else {
            r.setHeight(width);
            r.setY( (Director.instance().getViewHeight() - r.getHeight()) /2.0f);
        }

        ZSortAllViews();

        if (hidden)
            zIndex = OrganizeHiddenCards(zIndex, animation);
        if (end)
            zIndex = OrganizeEndCards(zIndex);
        if (active)
            zIndex = OrganizeActiveCards(zIndex, animation, pause);
    }

    private void ZSortAllViews() {
        int position = 0;

        Card cards[] = (Card[]) getHand().GetHiddenCards().toArray();
        for (int i=0; i<cards.length; ++i) {
            Card c = cards[i];
            if (c != null) {
                CardView cv = CardView.getCardView(c);
                cv.setZIndex(position++);
            }
        }
        cards = (Card[]) getHand().GetEndCards().toArray();
        for (int i=0; i<cards.length; ++i) {
            Card c = cards[i];
            if (c != null) {
                CardView cv = CardView.getCardView(c);
                cv.setZIndex(position++);
            }
        }
        int size = getHand().GetActiveCards().size();
        for (int i =0; i<size; ++i) {
            CardView cv = CardView.getCardView(getHand().GetActiveCards().get(i));
            cv.setZIndex(position++);
        }
    }

    private int OrganizeActiveCards(int position, boolean animation, final boolean pause) {
        if ( !(getParent() instanceof TableView) )
            throw new RuntimeException("Cannot organize cards in a hand without a TableView parent");
        if (pause && animation)
            AnimationFactory.get().pauseIncrement();
        TableView table = (TableView) getParent();
        int size = getHand().GetActiveCards().size();
        for (int i =0; i<size; ++i) {
            CardView cv = CardView.getCardView(getHand().GetActiveCards().get(i));
            CheckDisabledCards();
            HandUtils.Reparent(this, cv);

            if (animation) {
                final AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                builder.setPause(pause);
                builder.setDescription(String.format("Lining up active card %s", cv.getCard().toString())).setTable(table).setCard(cv).setHandID(getHand().getID())
                    .killPreviousAnimation(cv)
                    .setTweenCalculator(new CardAnimation.LineUpActiveCard(i))
                    .addStatusListener(new Animation.AnimationStatusListener() {
                    @Override
                    public void onEnd(Animation animation) {
                        builder.getCard().setSide(getHand().getType() == Hand.HandType.HUMAN ? CardView.Side.FRONT : CardView.Side.BACK);
                        }
                });

                if (i == size -1) {
                    builder.addStatusListener(new Animation.AnimationStatusListener() {
                        @Override
                        public void onEnd(Animation animation) {
                            if (pause) {
                                AnimationFactory.get().pauseDecrement();
                            }
                        }
                    });
                }
                builder.build().Start();
            } else {
                Vector3 pos = HandUtils.LineUpActiveCard(i, cv, table, mHand.getID(), getActivePosition(), getCardOverlapPercent());
                cv.setPosition(pos.x, pos.y);
                cv.setRotation(pos.z);
                if (mHand.getType() == Hand.HandType.CPU)
                    cv.setSide(CardView.Side.BACK);
            }
        }
        return position;
    }

    public void CheckDisabledCards() {
        if (!(getParent() instanceof TableView))
            throw new RuntimeException("The parent of the handview must be a tableview");
        TableView table = (TableView) getParent();
        for (Card c : getHand().GetActiveCards()) {
            CardView cv = CardView.getCardView(c);
            if (table.getTable().IsUnplayable(c))
                cv.setDisabled(true);
            else
                cv.setDisabled(false);
        }
    }

    private int OrganizeEndCards(int position) {
        if ( !(getParent() instanceof TableView) )
            throw new RuntimeException("Cannot organize cards in a hand without a TableView parent");
        Card cards[] = (Card[]) getHand().GetEndCards().toArray();
        TableView table = (TableView) getParent();
        for (int i=0; i<cards.length; ++i) {
            Card c = cards[i];
            if (c != null) {
                CardView cv = CardView.getCardView(c);
                float ov = cv.getWidth() * getEndCardOverlapPercent();
                if (HandUtils.IDtoSide(getHand().getID(), table) == HandUtils.HandSide.SIDE_TOP)
                    ov = - ov;
                Vector3 pos = HandUtils.LineUpEndCard(cv, table, getHand().getID(), getHiddenPosition(i), ov);
                cv.setPosition(pos.x, pos.y);
                cv.setRotation(pos.z);
            }
        }
        return position;
    }

    private int OrganizeHiddenCards(int position, boolean animation) {
        if ( !(getParent() instanceof TableView) )
            throw new RuntimeException("Cannot organize cards in a hand without a TableView parent");
        Card cards[] = (Card[]) getHand().GetHiddenCards().toArray();
        TableView table = (TableView) getParent();
        for (int i=0; i<cards.length; ++i) {
            Card c = cards[i];
            if (c != null) {
                CardView cv = CardView.getCardView(c);
                if (animation) {
                    AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                    builder.setPause(false).setDescription(String.format("Lining up hidden card %s", c.toString())).setTable(table).setCard(cv).setHandID(getHand().getID())
                            .setTweenCalculator(new CardAnimation.LineUpHiddenCards(i)).build().Start();
                }
                else {
                    Vector3 pos = HandUtils.LineUpHiddenCard(cv, table, getHand().getID(), getHiddenPosition(i));
                    cv.setPosition(pos.x, pos.y);
                    cv.setRotation(pos.z);
                    cv.setSide(CardView.Side.BACK);
                }
            }
        }
        return position;
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
        if (getActivePosition().contains(x,y)) {
            return super.hit(x,y,touchable);
        }
        for (int i=0; i<3; i++) {
            Rectangle r = getHiddenPosition(i);
            if (r.contains(x,y)) {
                return super.hit(x,y,touchable);
            }
        }
        return null;
    }

    public ActorGestureListener getGestureListener() {
        return mGestureListener;
    }


    @Override
    public void ReparentAllViews() {
        for (Actor c : getChildren()) {
            c.remove();
        }

        setupName();

        CoinView coin = createCoin();
        if (coin != null) {
            addActor(coin);
            return;
        }


        Card cards[] = (Card[]) mHand.GetHiddenCards().toArray();
        for (Card card : cards) {
            if (card != null) {
                CardView cv = CardView.getCardView(card);
                addActor(cv);
            }
        }

        cards = (Card[]) mHand.GetEndCards().toArray();
        for (Card card : cards) {
            if (card != null) {
                CardView cv = CardView.getCardView(card);
                addActor(cv);
            }
        }

        for (Card card : mHand.GetActiveCards()) {
            CardView cv = CardView.getCardView(card);
            addActor(cv);
        }

        OrganizeCards(false);
    }

    public void PanCamera(float x) {
        if ( !(getParent() instanceof TableView) )
            throw new RuntimeException("To pan the camera, the parent must be a TableView");
        ((TableView) getParent()).PanCamera(x, this);
    }

    @Override
    public String toString() {
        List<Card> cList = new ArrayList<>();
        cList.addAll(getHand().GetActiveCards());
        for (int i=0; i<3; ++i) {
            if (getHand().GetEndCards().toArray()[i] != null)
                cList.add((Card) getHand().GetEndCards().toArray()[i]);
            if (getHand().GetHiddenCards().toArray()[i] != null)
                cList.add((Card) getHand().GetHiddenCards().toArray()[i]);
        }
        int max = Math.max(cList.size(), getChildren().size);
        String s = String.format("HandView %d:\n", getHand().getID());
        for (int i = 0; i < max; ++i) {
            s += String.format("%s\t\t\t%s\n",
                    i < cList.size() ? cList.get(i) : null,
                    i < getChildren().size ? getChildren().get(i) : null
            );
        }
        return s;
    }


    @Override
    public void Reset(boolean newGame) {
        SnapshotArray<Actor> mSnap = new SnapshotArray<>(getChildren());
        for (Actor c : mSnap) {
            c.remove();
        }
        mSnap.clear();
        setupName();
    }

    @Override
    public void dispose() {
        if (mPlayerName != null)
            mPlayerName.dispose();
    }
}
