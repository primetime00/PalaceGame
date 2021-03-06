package com.kegelapps.palace.engine;
import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.utilities.Resettable;
import com.kegelapps.palace.engine.ai.AI;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.CardsProtos;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by Ryan on 12/5/2015.
 */
public class Hand implements Serializer, Resettable{


    public enum HandType {
        HUMAN,
        CPU,
        MAX
    }
    private int mID;
    private HandType mType;

    private AI mAI;


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
                return Collections.emptyList();
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


    public Hand(int id, HandType type) {
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
            Logic.log().info(String.format("Hand %d adding hidden card %s", mID, card));
        }
    }

    public void AddActiveCard(Card card) {
        mActiveCards.add(card);
        sortActiveCards();
        Director.instance().getEventSystem().Fire(EventSystem.EventType.LAYOUT_ACTIVE_CARD, card, getID());
        Logic.log().info(String.format("Hand %d adding active card %s", mID, card));
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
            Logic.log().info(String.format("Hand %d adding end card %s", mID, card));
            Logic.log().info(String.format("--------------------------------------"));
            Logic.log().info(String.format("%s", info()));
            Logic.log().info(String.format("--------------------------------------"));
            Logic.log().info(String.format(" "));
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

    public byte[] hashString() {
        String text = "";
        for (Card c : GetActiveCards()) {
            text += c.getRank().toString();
            text += c.getSuit().toString();
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes("UTF-8"));
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new byte[] {0};
    }




    public List<Card> GetEndCards() {
        return Arrays.asList(mEndCards);
    }

    public HandType getType() {
        return mType;
    }

    public void setType( HandType type) {
        mType = type;
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

    public boolean HasEndCards() {
        for (int i=0; i<mEndCards.length; ++i)
            if (mEndCards[i] != null)
                return true;
        return false;
    }

    public boolean HasAllEndCards() {
        int count = 0;
        for (int i=0; i<mEndCards.length; ++i) {
            if (mEndCards[i] != null)
                count++;
            else
                return false;
        }
        return count == 3;
    }



    public boolean HasHiddenCards() {
        for (int i=0; i<mHiddenCards.length; ++i)
            if (mHiddenCards[i] != null)
                return true;
        return false;
    }

    public boolean HasAnyCards() {
        if (GetActiveCards().size() == 0 && !HasEndCards() && !HasHiddenCards())
            return false;
        return true;
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

    public void SelectEndPlayCard(Card c) {
        if (!GetActiveCards().isEmpty())
            return;
        if (!mPendingCards.GetAllCards().isEmpty())
            return;
        mPendingCards.AddCardAndTransfer(c);
    }

    public void SelectHiddenPlayCard(Card c) {
        if (!GetActiveCards().isEmpty())
            return;
        if (HasEndCards())
            return;
        if (!mPendingCards.GetAllCards().isEmpty())
            return;
        mPendingCards.AddCardAndTransfer(c);
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

    public void RemoveCard(Card card) {
        for (Iterator<Card> it = GetActiveCards().iterator(); it.hasNext();) {
            if (card == it.next()) {
                it.remove();
                return;
            }
        }
        for (int i=0; i<mEndCards.length; ++i) {
            if (card == mEndCards[i]) {
                mEndCards[i] = null;
                return;
            }
        }
        for (int i=0; i<mHiddenCards.length; ++i) {
            if (card == mHiddenCards[i]) {
                mHiddenCards[i] = null;
                return;
            }
        }
    }




    public PendingCards GetPlayCards() {
        return mPendingCards;
    }
    public List<Card> GetDiscardCards() {
        return mDiscardCards;
    }

    public boolean ContainsRank(Card.Rank rank) {
        if (HasEndCards() && GetActiveCards().size() == 0) {
            for (Card c : GetEndCards()) {
                if (c == null)
                    continue;
                if (c.getRank() == rank)
                    return true;
            }
        }
        else if (GetActiveCards().size() > 0) {
            for (Card c : GetActiveCards()) {
                if (c.getRank() == rank)
                    return true;
            }
        }
        return false;
    }

    public Card FindRank(Card.Rank rank) {
        if (HasEndCards() && GetActiveCards().size() == 0) {
            for (Card c : GetEndCards()) {
                if (c == null)
                    continue;
                if (c.getRank() == rank)
                    return c;
            }
        }
        else if (GetActiveCards().size() > 0) {
            for (Card c : GetActiveCards()) {
                if (c.getRank() == rank)
                    return c;
            }
        }
        return null;
    }


    public void DrawEndTurnCards(Deck deck) {
        if (deck.GetCards().isEmpty())
            return;
        int size = 3 - GetActiveCards().size();
        if (size <= 0) //we don't need cards right now
            return;
        List<Card> cards = new ArrayList<>();
        for (int i=0; i<size; ++i) {
            Card c = deck.Draw();
            if (c == null) //out of cards
                break;
            cards.add(c);
        }
        GetActiveCards().addAll(cards);
        sortActiveCards();
        Director.instance().getEventSystem().Fire(EventSystem.EventType.DRAW_TURN_END_CARDS, getID(), cards);
        Logic.log().info(String.format("Hand %d drawing turn cards", mID));
        for (Card c : cards) {
            Logic.log().info(String.format("  %s", c));
        }
    }

    public void PickUpStack(InPlay stack) {
        if (stack.GetCards().isEmpty())
            throw new RuntimeException("The stack can't be empty!");
        List<Card> cards = new ArrayList<>();
        for (int i=stack.GetCards().size()-1; i>=0; --i) {
            Card c = stack.GetCards().get(i);
            cards.add(c);
        }
        GetActiveCards().addAll(cards);
        sortActiveCards();
        stack.Clear();
        Director.instance().getEventSystem().Fire(EventSystem.EventType.PICK_UP_STACK, getID(), cards);
        Logic.log().info(String.format("Hand %d picking up stack cards", mID));
        for (Card c : cards) {
            Logic.log().info(String.format("  %s", c));
        }
    }

    public void createAI(Identity id) {
        mAI = new AI(id, this);
    }

    public Identity getIdentity() {
        if (mAI == null)
            return null;
        return mAI.getIdentity();
    }

    public AI GetAI() {
        return mAI;
    }

    public void sortActiveCards() {
        Logic.get().SortActiveCards(this);
    }


    @Override
    public void Reset(boolean newGame) {
        Arrays.fill(mHiddenCards, null);
        Arrays.fill(mEndCards, null);
        mActiveCards.clear();
        mPendingCards.Clear();
        mDiscardCards.clear();
        if (mAI != null)
            mAI.Reset(newGame);
    }




    @Override
    public void ReadBuffer(Message msg) {
        CardsProtos.Hand hand = (CardsProtos.Hand) msg;
        GetActiveCards().clear();
        GetDiscardCards().clear();
        GetPlayCards().Clear();
        mID = hand.getId();
        mType = HandType.values()[hand.getType()];
        if (mType == HandType.CPU) {
            mAI = new AI(hand.getAi(), this);
        }
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
        Logic.log().info(info());
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
        if (getType() == HandType.CPU)
            builder.setAi((CardsProtos.AI) mAI.WriteBuffer());
        return builder.build();
    }

    public String info() {
        String s = String.format("Hand %d %s\n", mID, getIdentity() != null ? getIdentity().get().getName() : "");
        s+="Active:\n";
        for (Card c : mActiveCards) {
            s += String.format("\t%s\n", c);
        }
        s+="End:\n";
        for (Card c : mEndCards) {
            if (c != null)
                s += String.format("\t%s\n", c);
        }
        s+="Hidden:\n";
        for (Card c : mHiddenCards) {
            if (c != null)
                s += String.format("\t%s\n", c);
        }
        return s;
    }
}
