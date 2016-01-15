package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.*;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.animations.CardAnimation;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Deck;
import com.kegelapps.palace.Input;
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
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                System.out.print("Down");
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                System.out.print("Up");
            }
        });
        mHighlightView = new HighlightView();
        createEvents();
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
