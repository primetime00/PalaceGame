package com.kegelapps.palace.engine;
import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.CardsProtos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ryan on 12/5/2015.
 */
public class Hand implements Serializer{


    public enum HandType {
        HUMAN,
        CPU,
        MAX
    }
    private int mID;
    private HandType mType;

    private Card mHiddenCards[];
    private Card mEndCards[];
    private List<Card> mActiveCards;
    private List<Card> mPlayCards;
    private List<Card> mDiscardCards;


    public Hand(int id, HandType type, Deck deck) {
        mID = id;
        mType = type;
        mHiddenCards = new Card[3];
        mEndCards = new Card[3];
        mActiveCards = new ArrayList<>();
        mPlayCards = new ArrayList<>();
        mDiscardCards = new ArrayList<>();
    }

    public Hand(CardsProtos.Hand hand) {
        mHiddenCards = new Card[3];
        mEndCards = new Card[3];
        mActiveCards = new ArrayList<>();
        mPlayCards = new ArrayList<>();
        mDiscardCards = new ArrayList<>();
        ReadBuffer(hand);
    }

    public void AddHiddenCard(Card card) {
        int pos = -1;
        for (int i=0; i<3; ++i) {
            if (mHiddenCards[i] == null) {
                mHiddenCards[i] = card;
                pos = i;
                break;
            }
        }
        if (pos > -1) {
            Director.instance().getEventSystem().Fire(EventSystem.EventType.LAYOUT_HIDDEN_CARD, card, getID());
        }
    }

    public void AddActiveCard(Card card) {
        mActiveCards.add(card);
        Collections.sort(mActiveCards);
        Director.instance().getEventSystem().Fire(EventSystem.EventType.LAYOUT_ACTIVE_CARD, card, getID());
    }

    public void AddEndCard(Card card) {
        int pos = -1;
        for (int i=0; i<3; ++i) {
            if (mEndCards[i] == null) {
                mEndCards[i] = card;
                pos = i;
                break;
            }
        }
        if (pos > -1) {
            GetActiveCards().remove(card);
            Director.instance().getEventSystem().Fire(EventSystem.EventType.SELECT_END_CARD, card, getID(), pos);
        }
    }

    public void RemoveEndCard(Card card) {
        int i = GetEndCards().indexOf(card);
        mEndCards[i] = null;
        AddActiveCard(card);
    }

    @Override
    public String toString() {
        String s = "Active Cards: ";
        for (Card c : mActiveCards) {
            s+= c.toString() + "\n";
        }
        return s;
    }

    public int getID() {
        return mID;
    }


    public List<Card> GetEndCards() {
        return Arrays.asList(mEndCards);
    }

    public HandType getType() {
        return mType;
    }

    public List<Card> GetHiddenCards() {
        return Arrays.asList(mHiddenCards);
    }

    public int GetAvailableHiddenCardPosition() {
        for (int i=0; i<mHiddenCards.length; ++i)
            if (mHiddenCards[i] == null)
                return i;
        return -1;
    }

    public int GetAvailableEndCardPosition() {
        for (int i=0; i<mEndCards.length; ++i)
            if (mEndCards[i] == null)
                return i;
        return -1;
    }




    public List<Card> GetActiveCards() { return mActiveCards; }

    public void SelectEndCard(Card c) {
        mPlayCards.add(c);
        if (mDiscardCards.contains(c))
            mDiscardCards.remove(c);
    }

    public void DeselectEndCard(Card c) {
        mDiscardCards.add(c);
        if (mPlayCards.contains(c))
            mPlayCards.remove(c);
    }

    public void SelectPlayCard(Card c) {
        SelectEndCard(c);
    }


    public List<Card> GetPlayCards() {
        return mPlayCards;
    }
    public List<Card> GetDiscardCards() {
        return mDiscardCards;
    }

    public boolean ContainsRank(Card.Rank rank) {
        for (Card c : GetActiveCards()) {
            if (c.getRank() == rank)
                return true;
        }
        return false;
    }

    @Override
    public void ReadBuffer(Message msg) {
        CardsProtos.Hand hand = (CardsProtos.Hand) msg;
        GetActiveCards().clear();
        GetDiscardCards().clear();
        GetPlayCards().clear();
        mID = hand.getId();
        mType = HandType.values()[hand.getType()];

        for (CardsProtos.Card protoCard : hand.getActiveCardsList()) {
            GetActiveCards().add(Card.GetCard(Card.Suit.values()[protoCard.getSuit()], Card.Rank.values()[protoCard.getRank()]));
        }
        for (CardsProtos.Card protoCard : hand.getDiscarCardsList()) {
            GetDiscardCards().add(Card.GetCard(Card.Suit.values()[protoCard.getSuit()], Card.Rank.values()[protoCard.getRank()]));
        }
        for (CardsProtos.Card protoCard : hand.getPlayCardsList()) {
            GetPlayCards().add(Card.GetCard(Card.Suit.values()[protoCard.getSuit()], Card.Rank.values()[protoCard.getRank()]));
        }
        for (CardsProtos.PositionCard protoCard : hand.getHiddenCardsList()) {
            mHiddenCards[protoCard.getPosition()] = Card.GetCard(Card.Suit.values()[protoCard.getCard().getSuit()], Card.Rank.values()[protoCard.getCard().getRank()]);
        }
        for (CardsProtos.PositionCard protoCard : hand.getEndCardsList()) {
            mEndCards[protoCard.getPosition()] = Card.GetCard(Card.Suit.values()[protoCard.getCard().getSuit()], Card.Rank.values()[protoCard.getCard().getRank()]);
        }
    }

    @Override
    public Message WriteBuffer() {
        CardsProtos.Hand.Builder builder = CardsProtos.Hand.newBuilder();
        for (Card c : GetActiveCards()) {
            builder.addActiveCards((CardsProtos.Card) c.WriteBuffer());
        }
        for (Card c : GetDiscardCards()) {
            builder.addDiscarCards((CardsProtos.Card) c.WriteBuffer());
        }
        for (Card c : GetPlayCards()) {
            builder.addPlayCards((CardsProtos.Card) c.WriteBuffer());
        }
        for (int i = 0; i<mHiddenCards.length; ++i) {
            Card c = mHiddenCards[i];
            if (c != null)
                builder.addHiddenCards(CardsProtos.PositionCard.newBuilder().setPosition(i).setCard((CardsProtos.Card)c.WriteBuffer()).build());
        }
        for (int i = 0; i<mEndCards.length; ++i) {
            Card c = mEndCards[i];
            if (c != null)
                builder.addEndCards(CardsProtos.PositionCard.newBuilder().setPosition(i).setCard((CardsProtos.Card)c.WriteBuffer()).build());
        }
        builder.setId(getID());
        builder.setType(getType().ordinal());
        return builder.build();
    }
}
