package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.Serializer;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Deck;
import com.kegelapps.palace.Input;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.tasks.TapToStart;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.CardProtos;
import com.kegelapps.palace.protos.DeckProtos;

/**
 * Created by keg45397 on 12/9/2015.
 */
public class DeckView extends Group implements Input.BoundObject, Serializer {

    private Deck mDeck;

    private TextureAtlas.AtlasRegion mDeckBack;
    private HighlightView mHighlightView;

    private ActorGestureListener mGestureListener;

    private boolean mDeckLow = false;

    public DeckView() {
        super();
        mDeck = new Deck();
        init();
    }


    public DeckView(Deck deck) {
        super();
        assert (deck == null);
        mDeck = deck;
        init();
    }

    private void init() {
        mDeckBack = CardUtils.getCardBackDeckRegion();
        setBounds(0,0,mDeckBack.originalWidth, mDeckBack.originalHeight);
        mHighlightView = new HighlightView();
        createEvents();
        createGestures();
        mDeckLow = false;
    }

    private void createGestures() {
        mGestureListener = new ActorGestureListener() {

            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                super.tap(event, x, y, count, button);
                if (count >= 2) {
                    Logic.get().Request(Logic.LogicRequest.PLAY_START);
                }
            }

            @Override
            public void fling(InputEvent event, float velocityX, float velocityY, int button) {
                super.fling(event, velocityX, velocityY, button);
/*                if (event.getTarget() instanceof CardView) {
                    Card c = ((CardView)event.getTarget()).getCard();
                    if (getHand().getActiveCards().contains(c) && velocityY > 200.0f) {
                        Logic.get().PlayerSelectCard(getHand(), c);
                    }
                    else if (getHand().getEndCards().contains(c) && velocityY < -200.0f) {
                        Logic.get().PlayerUnselectCard(getHand(), c);
                    }
                }*/
            }
        };
        addListener(mGestureListener);
    }

    private void createEvents() {
        EventSystem.Event mTapToStartEvent = new EventSystem.Event(EventSystem.EventType.STATE_CHANGE) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 1 || !(params[0] instanceof State)) {
                    throw new IllegalArgumentException("Invalid parameters for STATE_CHANGE");
                }
                if ( !(params[0] instanceof TapToStart) ) {
                    setHighlight(false);
                    return;
                }
                setHighlight(true);
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mTapToStartEvent);

        EventSystem.Event mDrawCardEvent = new EventSystem.Event(EventSystem.EventType.DRAW_PLAY_CARD) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 1 || !(params[0] instanceof Card) )
                    throw new IllegalArgumentException("Invalid parameters for DRAW_PLAY_CARD");
                if (mDeck.GetCards().size() <= 4 && mDeckLow == false) { //we just hit 4 cards
                    mDeckLow = true;
                    int cascade = 0;
                    for (int i = mDeck.GetCards().size()-1; i >=0; --i) {
                        Card c = mDeck.GetCards().get(i);
                        CardView cardView = CardView.getCardView(c);
                        cardView.setSide(CardView.Side.BACK);
                        cardView.setX(cascade);
                        cardView.setY(0);
                        cascade += (cardView.getWidth() * 0.04f);
                        if (cardView.getParent() != null)
                            cardView.getParent().removeActor(cardView);
                        if (findActor(cardView.getName()) == null)
                            addActor(cardView);
                    }
                }
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mDrawCardEvent);


    }

    public Deck getDeck() {
        return mDeck;
    }

    public void setmDeck(Deck mDeck) {
        this.mDeck = mDeck;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (mDeckLow)
            super.draw(batch, parentAlpha);
        else
            batch.draw(mDeckBack, getX(), getY());
        if (mHighlightView.isVisible())
            mHighlightView.draw(batch, this);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle();
    }

    public void setHighlight(boolean highlight) {
        if (highlight)
            mHighlightView.show();
        else
            mHighlightView.hide();
    }

    @Override
    public void ReadBuffer(Message msg) {
        DeckProtos.DeckView dv = (DeckProtos.DeckView) msg;
        setPosition(dv.getX(), dv.getY());
        mDeck = new Deck();
        mDeck.GetCards().clear();
        for (int i=0; i<dv.getCardsCount(); ++i) {
            CardView cv = new CardView();
            cv.ReadBuffer(dv.getCards(i));
            mDeck.GetCards().add(cv.getCard());
        }

    }

    @Override
    public Message WriteBuffer() {
        DeckProtos.DeckView.Builder builder = DeckProtos.DeckView.newBuilder();
        for (int i = mDeck.GetCards().size()-1; i>=0; --i) {
            Card c = mDeck.GetCards().get(i);
            CardView cv = CardView.getCardView(c);
            if (cv != null)
                builder.addCards((CardProtos.CardView) cv.WriteBuffer());
        }
        builder.setY(getY());
        builder.setX(getX());
        return builder.build();
    }
}
