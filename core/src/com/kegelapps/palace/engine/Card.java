/**
 * Created by keg45397 on 12/3/2015.
 */

package com.kegelapps.palace.engine;

import com.google.protobuf.Message;
import com.kegelapps.palace.graphics.ui.common.StringMap;
import com.kegelapps.palace.protos.CardsProtos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Card implements Comparable<Card>, Serializer {

    static private int mCardNumberTotal = 0;
    static private Map<Suit, ArrayList<Card>> mCardMap;
    private int mCardNumber;

    public enum Suit {
        HEART,
        SPADE,
        CLUB,
        DIAMOND
    }

    public enum Rank {
        TWO, THREE, FOUR, FIVE, SIX, SEVEN,
        EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE
    }

    private  Suit mSuit;
    private  Rank mRank;

    public Card(Suit suit, Rank rank) {
        mSuit = suit;
        mRank = rank;
        init();
    }

    public static Card GetCard(Suit suit, Rank rank) {
        if (mCardMap == null)
            mCardMap = new HashMap<>();
        ArrayList<Card> cList = mCardMap.get(suit);
        if (cList == null)
            cList = new ArrayList<>();
        for (Card c : cList) {
            if (c.getRank() == rank)
                return c;
        }
        Card c = new Card(suit, rank);
        cList.add(c);
        mCardMap.put(suit, cList);
        return c;
    }

    public static List<Card> GetAllCards() {
        List cards = new ArrayList<>();
        for (Rank r : Rank.values()) {
            for (Suit s : Suit.values()) {
                cards.add(GetCard(s, r));
            }
        }
        return cards;
    }


    public static Card GetCard(Message c) {
        Card card = new Card(null, null);
        card.ReadBuffer(c);
        return GetCard(card.getSuit(), card.getRank());
    }

    private void init() {
        mCardNumber = mCardNumberTotal++;
        mCardNumberTotal = mCardNumberTotal % (Rank.values().length * Suit.values().length);
    }

    public Suit getSuit() {
        return mSuit;
    }

    public Rank getRank() {
        return mRank;
    }

    static public String getSuitString(Card.Suit suit) {
        switch (suit) {
            case HEART: return StringMap.getString("heart");
            case DIAMOND: return StringMap.getString("diamond");
            case SPADE: return StringMap.getString("spade");
            default:
            case CLUB: return StringMap.getString("club");
        }
    }

    @Override
    public int hashCode() {
        return (getSuit().ordinal() << 16) + getRank().ordinal();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Card)) return false;
        if (((Card) obj).getSuit() != getSuit()) return false;
        return ((Card) obj).getRank() == getRank();
    }

    public String getSuitString() {
        return getSuitString(mSuit);
    }

    private int getValue() {
        switch (mRank) {
            case TWO: return 12;
            case THREE: return 1;
            case FOUR: return 2;
            case FIVE: return 3;
            case SIX: return 4;
            case SEVEN: return 5;
            case EIGHT: return 6;
            case NINE: return 7;
            case TEN: return 12;
            case JACK: return 8;
            case QUEEN: return 9;
            case KING: return 10;
            default:
            case ACE: return 11;
        }
    }

    static public String getRankString(Card.Rank rank) {
        switch (rank) {
            case TWO: return "2";
            case THREE: return "3";
            case FOUR: return "4";
            case FIVE: return "5";
            case SIX: return "6";
            case SEVEN: return "7";
            case EIGHT: return "8";
            case NINE: return "9";
            case TEN: return "10";
            case JACK: return StringMap.getString("jack");
            case KING: return StringMap.getString("king");
            case QUEEN: return StringMap.getString("queen");
            default:
            case ACE: return StringMap.getString("ace");
        }
    }


    public String getRankString() {
        return getRankString(mRank);
    }

    @Override
    public String toString() {
        return getRankString() + " of " + getSuitString()+"s" + " [" + mCardNumber +"]";
    }

    @Override
    public int compareTo(Card o) {
        Integer value = getValue();
        return value.compareTo(o.getValue());
    }

    @Override
    public void ReadBuffer(Message msg) {
        CardsProtos.Card cardProto = (CardsProtos.Card) msg;
        mSuit = Suit.values()[cardProto.getSuit()];
        mRank = Rank.values()[cardProto.getRank()];
         //debuggin only
        /*
        if (mSuit == Suit.DIAMOND && mRank == Rank.TEN)
            mRank = Rank.THREE;
        else if (mSuit == Suit.DIAMOND && mRank == Rank.THREE)
            mRank = Rank.TEN;*/
    }

    @Override
    public Message WriteBuffer() {
        return CardsProtos.Card.newBuilder().setRank(getRank().ordinal()).setSuit(getSuit().ordinal()).build();
    }
}
