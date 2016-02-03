package com.kegelapps.palace.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.Input;
import com.kegelapps.palace.animations.AnimationBuilder;
import com.kegelapps.palace.animations.AnimationFactory;
import com.kegelapps.palace.animations.CameraAnimation;
import com.kegelapps.palace.animations.CardAnimation;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.Play;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.tasks.TapToStart;
import com.kegelapps.palace.events.EventSystem;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by keg45397 on 12/9/2015.
 */
public class TableView extends Group implements Input.BoundObject {

    private Table mTable;
    private DeckView mDeck;
    private InPlayView mPlayView;
    private Array<HandView> mHands;
    private Map<HandUtils.HandSide, HandView> mHandSide;
    private CardCamera mCamera;

    private Array<CardView> mCards;

    private Pixmap mPixmap;
    private Texture mBackground;

    final private float mDeckToActiveGap = 0.10f;

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
        for (Card c : mTable.getDeck().GetCards()) {
            mCards.add(new CardView(c));
        }

        init();
    }

    private void init() {
        mHelperText = new TextView(Director.instance().getGameFont());


        mPixmap = new Pixmap(Director.instance().getVirtualWidth(),Director.instance().getVirtualHeight(), Pixmap.Format.RGB888);
        mPixmap.setColor(Color.GREEN);
        mPixmap.fillRectangle(0, 0, mPixmap.getWidth(), mPixmap.getHeight());
        mBackground = new Texture(mPixmap);
        onScreenSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        addActor(mDeck);
        addActor(mPlayView);
        for (HandView hView : mHands){
            addActor(hView);
        }


        createTableEvents();

        mHandSide = new HashMap<>();
        for (HandView h : mHands) {
            if (h.getHand().getType() == Hand.HandType.HUMAN)
                mHandSide.put(HandUtils.HandSide.SIDE_BOTTOM, h);
            else {
                if (mHandSide.get(HandUtils.HandSide.SIDE_LEFT) == null)
                    mHandSide.put(HandUtils.HandSide.SIDE_LEFT, h);
                else if (mHandSide.get(HandUtils.HandSide.SIDE_TOP) == null)
                    mHandSide.put(HandUtils.HandSide.SIDE_TOP, h);
                else if (mHandSide.get(HandUtils.HandSide.SIDE_RIGHT) == null)
                    mHandSide.put(HandUtils.HandSide.SIDE_RIGHT, h);
                else if (mHandSide.get(HandUtils.HandSide.SIDE_BOTTOM) == null)
                    mHandSide.put(HandUtils.HandSide.SIDE_BOTTOM, h);
            }
        }

        float w = (Director.instance().getScreenWidth() - CardUtils.getCardTextureWidth())/2.0f;
        float h = (Director.instance().getScreenHeight() - CardUtils.getCardTextureHeight())/2.0f;
        mDeck.setPosition(w,h);
        mPlayView.setPosition(mDeck.getX()+CardUtils.getCardWidth()+(CardUtils.getCardWidth()*mDeckToActiveGap), mDeck.getY());
    }

    private void createTableEvents() {
        //Triggered when a card is drawn from the deck to the play card pile
        EventSystem.EventListener mDrawCardEventListener = new EventSystem.EventListener(EventSystem.EventType.DRAW_PLAY_CARD) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 1 || !(params[0] instanceof Card) )
                    throw new IllegalArgumentException("Invalid parameters for DRAW_PLAY_CARD");
                CardView cardView = CardView.getCardView((Card) params[0]);
                cardView.remove();
                if (mPlayView.findActor(cardView.getName()) == null)
                    mPlayView.addActor(cardView);
                cardView.setPosition(mDeck.getX(), mDeck.getY());

                AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                builder.setPause(true).setDescription("Drawing from deck to active").setTable(TableView.this).setCard(cardView)
                        .setTweenCalculator(new CardAnimation.DrawToActive()).build().Start();

            }
        };
        Director.instance().getEventSystem().RegisterEvent(mDrawCardEventListener);

        //Triggered when a card is dealt from the deck
        EventSystem.EventListener mDealCardEventListener = new EventSystem.EventListener(EventSystem.EventType.DEAL_CARD) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length < 2 || !(params[0] instanceof Card) || !(params[1] instanceof Hand))
                    throw new IllegalArgumentException("Invalid parameters for DEAL_HIDDEN_CARD");
                CardView cardView = CardView.getCardView((Card) params[0]);
                Hand hand =  (Hand) params[1];

                float duration = params.length >= 3 && params[2] instanceof Float ? (float)params[2] : 0.5f;
                cardView.setPosition(mDeck.getX(), mDeck.getY());
                cardView.setSide(CardView.Side.BACK);

                if (findActor(cardView.getCard().toString()) == null)
                    addActor(cardView);
                for (int index =0; index<mHands.size; ++index)
                {
                    if (mHands.get(index).getHand() == hand) {
                        AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                        builder.setPause(true).setDescription("Dealing to a hand").setTable(TableView.this).setCard(cardView).setHandID(index)
                                .setTweenCalculator(new CardAnimation.DealToHand()).build().Start();
                        break;
                    }
                }
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
                if ((params[0] instanceof TapToStart)) {
                    mHelperText.setText("Double Tap Deck To Start!");
                    float x = mDeck.getX() - (mHelperText.getWidth() + mDeck.getWidth())/4.0f;
                    float y = mDeck.getY()-(mDeck.getHeight()*0.05f);
                    mHelperText.setX(x);
                    mHelperText.setY(y);
                    mHelperText.setColor(Color.RED);
                    return;
                }
                mHelperText.setText("");

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

                AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                builder.setPause(true).setDescription("Failed card").setTable(TableView.this).setCard(cardView).setHandID(hand.getID()).
                        killPreviousAnimation(cardView).setTweenCalculator(new CardAnimation.PlayFailedCard()).build().Start();

                //mHands.get(hand.getID()).OrganizeCards(true);
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mCardPlayFailed);

        //Triggered when the player plays a valid card
        EventSystem.EventListener mCardPlaySuccess = new EventSystem.EventListener(EventSystem.EventType.CARD_PLAY_SUCCESS) {
            @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 2 || !(params[0] instanceof Card) || !(params[1] instanceof Hand)) {
                    throw new IllegalArgumentException("Invalid parameters for CARD_PLAY_SUCCESS");
                }
                Hand hand =  (Hand) params[1];

                CardView cardView = CardView.getCardView((Card) params[0]);

                cardView.remove();
                if (mPlayView.findActor(cardView.getName()) == null)
                    mPlayView.addActor(cardView);

                AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                builder.setPause(true).setDescription("Success card").setTable(TableView.this).setCard(cardView).setHandID(hand.getID()).
                killPreviousAnimation(cardView).setTweenCalculator(new CardAnimation.PlaySuccessCard()).build().Start();

                mHands.get(hand.getID()).OrganizeCards(true);
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
                        s.containsState(State.Names.PLAY) ||
                        s.containsState(State.Names.DRAW_PLAY_CARD)) {

                    HandUtils.HandSide side = HandUtils.HandSide.SIDE_UNKNOWN;
                    if (s.getState(State.Names.SELECT_END_CARDS) != null || s.getState(State.Names.DRAW_PLAY_CARD) != null)
                        side = HandUtils.HandSide.SIDE_BOTTOM;
                    else if (s.getState(State.Names.PLAY) != null) {
                        int id = ((Play)(s.getState(State.Names.PLAY))).getCurrentPlayer();
                        side = getSideFromHand(id);
                    }
                    Vector2 pos = HandUtils.GetHandPosition(TableView.this, side);
                    mCamera.SetPosition(pos, 1.0f, HandUtils.HandSideToCamera(side));
                }
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

    }

    public void onScreenSize(int w, int h) {
        setBounds(0,0,w,h);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = -(mBackground.getWidth()/2.0f - (mDeck.getX() + mDeck.getWidth()/2.0f));
        float y = -(mBackground.getHeight()/2.0f - (mDeck.getY() + mDeck.getHeight()/2.0f));
        batch.draw(mBackground, x,y);
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

    public Vector2 MoveCamera(HandUtils.HandSide side) {
        Vector2 res = new Vector2();
        float camX;
        float camY;
        CardCamera camera = Director.instance().getScene().getCardCamera();
        switch (side) {
            default:
            case SIDE_BOTTOM:
                camX = mDeck.getX()+(mDeck.getWidth()/2.0f);
                camY = mHands.get(0).getHiddenPosition(0).getY() + CardUtils.getCardHeight();
                break;
            case SIDE_LEFT:
                camX = mHands.get(1).getHiddenPosition(0).getX() - CardUtils.getCardWidth();
                camY = mDeck.getY()+(mDeck.getHeight()/2.0f);
                break;
            case SIDE_TOP:
                camX = mDeck.getX()+(mDeck.getWidth()/2.0f);
                camY = mHands.get(2).getHiddenPosition(0).getY() - CardUtils.getCardHeight();
                break;
            case SIDE_RIGHT:
                camX = mHands.get(3).getHiddenPosition(0).getX() + CardUtils.getCardWidth();
                camY = mDeck.getY()+(mDeck.getHeight()/2.0f);
                break;
        }
        res.set(camX, camY);
        return res;
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

    CardCamera getCamera() {
        return mCamera;
    }


    public InPlayView getPlayView() {
        return mPlayView;
    }
}
