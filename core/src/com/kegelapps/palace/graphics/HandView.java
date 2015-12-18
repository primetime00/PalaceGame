package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.OrderedMap;
import com.kegelapps.palace.Card;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.Hand;
import com.kegelapps.palace.actions.GraphicActions;
import com.kegelapps.palace.events.HandEvent;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateTo;

/**
 * Created by keg45397 on 12/9/2015.
 */
public class HandView extends Actor{

    private Hand mHand;
    private Polygon mHiddenPositions[];

    public HandView(Hand hand) {
        super();
        assert(hand == null);
        mHand = hand;

        setupLayout();


        createHandEvents();
    }

    public Hand getHand() {
        return mHand;
    }

    private void setupLayout() {
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
                mHiddenPositions[0].setPosition(startX, -cardHeight/2);
                mHiddenPositions[1].setPosition(startX + nextX, -cardHeight/2);
                mHiddenPositions[2].setPosition(startX + nextX + nextX, -cardHeight/2);
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
                mHiddenPositions[0].setPosition(screenWidth - cardWidth, startY);
                mHiddenPositions[0].setRotation(90.0f);
                mHiddenPositions[1].setPosition(screenWidth - cardWidth, startY + nextX);
                mHiddenPositions[1].setRotation(90.0f);
                mHiddenPositions[2].setPosition(screenWidth - cardWidth, startY + nextX + nextX);
                mHiddenPositions[2].setRotation(90.0f);
                break;
        }
    }

    public Polygon getHiddenPosition(int index) {
        if (index > 2)
            index = 0;
        return mHiddenPositions[index];
    }


    private void createHandEvents() {
        mHand.AddEvent(new HandEvent() {
            @Override
            public void onReceivedHiddenCard(OrderedMap<String, Object> data) {
                CardView cardView = CardView.getCardView((Card) data.get("card"));
                int pos = getHand().GetHiddenCards().size()-1;
                Polygon r = getHiddenPosition(pos);
                cardView.addSequenceAction(new GraphicActions(false).LineUpHiddenCard(r, getHand().getID()));
            }
        });
    }

    private float rot = 0;
    @Override
    public void drawDebug(ShapeRenderer shapes) {
        super.drawDebug(shapes);
        shapes.setColor(Color.RED);
        for (int i=0; i<mHiddenPositions.length; ++i) {
                mHiddenPositions[0].rotate(rot);
                rot = 90;
            if (i == 0)
                shapes.rect(mHiddenPositions[i].getX(), mHiddenPositions[i].getY(), mHiddenPositions[i].getBoundingRectangle().getWidth(), mHiddenPositions[i].getBoundingRectangle().getHeight());
        }
    }
}
