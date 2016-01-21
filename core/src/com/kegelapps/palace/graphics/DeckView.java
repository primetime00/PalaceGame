package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.animations.CardAnimation;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Deck;
import com.kegelapps.palace.Input;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.tasks.TapToStart;
import com.kegelapps.palace.events.EventSystem;

/**
 * Created by keg45397 on 12/9/2015.
 */
public class DeckView extends Actor implements Input.BoundObject {

    private Deck mDeck;

    private TextureAtlas.AtlasRegion mDeckBack;
    private HighlightView mHighlightView;

    private ActorGestureListener mGestureListener;

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
        //if (getHand().getType() == Hand.HandType.HUMAN)
        //    addListener(mGestureListener);
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

    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if ( mDeck.CountCards() > 4 ) { //lets draw the stack of cards
            batch.draw(mDeckBack, getX(), getY());
        }
        /*
        else if (mDeck.CountCards() <= 4) { //we will draw cascaded cards?
            setTexture(null);
        }*/
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


}
