package com.kegelapps.palace.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.utils.Array;
import com.kegelapps.palace.*;
import com.kegelapps.palace.animations.CameraAnimation;
import com.kegelapps.palace.animations.CardAnimation;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.events.EventSystem;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.addAction;

/**
 * Created by keg45397 on 12/9/2015.
 */
public class TableView extends Group implements Input.BoundObject {

    private Table mTable;
    private DeckView mDeck;
    private Array<HandView> mHands;
    private Array<CardView> mCards;

    private Pixmap mPixmap;
    private Texture mBackground;

    public TableView(Table table) {
        mTable = table;
        mDeck = new DeckView(table.getDeck());
        mCards = new Array<>();
        mHands = new Array<>();
        for (Hand h : mTable.getHands()) {
            mHands.add(new HandView(h));
        }
        for (Card c : mTable.getDeck().GetCards()) {
            mCards.add(new CardView(c));
        }

        mPixmap = new Pixmap(Director.instance().getVirtualWidth(),Director.instance().getVirtualHeight(), Pixmap.Format.RGB888);
        mPixmap.setColor(Color.GREEN);
        mPixmap.fillRectangle(0, 0, mPixmap.getWidth(), mPixmap.getHeight());
        mBackground = new Texture(mPixmap);
        onScreenSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        addActor(mDeck);
        for (HandView hView : mHands){
            addActor(hView);
        }


        createTableEvents();

        float w = (Director.instance().getScreenWidth() - CardUtils.getCardTextureWidth())/2.0f;
        float h = (Director.instance().getScreenHeight() - CardUtils.getCardTextureHeight())/2.0f;
        mDeck.setPosition(w,h);
    }

    private void createTableEvents() {
        EventSystem.Event mDrawCardEvent = new EventSystem.Event(EventSystem.EventType.DRAW_PLAY_CARD) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 1 || !(params[0] instanceof Card) )
                    throw new IllegalArgumentException("Invalid parameters for DRAW_PLAY_CARD");
                CardView cardView = CardView.getCardView((Card) params[0]);
                cardView.setPosition(mDeck.getX(), mDeck.getY());
                cardView.setSide(CardView.Side.BACK);
                if (findActor(cardView.getCard().toString()) == null)
                    addActor(cardView);

                new CardAnimation(true).DrawToActive(mDeck, cardView);
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mDrawCardEvent);

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
                        new CardAnimation(true).DealToHand(mDeck, mHands.get(index), cardView, 0.5f);
                        break;
                    }
                }
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mDealCardEvent);

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
                float camX = Director.instance().getScene().getCamera().position.x;
                float camY = Director.instance().getScene().getCamera().position.y;
                OrthographicCamera camera = (OrthographicCamera) Director.instance().getScene().getCamera();
                camX = mDeck.getX()+(mDeck.getWidth()/2.0f);
                camY = mHands.get(0).getY()-mHands.get(0).getHeight(); //mDeck.getY()+(mDeck.getHeight()/2.0f);
                new CameraAnimation(false).MoveCamera(duration, camX, camY, 1.0f, camera);
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mDealFirstActiveCard);

    }

    public void onScreenSize(int w, int h) {
        //mDeck.setPosition( (w - CardUtils.getCardTextureWidth())/2.0f, (h - CardUtils.getCardHeight())/2.0f);
        //mDeck.setSize(CardUtils.getCardTextureWidth(), CardUtils.getCardHeight());
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
    }



    @Override
    public Rectangle getBounds() {
        return new Rectangle(0,0,800,480);
    }
}
