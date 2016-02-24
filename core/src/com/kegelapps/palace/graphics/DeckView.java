package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.Input;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Deck;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.dealtasks.TapToStart;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.utils.CardUtils;

/**
 * Created by keg45397 on 12/9/2015.
 */
public class DeckView extends Group implements Input.BoundObject {

    private Deck mDeck;

    private TextureAtlas.AtlasRegion mDeckBack;
    private HighlightView mHighlightView;

    private ActorGestureListener mGestureListener;

    private boolean mDeckLow = false;

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
                    if (button == 0) {
                        Logic.get().Request(State.Names.TAP_DECK_START);
                        Logic.get().Request(State.Names.PLAY_HUMAN_TURN);
                    }
                    else if (button == 1)
                        Logic.get().SaveState();
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
                    else if (getHand().GetEndCards().contains(c) && velocityY < -200.0f) {
                        Logic.get().PlayerUnselectCard(getHand(), c);
                    }
                }*/
            }
        };
        addListener(mGestureListener);
    }

    private void createEvents() {

        EventSystem.EventListener mTapToStartEventListener = new EventSystem.EventListener(EventSystem.EventType.STATE_CHANGE) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 1 || !(params[0] instanceof State)) {
                    throw new IllegalArgumentException("Invalid parameters for STATE_CHANGE");
                }
                if (!(params[0] instanceof TapToStart)) {
                    setHighlight(false);
                    return;
                }
                setHighlight(true);
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mTapToStartEventListener);

        EventSystem.EventListener mDrawCardEventListener = new EventSystem.EventListener(EventSystem.EventType.DRAW_PLAY_CARD) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 1 || !(params[0] instanceof Card))
                    throw new IllegalArgumentException("Invalid parameters for DRAW_PLAY_CARD");
                CheckLowDeck();
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mDrawCardEventListener);

        EventSystem.EventListener mWaitForPlayerToTap = new EventSystem.EventListener(EventSystem.EventType.HIGHLIGHT_DECK) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 1 || !(params[0] instanceof Boolean)) {
                    throw new IllegalArgumentException("Invalid parameters for HIGHLIGHT_DECK");
                }
                setHighlight((Boolean) params[0]);
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mWaitForPlayerToTap);

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.STATE_LOADED) {
            @Override
            public void handle(Object[] params) {
                CheckLowDeck();
                if (Logic.get().GetMainState() != null) {
                    setHighlight(Logic.get().GetMainState().containsState(State.Names.TAP_DECK_START));
                }
            }
        });


        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.DRAW_TURN_END_CARDS) {
            @Override
            public void handle(Object[] params) {

                CheckLowDeck();
            }
        });
    }

    private void CheckLowDeck() {
        if (mDeck.GetCards().size() <= 4 && mDeckLow == false) { //we just hit 4 cards
            mDeckLow = true;
            int cascade = 0;
            for (int i = mDeck.GetCards().size() - 1; i >= 0; --i) {
                Card c = mDeck.GetCards().get(i);
                CardView cardView = CardView.getCardView(c);
                cardView.setSide(CardView.Side.BACK);
                cardView.setX(cascade);
                cardView.setY(0);
                cascade += (cardView.getWidth() * 0.04f);
                cardView.remove();
                addActor(cardView);
            }
        }
    }

    public Deck getDeck() {
        return mDeck;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (mDeckLow) {
            super.draw(batch, parentAlpha);
        }
        else {
            batch.draw(mDeckBack, getX(), getY());
        }
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
    public String toString() {
        return "DeckView";
    }
}
