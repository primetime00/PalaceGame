package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.OrderedMap;
import com.google.protobuf.Message;
import com.kegelapps.palace.Serializer;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.protos.CardProtos;
import com.kegelapps.palace.protos.HandProtos;

/**
 * Created by keg45397 on 12/8/2015.
 */
public class CardView extends Actor implements Serializer{

    private Card mCard;
    private Side mSide;
    private TextureAtlas.AtlasRegion mCardRegion;
    private TextureAtlas.AtlasRegion mCardFace;
    private float mMaxCardSize;

    private HighlightView mHighlightView;

    public enum Side {
        FRONT,
        BACK
    }

    private static OrderedMap<Card, CardView> mCardMap;

    public CardView() {
        super();
    }

    public CardView(Card.Suit suit, Card.Rank rank) {
        super();
        mCard = new Card(suit, rank);
        setSide(Side.FRONT);
        mCardFace = CardUtils.getCardRegion(mCard.getSuit(), mCard.getRank());
        init();
    }

    public CardView(Card card) {
        super();
        assert(card == null);
        mCard = card;
        setSide(Side.FRONT);
        mCardFace = CardUtils.getCardRegion(mCard.getSuit(), mCard.getRank());
        init();
    }

    private void init() {
        setBounds(0,0,mCardRegion.originalWidth, mCardRegion.originalHeight);
        setOrigin(mCardRegion.originalWidth/2.0f, mCardRegion.originalHeight/2.0f);
        mMaxCardSize = new Vector2(mCardRegion.originalWidth, mCardRegion.originalHeight).len();
        if (mCardMap == null)
            mCardMap = new OrderedMap<>();
        mCardMap.put(mCard, this);
        mHighlightView = new HighlightView();
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


    public void setHighlight(boolean highlight) {
        if (highlight)
            mHighlightView.show();
        else
            mHighlightView.hide();
    }

    @Override
    public String getName() {
        if (mCard != null)
            return mCard.toString();
        return "";
    }

    @Override
    public void ReadBuffer(Message msg) {
        CardProtos.CardView cv = (CardProtos.CardView) msg;
        if (cv.hasX() && cv.hasY())
            setPosition(cv.getX(), cv.getY());
        mCard = new Card(Card.Suit.values()[cv.getCard().getSuit()], Card.Rank.values()[cv.getCard().getRank()]);
        mCardFace = CardUtils.getCardRegion(mCard.getSuit(), mCard.getRank());
        setSide(Side.values()[cv.getSide()]);
        init();
    }

    @Override
    public Message WriteBuffer() {
        com.kegelapps.palace.protos.CardProtos.CardView c = com.kegelapps.palace.protos.CardProtos.CardView.newBuilder()
                .setY(getY())
                .setX(getX())
                .setSide(mSide.ordinal())
                .setCard(CardProtos.CardView.Card.newBuilder()
                .setRank(mCard.getRank().ordinal())
                .setSuit(mCard.getSuit().ordinal()).build())
                .build();
        return c;
    }


}
