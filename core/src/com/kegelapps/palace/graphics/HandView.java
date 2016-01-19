package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.animations.CardAnimation;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.events.EventSystem;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateTo;

/**
 * Created by keg45397 on 12/9/2015.
 */
public class HandView extends Group{

    private Hand mHand;
    private Rectangle mHiddenPositions[];
    private Rectangle mActivePosition;

    private float mCardOverlapPercent;
    private float mEndCardOverlapPercent;

    private ActorGestureListener mGestureListener;

    public HandView(Hand hand) {
        super();
        assert(hand == null);
        mHand = hand;
        mCardOverlapPercent = 0.75f;
        mEndCardOverlapPercent = 0.05f;

        setupHiddenLayout();
        setupActiveLayout();


        createHandEvents();

        mGestureListener = new ActorGestureListener() {
            @Override
            public void fling(InputEvent event, float velocityX, float velocityY, int button) {
                super.fling(event, velocityX, velocityY, button);
                if (event.getTarget() instanceof CardView) {
                    Card c = ((CardView)event.getTarget()).getCard();
                    if (getHand().getActiveCards().contains(c) && velocityY > 200.0f) {
                        Logic.get().PlayerSelectCard(getHand(), c);
                    }
                    else if (getHand().getEndCards().contains(c) && velocityY < -200.0f) {
                        Logic.get().PlayerUnselectCard(getHand(), c);
                    }
                }
            }
        };
        if (getHand().getType() == Hand.HandType.HUMAN)
            addListener(mGestureListener);
    }

    public void setCardOverLapPercent(float val) {
        mCardOverlapPercent = val;
    }
    public void setEndCardOverlapPercent(float val) { mEndCardOverlapPercent  = val;}


    public float getCardOverlapPercent() {
        return mCardOverlapPercent;
    }
    public float getEndCardOverlapPercent() { return mEndCardOverlapPercent; }

    private void setupActiveLayout() {
        int cardHeight = CardUtils.getCardHeight();
        int cardWidth = CardUtils.getCardWidth();
        switch (mHand.getID()) {
            default:
            case 0: //bottom
                mActivePosition = new Rectangle(0, 0, Director.instance().getScreenWidth(), cardHeight);
                mActivePosition.setY(-cardHeight - (cardHeight*0.10f));
                break;
            case 1: //left
                mActivePosition = new Rectangle(0, 0, cardHeight, Director.instance().getScreenHeight());
                mActivePosition.setX(-cardHeight - (cardHeight*0.10f));
                break;
            case 2: //top
                mActivePosition = new Rectangle(0, Director.instance().getScreenHeight()-cardHeight, Director.instance().getScreenWidth(), cardHeight);
                mActivePosition.setY(Director.instance().getScreenHeight()+ (cardHeight*0.10f));
                break;
            case 3: //right
                mActivePosition = new Rectangle(Director.instance().getScreenWidth()-cardHeight, 0, cardHeight, Director.instance().getScreenHeight());
                mActivePosition.setX(Director.instance().getScreenWidth()+ (cardHeight*0.10f));
                break;
        }
    }

    public Hand getHand() {
        return mHand;
    }

    private void setupHiddenLayout() {
        mHiddenPositions = new Rectangle[3];
        int cardHeight = CardUtils.getCardHeight();
        int cardWidth = CardUtils.getCardWidth();
        int screenWidth = Director.instance().getScreenWidth();
        int screenHeight = Director.instance().getScreenHeight();
        float cardGap = cardWidth * 0.1f;
        float hiddenWidth = cardWidth * 3 + cardGap * 2;
        float startX = (screenWidth - hiddenWidth) / 2.0f;
        float startY = (screenHeight - hiddenWidth) / 2.0f;
        float nextX = cardWidth + cardGap;
        for (int i=0; i<mHiddenPositions.length; ++i) {
            if (i == 0 || i == 2)
                mHiddenPositions[i] = new Rectangle(0, 0, cardWidth, cardHeight);
            else
                mHiddenPositions[i] = new Rectangle(0, 0, cardHeight, cardWidth);
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
                mHiddenPositions[0].setPosition(startX, screenHeight-cardHeight);
                mHiddenPositions[1].setPosition(startX + nextX, screenHeight-cardHeight);
                mHiddenPositions[2].setPosition(startX + nextX + nextX, screenHeight-cardHeight);
                break;
            case 3: //right
                mHiddenPositions[0].setPosition(screenWidth - cardHeight, startY);
                mHiddenPositions[1].setPosition(screenWidth - cardHeight, startY + nextX);
                mHiddenPositions[2].setPosition(screenWidth - cardHeight, startY + nextX + nextX);
                break;
        }
    }

    public Rectangle getHiddenPosition(int index) {
        if (index > 2)
            index = 0;
        return mHiddenPositions[index];
    }

    public Rectangle getActivePosition() {
        return mActivePosition;
    }


    private void createHandEvents() {

        EventSystem.Event mLayoutHiddenCardEvent = new EventSystem.Event(EventSystem.EventType.LAYOUT_HIDDEN_CARD) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 2 || !(params[0] instanceof Card) || !(params[1] instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid parameters for LAYOUT_HIDDEN_CARD");
                }

                int id = (int) params[1];
                if (getHand().getID() != id)
                    return;

                CardView cardView = CardView.getCardView((Card) params[0]);

                cardView.getParent().removeActor(cardView);
                addActor(cardView);

                int pos = getHand().GetHiddenCards().size()-1;
                Rectangle r = getHiddenPosition(pos);

                new CardAnimation(false).LineUpHiddenCards(r, getHand().getID(), cardView);
                cardView.setZIndex(0);
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mLayoutHiddenCardEvent);

        EventSystem.Event mLayoutActiveCardEvent = new EventSystem.Event(EventSystem.EventType.LAYOUT_ACTIVE_CARD) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 2 || !(params[0] instanceof Card) || !(params[1] instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid parameters for LAYOUT_ACTIVE_CARD");
                }

                int id = (int) params[1];
                if (getHand().getID() != id)
                    return;

                CardView cardView = CardView.getCardView((Card) params[0]);
                cardView.getParent().removeActor(cardView);
                addActor(cardView);

                OrganizeCards();
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mLayoutActiveCardEvent);

        EventSystem.Event mSelectEndCardEvent = new EventSystem.Event(EventSystem.EventType.SELECT_END_CARD) {
            @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 3 || !(params[0] instanceof Card) || !(params[1] instanceof Integer) || !(params[2] instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid parameters for SELECT_END_CARD");
                }
                int id = (int) params[1];
                if (getHand().getID() != id)
                    return;

                CardView cardView = CardView.getCardView((Card) params[0]);
                Rectangle r = getHiddenPosition((int)params[2]);

                float ov = CardUtils.getCardWidth() * getEndCardOverlapPercent();
                new CardAnimation(false).SelectEndCard(r.getX()+ov, r.getY()+ov, id, cardView);

                OrganizeCards();
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mSelectEndCardEvent);

    }

    public void OrganizeCards() {
        int zIndex = 0;
        int size = getHand().GetActiveCards().size();
        Rectangle r = getActivePosition();
        float width = (( (size-1) * CardUtils.getCardWidth()) * mCardOverlapPercent) + CardUtils.getCardWidth();
        if (getHand().getID() == 0 || getHand().getID() == 2) {
            r.setWidth(width);
            r.setX( (Director.instance().getScreenWidth() - r.getWidth()) /2.0f);
        }
        else {
            r.setHeight(width);
            r.setY( (Director.instance().getScreenHeight() - r.getHeight()) /2.0f);
        }
        for (Card c : getHand().GetHiddenCards()) {
            CardView cv = CardView.getCardView(c);
            cv.setZIndex(zIndex++);
        }
        for (int i =0; i<size; ++i) {
            CardView cv = CardView.getCardView(getHand().getActiveCards().get(i));
            cv.setZIndex(zIndex++);
            new CardAnimation(false).LineUpActiveCard(HandView.this, cv, i);
        }
        for (Card c : getHand().getEndCards()) {
            if (c != null) {
                CardView cv = CardView.getCardView(c);
                cv.setZIndex(zIndex++);
            }
        }
    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        super.drawDebug(shapes);
        shapes.set(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.RED);
        shapes.rect(mActivePosition.getX(), mActivePosition.getY(), mActivePosition.getWidth(), mActivePosition.getHeight());
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
        if (getActivePosition().contains(x,y)) {
            Actor a = super.hit(x,y,touchable);
            return a;
        }
        for (int i=0; i<3; i++) {
            Rectangle r = getHiddenPosition(i);
            if (r.contains(x,y)) {
                Actor a = super.hit(x,y,touchable);
                return a;
            }
        }
        return null;
    }

    public ActorGestureListener getGestureListener() {
        return mGestureListener;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }
}
