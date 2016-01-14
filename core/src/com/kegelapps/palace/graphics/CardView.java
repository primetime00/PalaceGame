package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.OrderedMap;
import com.kegelapps.palace.engine.Card;

/**
 * Created by keg45397 on 12/8/2015.
 */
public class CardView extends Actor {

    private Card mCard;
    private Side mSide;
    private TextureAtlas.AtlasRegion mCardRegion;
    private TextureAtlas.AtlasRegion mCardFace;
    private float mMaxCardSize;

    public enum Side {
        FRONT,
        BACK
    }

    private static OrderedMap<Card, CardView> mCardMap;

    public CardView(Card.Suit suit, Card.Rank rank) {
        super();
        mCard = new Card(suit, rank);
        init();
    }

    public CardView(Card card) {
        super();
        assert(card == null);
        mCard = card;
        init();
    }

    private void init() {
        mCardFace = CardUtils.getCardRegion(mCard.getSuit(), mCard.getRank());
        setSide(Side.FRONT);
        setBounds(0,0,mCardRegion.originalWidth, mCardRegion.originalHeight);
        setOrigin(mCardRegion.originalWidth/2.0f, mCardRegion.originalHeight/2.0f);
        mMaxCardSize = new Vector2(mCardRegion.originalWidth, mCardRegion.originalHeight).len();
        if (mCardMap == null)
            mCardMap = new OrderedMap<>();
        mCardMap.put(mCard, this);
        setName(mCard.toString());
    }

    public Card getCard() {
        return mCard;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.draw(mCardRegion,getX(), getY(),getOriginX(),getOriginY(),getWidth(),getHeight(),getScaleX(),getScaleY(),getRotation());
    }

    public void setSide(Side side) {
        mSide = side;
        mCardRegion = side == Side.FRONT ? mCardFace : CardUtils.getCardBackRegion();
    }

    public float getMaxCardSize() {
        return mMaxCardSize;
    }

    public static CardView getCardView(Card c) {
        if (mCardMap != null)
            return mCardMap.get(c);
        return null;
    }

        @Override
    public void drawDebug(ShapeRenderer shapes) {
        super.drawDebug(shapes);
        if (getZIndex() > 0) {
            shapes.setColor(Color.WHITE);
            shapes.set(ShapeRenderer.ShapeType.Filled);
            shapes.circle(getOriginX() + getX(), getOriginY() + getY(), 10.0f);
        }
    }
}
