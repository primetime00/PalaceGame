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
    private PendingCards mPendingCards;
    private List<Card> mDiscardCards;

    static public class PendingCards {
        private List<Card> mReadyCards;
        private List<Card> mQueuedCards;
        private boolean mCommitted;

        public PendingCards() {
            mCommitted = false;
            mQueuedCards = new ArrayList<>();
            mReadyCards = null;
        }

        public Card PopCard() {
            if (mReadyCards == null || mReadyCards.isEmpty())
                return null;
            Card c = mReadyCards.get(0);
            mReadyCards.remove(0);
            if (mReadyCards.isEmpty())
                ResetQueue();
            return c;
        }

        public void TransferQueueToPlay() {
            mCommitted = true;
            mReadyCards = mQueuedCards;
        }
        public void ResetQueue() {
            mCommitted = false;
            mReadyCards = null;
        }

        public List<Card> GetPendingCards() {
            if (mReadyCards == null)
                return Collections.<Card>emptyList();
            return mReadyCards;
        }

        public void Clear() {
            mQueuedCards.clear();
            ResetQueue();
        }

        public List<Card> GetAllCards() {
            return mQueuedCards;
        }

        public boolean Committed() {
            return mCommitted;
        }

        public void AddCard(Card c) {
            if (mQueuedCards.size() == 0)
                ResetQueue();
            mQueuedCards.add(c);
        }

        public void AddCardAndTransfer(Card card) {
            mQueuedCards.add(card);
            TransferQueueToPlay();
        }
    }


    public Hand(int id, HandType type, Deck deck) {
        mID = id;
        mType = type;
        mHiddenCards = new Card[3];
        mEndCards = new Card[3];
        mActiveCards = new ArrayList<>();
        mPendingCards = new PendingCards();
        mPendingCards = new PendingCards();
        mDiscardCards = new ArrayList<>();
    }

    public Hand(CardsProtos.Hand hand) {
        mHiddenCards = new Card[3];
        mEndCards = new Card[3];
        mActiveCards = new ArrayList<>();
        mPendingCards = new PendingCards();
        mPendingCards = new PendingCards();
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
            Director.instance().getEventSystem().Fire(EventSystem.EventType.LAYOUT_HIDDEN_CARD, card, getID(), pos);
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
        mPendingCards.AddCardAndTransfer(c);
        if (mDiscardCards.contains(c))
            mDiscardCards.remove(c);
    }

    public void DeselectEndCard(Card c) {
        mDiscardCards.add(c);
        mPendingCards.Clear();
        //if (mPendingCards.GetPendingCards().contains(c))
        //    mPendingCards.GetPendingCards().remove(c);
    }

    public void SelectPlayCard(Card c) {
        if (!mPendingCards.GetAllCards().contains(c)) { //we selected a single card that was not in the selection
            if (!mPendingCards.GetAllCards().isEmpty()) //we have pending cards that need to be unselected
                mPendingCards.Clear();
            mPendingCards.AddCardAndTransfer(c);
        }
        else { //we selected a card from the current selection
            mPendingCards.TransferQueueToPlay();
        }
    }

    public void SelectAllPlayCard(Card c) {
        if (mPendingCards.GetAllCards().contains(c))
            return;
        UnSelectAllPlayCard();
        for (Card card : GetActiveCards()) {
            if (card.getRank() == c.getRank())
                mPendingCards.AddCard(card);
        }
        if (!mPendingCards.GetAllCards().isEmpty()) {
            Director.instance().getEventSystem().Fire(EventSystem.EventType.SELECT_MULTIPLE_CARDS, getID());
        }
    }

    public void UnSelectAllPlayCard() {
        if (!mPendingCards.GetAllCards().isEmpty()) {
            Director.instance().getEventSystem().Fire(EventSystem.EventType.UNSELECT_MULTIPLE_CARDS, getID());
            mPendingCards.Clear();
        }
    }




    public PendingCards GetPlayCards() {
        return mPendingCards;
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
        GetPlayCards().Clear();
        mID = hand.getId();
        mType = HandType.values()[hand.getType()];
        for (CardsProtos.Card protoCard : hand.getActiveCardsList()) {
            GetActiveCards().add(Card.GetCard(protoCard));
        }
        for (CardsProtos.Card protoCard : hand.getDiscarCardsList()) {
            GetDiscardCards().add(Card.GetCard(protoCard));
        }
        for (CardsProtos.Card protoCard : hand.getPlayCardsList()) {
            GetPlayCards().AddCard(Card.GetCard(protoCard));
        }
        if (hand.hasPlayCardsCommitted() && hand.getPlayCardsCommitted())
            GetPlayCards().TransferQueueToPlay();
        for (CardsProtos.PositionCard protoCard : hand.getHiddenCardsList()) {
            mHiddenCards[protoCard.getPosition()] = Card.GetCard(protoCard.getCard());
        }
        for (CardsProtos.PositionCard protoCard : hand.getEndCardsList()) {
            mEndCards[protoCard.getPosition()] = Card.GetCard(protoCard.getCard());
        }
        if (hand.hasPlayCardsCommitted() && hand.getPlayCardsCommitted())
            GetPlayCards().TransferQueueToPlay();
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
        for (Card c : GetPlayCards().GetAllCards()) {
            builder.addPlayCards((CardsProtos.Card) c.WriteBuffer());
        }
        builder.setPlayCardsCommitted(GetPlayCards().Committed());
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
