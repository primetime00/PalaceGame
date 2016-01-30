package com.kegelapps.palace.engine;
import com.google.protobuf.Message;
import com.kegelapps.palace.protos.CardsProtos;
import com.kegelapps.palace.protos.StatusProtos;

import java.util.*;

/**
 * Created by Ryan on 12/5/2015.
 */
public class Deck implements Serializer{

    private List<Card> mCards;

    private List<Card.Rank> mDebugRanks;

    public Deck() {
        mDebugRanks = new ArrayList<>();
        //mDebugRanks.add(Card.Rank.THREE);
        //mDebugRanks.add(Card.Rank.FOUR);
        //mDebugRanks.add(Card.Rank.FIVE);
        mCards = new ArrayList<>();

        if (mDebugRanks.size() > 0) { //this is debug mode
            for (Card.Suit s : Card.Suit.values()) {
                for (int rank = 0; rank < Card.Rank.values().length; ++rank) {
                    Card.Rank r = mDebugRanks.get(rank % mDebugRanks.size());
                    mCards.add(Card.GetCard(s, r));
                }
            }
        }
        else {
            for (Card.Suit s : Card.Suit.values()) {
                for (Card.Rank r : Card.Rank.values()) {
                    mCards.add(Card.GetCard(s, r));
                }
            }
        }
    }

    public Deck(CardsProtos.Deck deckProto) {
        mCards = new ArrayList<>();
        ReadBuffer(deckProto);
    }

    public void Shuffle() {
        long seed = System.nanoTime();
        Collections.shuffle(mCards, new Random(seed));
    }

    public Card Draw() {
        if (mCards.size() <= 0) {
            return null;
        }
        Card c = mCards.get(0);
        mCards.remove(0);
        return c;
    }

    public List<Card> GetCards() {
        return mCards;
    }

    public int CountCards() {
        return mCards.size();
    }

    @Override
    public Message WriteBuffer() {
        CardsProtos.Deck.Builder builder = CardsProtos.Deck.newBuilder();
        for (int i = GetCards().size()-1; i>=0; --i) {
            Card c = GetCards().get(i);
            builder.addCards((CardsProtos.Card) c.WriteBuffer());
        }
        return builder.build();
    }

    @Override
    public void ReadBuffer(Message msg) {
        CardsProtos.Deck deck = (CardsProtos.Deck) msg;
        mCards.clear();
        for (CardsProtos.Card protoCard : deck.getCardsList()) {
            mCards.add(Card.GetCard(Card.Suit.values()[protoCard.getSuit()], Card.Rank.values()[protoCard.getRank()]));
        }
    }
}
