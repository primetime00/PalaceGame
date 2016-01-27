package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.kegelapps.palace.engine.InPlay;

/**
 * Created by Ryan on 1/25/2016.
 */
public class InPlayView extends Group {

    Rectangle mPlayRectangle;
    InPlay mInPlayCards;
    final private int cardsHorizontal = 5;
    final private float overlapPercentX = 0.1f;
    final private float overlapPercentY = 0.15f;
    private int mOldSize = -1;
    Vector2 mNextCardPosition;

    public InPlayView(InPlay play) {
        super();
        mInPlayCards = play;
        init();
    }

    private void init() {
        mOldSize = -1;
        mNextCardPosition = new Vector2();
        mPlayRectangle = new Rectangle(0, 0, CardUtils.getCardWidth(), CardUtils.getCardHeight());
    }


    private void CalculatePositionAndSize() {
        float x = mPlayRectangle.getX();
        float y = mPlayRectangle.getY();
        int size = mInPlayCards.GetCards().size();
        int left = size % cardsHorizontal;
        int down = size/cardsHorizontal;
        if (down % 2 == 0) //even
            x = x + (CardUtils.getCardWidth() * overlapPercentX * left);
        else //odd
            x = x + (CardUtils.getCardWidth() * overlapPercentX * (cardsHorizontal-left));
        y = y - (CardUtils.getCardWidth() * overlapPercentY * down);
        mPlayRectangle.setWidth(x+CardUtils.getCardWidth() - getX());
        mPlayRectangle.setHeight(y+CardUtils.getCardHeight() - getY());
        mNextCardPosition.set(x, y);
        setWidth(mPlayRectangle.getWidth());
        setHeight(mPlayRectangle.getHeight());
    }

    public Vector2 GetNextCardPosition() {
        return mNextCardPosition;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        mPlayRectangle.setPosition(getX(), getY());
        if (mOldSize != mInPlayCards.GetCards().size()) {
            mOldSize = mInPlayCards.GetCards().size();
            CalculatePositionAndSize();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        //we don't want to apply a transform
        drawChildren(batch, parentAlpha);
    }

    public InPlay getInPlay() {
        return mInPlayCards;
    }
}
