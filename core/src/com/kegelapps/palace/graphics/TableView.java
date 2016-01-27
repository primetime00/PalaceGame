package com.kegelapps.palace.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.utils.Array;
import com.google.protobuf.Message;
import com.kegelapps.palace.*;
import com.kegelapps.palace.animations.CameraAnimation;
import com.kegelapps.palace.animations.CardAnimation;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.tasks.TapToStart;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.DeckProtos;
import com.kegelapps.palace.protos.HandProtos;
import com.kegelapps.palace.protos.InPlayProtos;
import com.kegelapps.palace.protos.TableProtos;

import java.util.ArrayList;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.addAction;

/**
 * Created by keg45397 on 12/9/2015.
 */
public class TableView extends Group implements Input.BoundObject {

    private Table mTable;
    private DeckView mDeck;
    private InPlayView mPlayView;
    private Array<HandView> mHands;
    private Array<CardView> mCards;

    private Pixmap mPixmap;
    private Texture mBackground;

    final private float mDeckToActiveGap = 0.10f;

    private TextView mHelperText;

    public TableView(Table table) {
        mTable = table;
        mDeck = new DeckView(table.getDeck());
        mPlayView = new InPlayView(table.getInPlay());
        mCards = new Array<>();
        mHands = new Array<>();

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

        float w = (Director.instance().getScreenWidth() - CardUtils.getCardTextureWidth())/2.0f;
        float h = (Director.instance().getScreenHeight() - CardUtils.getCardTextureHeight())/2.0f;
        mDeck.setPosition(w,h);
        mPlayView.setPosition(mDeck.getX()+CardUtils.getCardWidth()+(CardUtils.getCardWidth()*mDeckToActiveGap), mDeck.getY());
    }

    private void createTableEvents() {
        //Triggered when a card is drawn from the deck to the play card pile
        EventSystem.Event mDrawCardEvent = new EventSystem.Event(EventSystem.EventType.DRAW_PLAY_CARD) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 1 || !(params[0] instanceof Card) )
                    throw new IllegalArgumentException("Invalid parameters for DRAW_PLAY_CARD");
                CardView cardView = CardView.getCardView((Card) params[0]);
                cardView.remove();
                if (mPlayView.findActor(cardView.getName()) == null)
                    mPlayView.addActor(cardView);
                cardView.setPosition(mDeck.getX(), mDeck.getY());

                new CardAnimation(true, "Drawing from deck to active").DrawToActive(mDeck, mPlayView, cardView);
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mDrawCardEvent);

        //Triggered when a card is dealt from the deck
        EventSystem.Event mDealCardEvent = new EventSystem.Event(EventSystem.EventType.DEAL_CARD) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length < 2 || !(params[0] instanceof Card) || !(params[1] instanceof Hand))
                    throw new IllegalArgumentException("Invalid parameters for DEAL_CARD");
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
                        new CardAnimation(true, "Dealing to a hand").DealToHand(mDeck, mHands.get(index), cardView, duration);
                        break;
                    }
                }
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mDealCardEvent);

        //Triggered when dealing the 7 active cards
        EventSystem.Event mDealFirstActiveCard = new EventSystem.Event(EventSystem.EventType.DEAL_ACTIVE_CARDS) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 2 || !(params[0] instanceof Integer) || !(params[1] instanceof Integer))
                    throw new IllegalArgumentException("Invalid parameters for DEAL_ACTIVE_CARDS");
                int round = (int) params[0];
                int player = (int) params[1];
                if (round != 3 && player != 0)
                    return;
                float duration = 1.5f;
                float camX;
                float camY;
                OrthographicCamera camera = (OrthographicCamera) Director.instance().getScene().getCamera();
                camX = mDeck.getX()+(mDeck.getWidth()/2.0f);
                camY = mHands.get(0).getHiddenPosition(0).getY() + CardUtils.getCardHeight();
                new CameraAnimation(false).MoveCamera(duration, camX, camY, 1.0f, camera);
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mDealFirstActiveCard);

        //Triggered when the state engine changes state
        EventSystem.Event mTapDeckEvent = new EventSystem.Event(EventSystem.EventType.STATE_CHANGE) {
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
        Director.instance().getEventSystem().RegisterEvent(mTapDeckEvent);

        //Triggered when the player tries to play an invalid card
        EventSystem.Event mCardPlayFailed = new EventSystem.Event(EventSystem.EventType.CARD_PLAY_FAILED) {
            @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 2 || !(params[0] instanceof Card) || !(params[1] instanceof Hand)) {
                    throw new IllegalArgumentException("Invalid parameters for CARD_PLAY_FAILED");
                }
                Hand hand =  (Hand) params[1];

                CardView cardView = CardView.getCardView((Card) params[0]);

                new CardAnimation(true, "Failed card").PlayFailedCard(mPlayView, hand.getID(), cardView);

                mHands.get(hand.getID()).OrganizeCards();
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mCardPlayFailed);

        //Triggered when the player plays a valid card
        EventSystem.Event mCardPlaySuccess = new EventSystem.Event(EventSystem.EventType.CARD_PLAY_SUCCESS) {
            @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 2 || !(params[0] instanceof Card) || !(params[1] instanceof Hand)) {
                    throw new IllegalArgumentException("Invalid parameters for CARD_PLAY_FAILED");
                }
                Hand hand =  (Hand) params[1];

                CardView cardView = CardView.getCardView((Card) params[0]);

                cardView.remove();
                if (mPlayView.findActor(cardView.getName()) == null)
                    mPlayView.addActor(cardView);

                new CardAnimation(true, "Success card").PlaySuccessCard(mPlayView, hand.getID(), cardView);

                mHands.get(hand.getID()).OrganizeCards();
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mCardPlaySuccess);

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

    public Table getTable() {
        return mTable;
    }
}
