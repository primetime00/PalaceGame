package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.OrderedMap;
import com.kegelapps.palace.loaders.types.CardResource;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;

/**
 * Created by keg45397 on 12/8/2015.
 */
public class CardView extends Actor {

    private Card mCard;
    private TextureAtlas.AtlasRegion mCardRegion;
    private TextureAtlas.AtlasRegion mCardFace;
    private float mMaxCardSize;

    private HighlightView mHighlightView;
    private DisableView mDisableView;

    public enum Side {
        FRONT,
        BACK
    }

    private static OrderedMap<Card, CardView> mCardMap;

    public CardView(Card.Suit suit, Card.Rank rank) {
        super();
        mCard = Card.GetCard(suit, rank);
        init(false);
    }

    public CardView(Card card) {
        super();
        assert(card != null);
        mCard = card;
        init(false);
    }

    public CardView(Card card, boolean newInstance) {
        super();
        assert(card != null);
        mCard = card;
        init(newInstance);
    }


    private void init(boolean newInstance) {
        mCardFace = Director.instance().getAssets().get("cards", CardResource.class).getCard(mCard.getSuit(), mCard.getRank());
        setSide(Side.FRONT);
        setBounds(0,0,mCardRegion.originalWidth, mCardRegion.originalHeight);
        setOrigin(mCardRegion.originalWidth/2.0f, mCardRegion.originalHeight/2.0f);
        mMaxCardSize = new Vector2(mCardRegion.originalWidth, mCardRegion.originalHeight).len();
        if (!newInstance) {
            if (mCardMap == null)
                mCardMap = new OrderedMap<>();
            mCardMap.put(mCard, this);
        }
        mHighlightView = new HighlightView();
        mDisableView = new DisableView();
    }

    public Card getCard() {
        return mCard;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.draw(mCardRegion,getX(), getY(),getOriginX(),getOriginY(),getWidth(),getHeight(),getScaleX(),getScaleY(),getRotation());
        if (mHighlightView.isVisible()) {
            mHighlightView.draw(batch, this);
        }
        if (mCardRegion == mCardFace && mDisableView.isVisible()) //we are showing this card
            mDisableView.draw(batch, this);

    }

    public void setSide(Side side) {
        mCardRegion = side == Side.FRONT ? mCardFace : Director.instance().getAssets().get("cards", CardResource.class).getCardBack();
    }

    public float getMaxCardSize() {
        return mMaxCardSize;
    }

    public static CardView getCardView(Card c) {
        if (mCardMap != null) {
            CardView cv = mCardMap.get(c);
            if (cv == null) {//we don't have it, lets add it!
                cv = new CardView(c);
            }
            return cv;
        }
        throw new RuntimeException("The cardview map needs to be created first.");
    }


    public void setHighlight(boolean highlight) {
        if (highlight)
            mHighlightView.show();
        else
            mHighlightView.hide();
    }

    public void setDisabled(boolean disable) {
        if (disable)
            mDisableView.show();
        else
            mDisableView.hide();
    }


    @Override
    public String getName() {
        if (mCard != null)
            return mCard.toString();
        return "";
    }

    public static void Reset() {
        for (CardView c : mCardMap.values()) {
            if (c != null)
                c.remove();
        }
    }

    @Override
    public String toString() {
        String s;
        if (getParent() == null)
            return String.format("CardView: %s [none]", mCard);
        else if (getParent() instanceof HandView)
            return String.format("CardView: %s [%s - %d]", mCard, ((HandView)getParent()).getHand().getIdentity().get().getName(), ((HandView)getParent()).getHand().getID());
        else if (getParent() instanceof InPlayView)
            return String.format("CardView: %s [in play]", mCard);
        else if (getParent() instanceof TableView)
            return String.format("CardView: %s [table]", mCard);
        else
            return String.format("CardView: %s [%s]", mCard, getParent().getClass());

    }
}
