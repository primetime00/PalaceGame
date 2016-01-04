package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.animations.CardAnimation;
import com.kegelapps.palace.events.EventSystem;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateTo;

/**
 * Created by keg45397 on 12/9/2015.
 */
public class HandView extends Group{

    private Hand mHand;
    private Polygon mHiddenPositions[];
    private Rectangle mActivePosition;
    private float mCardOverlapPercent;

    public HandView(Hand hand) {
        super();
        assert(hand == null);
        mHand = hand;
        mCardOverlapPercent = 0.75f;
        debug();

        setupHiddenLayout();
        setupActiveLayout();


        createHandEvents();
    }

    public void setCardOverLapPercent(float val) {
        mCardOverlapPercent = val;
    }

    public float getCardOverlapPercent() {
        return mCardOverlapPercent;
    }

    private void setupActiveLayout() {
        int cardHeight = CardUtils.getCardHeight();
        int cardWidth = CardUtils.getCardWidth();
        switch (mHand.getID()) {
            default:
            case 0: //bottom
                mActivePosition = new Rectangle(0, 0, Director.instance().getScreenWidth(), cardHeight);
                break;
            case 1: //left
                mActivePosition = new Rectangle(0, 0, cardHeight, Director.instance().getScreenHeight());
                break;
            case 2: //top
                mActivePosition = new Rectangle(0, Director.instance().getScreenHeight()-cardHeight, Director.instance().getScreenWidth(), cardHeight);
                break;
            case 3: //right
                mActivePosition = new Rectangle(Director.instance().getScreenWidth()-cardHeight, 0, cardHeight, Director.instance().getScreenHeight());
                break;
        }
    }

    public Hand getHand() {
        return mHand;
    }

    private void setupHiddenLayout() {
        mHiddenPositions = new Polygon[3];
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
            mHiddenPositions[i] = new Polygon(new float[]{0, 0, cardWidth, 0, cardWidth, cardHeight, 0, cardHeight});
            mHiddenPositions[i].setOrigin(cardWidth/2.0f, cardHeight/2.0f);
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
                mHiddenPositions[0].rotate(-90.0f);
                mHiddenPositions[1].setPosition(0, startY + nextX);
                mHiddenPositions[1].rotate(-90.0f);
                mHiddenPositions[2].setPosition(0, startY + nextX + nextX);
                mHiddenPositions[2].rotate(-90.0f);
                break;
            case 2: //top
                mHiddenPositions[0].setPosition(startX, screenHeight-cardHeight);
                mHiddenPositions[1].setPosition(startX + nextX, screenHeight-cardHeight);
                mHiddenPositions[2].setPosition(startX + nextX + nextX, screenHeight-cardHeight);
                break;
            case 3: //right
                mHiddenPositions[0].setPosition(screenWidth - cardHeight, startY);
                mHiddenPositions[0].setRotation(90.0f);
                mHiddenPositions[1].setPosition(screenWidth - cardHeight, startY + nextX);
                mHiddenPositions[1].setRotation(90.0f);
                mHiddenPositions[2].setPosition(screenWidth - cardHeight, startY + nextX + nextX);
                mHiddenPositions[2].setRotation(90.0f);
                break;
        }
    }

    public Polygon getHiddenPosition(int index) {
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
                Polygon r = getHiddenPosition(pos);

                new CardAnimation(false).LineUpHiddenCards(r, getHand().getID(), cardView);
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
                for (int i =0; i<size; ++i) {
                    CardView cv = CardView.getCardView(getHand().getActiveCards().get(i));
                    cv.setZIndex(i);
                    new CardAnimation(false).LineUpActiveCard(HandView.this, cv, i);
                }
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mLayoutActiveCardEvent);

    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        super.drawDebug(shapes);
        shapes.setColor(Color.RED);
        //for (int i=0; i<3; ++i)
        //    shapes.rect(mHiddenPositions[i].getX(), mHiddenPositions[i].getY(), mHiddenPositions[i].getBoundingRectangle().getWidth(), mHiddenPositions[i].getBoundingRectangle().getHeight());
        shapes.rect(mActivePosition.getX(), mActivePosition.getY(), mActivePosition.getWidth(), mActivePosition.getHeight());
    }
}
