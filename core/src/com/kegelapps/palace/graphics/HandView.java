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
    private Rectangle mHiddenPositions[];

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
        switch (mHand.getID()) {
            default:
            case 0: //bottom
                mHiddenPositions[0] = new Rectangle(startX, -cardHeight/2, cardWidth, cardHeight);
                mHiddenPositions[1] = new Rectangle(startX + nextX, -cardHeight/2, cardWidth, cardHeight);
                mHiddenPositions[2] = new Rectangle(startX + nextX + nextX, -cardHeight/2, cardWidth, cardHeight);
                break;
            case 1: //left
                mHiddenPositions[0] = new Rectangle(0, startY, cardWidth, cardHeight);
                mHiddenPositions[1] = new Rectangle(0, startY + nextX, cardHeight, cardWidth);
                mHiddenPositions[2] = new Rectangle(0, startY + nextX + nextX, cardHeight, cardWidth);
                break;
            case 2: //top
                mHiddenPositions[0] = new Rectangle(startX, screenHeight-cardHeight, cardWidth, cardHeight);
                mHiddenPositions[1] = new Rectangle(startX + nextX, screenHeight-cardHeight, cardWidth, cardHeight);
                mHiddenPositions[2] = new Rectangle(startX + nextX + nextX, screenHeight-cardHeight, cardWidth, cardHeight);
                break;
            case 3: //right
                mHiddenPositions[0] = new Rectangle(screenWidth - cardWidth, startY, cardHeight, cardWidth);
                mHiddenPositions[1] = new Rectangle(screenWidth - cardWidth, startY + nextX, cardHeight, cardWidth);
                mHiddenPositions[2] = new Rectangle(screenWidth - cardWidth, startY + nextX + nextX, cardHeight, cardWidth);
                break;
        }
    }

    public Rectangle getHiddenPosition(int index) {
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
                Rectangle r = getHiddenPosition(pos);
                cardView.addSequenceAction(new GraphicActions(false).LineUpHiddenCard(r, getHand().getID()));
            }
        });
    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        super.drawDebug(shapes);
        shapes.setColor(Color.RED);
        for (int i=0; i<mHiddenPositions.length; ++i) {
            shapes.rect(mHiddenPositions[i].getX(), mHiddenPositions[i].getY(), mHiddenPositions[i].getWidth(), mHiddenPositions[i].getHeight());
        }
    }
}
