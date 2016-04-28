package com.kegelapps.palace.engine.ai;

import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by keg45397 on 4/26/2016.
 */
public class PlayCardSelection {
    private int mId;
    private Map<CardClassify, ArrayList<Card>> mClassMap;
    private int mNumberOfUnplayableCards;

    enum CardClassify {
        LOW,
        MEDIUM,
        HIGH,
        BEST
    }

    enum RankClassify {
        WORST,
        BEST,
        RANDOM
    }

    public PlayCardSelection(int handID) {
        mId = handID;
        mClassMap = new HashMap<>();
    }

    public Card SelectCard() {
        Hand mHand = Logic.get().GetTable().GetHand(mId);
        mNumberOfUnplayableCards = 0;
        ArrayList<Card> playableCards = getAvailablePlayCards();
        if (playableCards.isEmpty())
            return null;
        organizeCardClass(playableCards);

        if (mNumberOfUnplayableCards == 0 && !mHand.HasHiddenCards()) {
            Card c = winningPath(playableCards);
            if (c != null)
                return c;
        }


        int nextActiveCards = getNextActiveCards();
        int nextEndCards = getNextEndCards();
        int nextHiddenCards = getNextHiddenCards();
        if (nextEndCards != 0) {//we have end cards and all hidden cards
            return lowestCard();
        }
        if (nextHiddenCards > 1) { //has 2 or 3 hidden cards
            if (nextActiveCards < 3) //maybe we should play medium and work down
                return decreaseClass(CardClassify.MEDIUM, RankClassify.WORST);
            else
                return lowestCard();
        }
        else if (nextHiddenCards == 1) { //we are down to 1 hidden card
            if (nextActiveCards < 3) //This person is getting close to winning
                return decreaseClass(CardClassify.HIGH, RankClassify.WORST);
            else
                return lowestCard();
        }
        else if (nextHiddenCards == 0) { //Only active cards left!
            if (nextActiveCards < 3) //This person is getting close to winning
                return decreaseClass(CardClassify.HIGH, RankClassify.BEST);
            else
                return decreaseClass(CardClassify.HIGH, RankClassify.RANDOM);
        }
        return lowestCard();
    }

    private Card winningPath(ArrayList<Card> cards) {
        //check for burns/replays
        int normalCards = 0;
        for (Card c : cards) {
            if (c.getRank() != Card.Rank.TEN && c.getRank() != Card.Rank.TWO && countCards(c.getRank(), cards) != 4) {
                normalCards++;
                if (normalCards > 1)
                    return null;
            }
        }
        for (Card c : cards) {
            if (c.getRank() == Card.Rank.TEN || c.getRank() == Card.Rank.TWO || countCards(c.getRank(), cards) == 4) {
                return c;
            }
        }
        return null;
    }

    private int countCards(Card.Rank rank, ArrayList<Card> cards) {
        int count = 0;
        for (Card c : cards) {
            if (c.getRank() == rank)
                count++;
        }
        return count;
    }

    private Card decreaseClass(CardClassify cardClass, RankClassify rank) {
        for (int i=cardClass.ordinal(); i>=0; --i) {
            ArrayList<Card> cards = mClassMap.get(CardClassify.values()[i]);
            if (cards.size() > 0)
                return choseCard(rank, mClassMap.get(CardClassify.values()[i]));
        }
        //failed to find any cards, let's just default to the lowest card possible
        return lowestCard();
    }

    private Card choseCard(RankClassify rank, ArrayList<Card> cards) {
        switch (rank) {
            case WORST:
                return cards.get(0);
            case BEST:
                return cards.get(cards.size()-1);
            default:
            case RANDOM:
                int val = (int)(Math.random() * cards.size());
                return cards.get(val);
        }
    }

    private Card lowestCard() {
        for (int i=0; i<CardClassify.values().length; ++i) {
            ArrayList<Card> cards = mClassMap.get(CardClassify.values()[i]);
            if (cards.size() > 0)
                return cards.get(0);
        }
        throw new RuntimeException("There are no cards to play.");
    }

    private int getNextHiddenCards() {
        Hand mHand = getNextHand();
        int count = 0;
        for (int i=0; i<mHand.GetHiddenCards().toArray().length; ++i) {
            if (mHand.GetHiddenCards().toArray()[i] != null)
                count++;
        }
        return count;
    }

    private int getNextEndCards() {
        Hand mHand = getNextHand();
        int count = 0;
        for (int i=0; i<mHand.GetEndCards().toArray().length; ++i) {
            if (mHand.GetEndCards().toArray()[i] != null)
                count++;
        }
        return count;
    }

    private int getNextActiveCards() {
        Hand mHand = getNextHand();
        return mHand.GetActiveCards().size();
    }

    private Hand getNextHand() {
        int id = Logic.get().GetTable().GetNextPlayerId();
        if (id == -1)
            throw new RuntimeException("There is no next player, but there should be!");
        return Logic.get().GetTable().GetHand(id);
    }

    private void organizeCardClass(ArrayList<Card> cards) {
        mClassMap.clear();
        mClassMap.put(CardClassify.LOW, new ArrayList<Card>());
        mClassMap.put(CardClassify.MEDIUM, new ArrayList<Card>());
        mClassMap.put(CardClassify.HIGH, new ArrayList<Card>());
        mClassMap.put(CardClassify.BEST, new ArrayList<Card>());
        for (Card c : cards) {
            if (c.getRank() == Card.Rank.TWO || c.getRank() == Card.Rank.TEN)
                mClassMap.get(CardClassify.BEST).add(c);
            else if (c.getRank().compareTo(Card.Rank.SIX) < 0)
                mClassMap.get(CardClassify.LOW).add(c);
            else if (c.getRank().compareTo(Card.Rank.NINE) < 0)
                mClassMap.get(CardClassify.MEDIUM).add(c);
            else if (c.getRank().compareTo(Card.Rank.ACE) < 0)
                mClassMap.get(CardClassify.HIGH).add(c);
            else
                mClassMap.get(CardClassify.BEST).add(c);
        }
        if (mClassMap.get(CardClassify.BEST).size() > 1)
            Collections.sort(mClassMap.get(CardClassify.BEST));
    }

    private ArrayList<Card> getAvailablePlayCards() {
        ArrayList<Card> playableCards = new ArrayList<>();
        Hand mHand = Logic.get().GetTable().GetHand(mId);
        for (Card c : mHand.GetActiveCards()) {
            Logic.ChallengeResult playResult = Logic.get().ChallengeCard(c);
            if (playResult != Logic.ChallengeResult.FAIL)
                playableCards.add(c);
            else
                mNumberOfUnplayableCards++;
        }
        return playableCards;
    }


}
