package com.kegelapps.palace.engine.ai;

import com.google.protobuf.Message;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Identity;
import com.kegelapps.palace.engine.Serializer;
import com.kegelapps.palace.protos.CardsProtos;

import java.util.ArrayList;

/**
 * Created by keg45397 on 3/22/2016.
 */
public class AI implements Serializer{
    private EndCardSelection mEndCardAI;
    private Identity mIdentity;
    private Hand mHand;
    private ArrayList<Card> mSelectedCards;

    public AI(Identity mIdentity, Hand parent) {
        this.mIdentity = mIdentity;
        mEndCardAI = new EndCardSelection(parent);
        mHand = parent;
        mSelectedCards = new ArrayList<>();
    }

    public AI(CardsProtos.AI proto, Hand parent) {
        mEndCardAI = new EndCardSelection(parent);
        mHand = parent;
        mSelectedCards = new ArrayList<>();
        ReadBuffer(proto);
    }


    public Identity getIdentity() {
        return mIdentity;
    }

    public void SelectEndCards() {
        mEndCardAI.SelectEndCards();
        Card[] cards = mEndCardAI.GetRandomCardSet(mIdentity.get().getEndCardSpread(), true);
        mSelectedCards.clear();
        for (Card c: cards)
            mSelectedCards.add(c);
    }

    public Card PopSelectedCard() {
        if (mSelectedCards.isEmpty())
            return null;
        Card c = mSelectedCards.get(0);
        mSelectedCards.remove(0);
        return c;
    }


    @Override
    public void ReadBuffer(Message msg) {
        CardsProtos.AI ai = (CardsProtos.AI) msg;
        mSelectedCards.clear();
        for (CardsProtos.Card protoCard : ai.getSelectedCardsList()) {
            mSelectedCards.add(Card.GetCard(protoCard));
        }
        mIdentity = new Identity(ai.getId());
    }

    @Override
    public Message WriteBuffer() {
        CardsProtos.AI.Builder builder = CardsProtos.AI.newBuilder();
        for (Card c : mSelectedCards) {
            builder.addSelectedCards((CardsProtos.Card) c.WriteBuffer());
        }
        builder.setId(getIdentity().get().getId());
        return builder.build();
    }
}
