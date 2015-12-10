/**
 * Created by keg45397 on 12/3/2015.
 */

package com.kegelapps.palace;

public class Card implements Comparable<Card> {

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

    private Suit mSuit;
    private Rank mRank;

    public Card(Suit suit, Rank rank) {
        mSuit = suit;
        mRank = rank;
    }

    public Suit getSuit() {
        return mSuit;
    }

    public Rank getRank() {
        return mRank;
    }

    static public String getSuitString(Card.Suit suit) {
        switch (suit) {
            case HEART: return "Heart";
            case DIAMOND: return "Diamond";
            case SPADE: return "Spade";
            default:
            case CLUB: return "Club";
        }
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
            case JACK: return "Jack";
            case KING: return "King";
            case QUEEN: return "Queen";
            default:
            case ACE: return "Ace";
        }
    }


    public String getRankString() {
        return getRankString(mRank);
    }

    @Override
    public String toString() {
        return getRankString() + " of " + getSuitString()+"s";
    }

    @Override
    public int compareTo(Card o) {
        Integer value = getValue();
        return value.compareTo(o.getValue());
    }

}
