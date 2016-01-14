package com.kegelapps.palace.engine;
import com.kegelapps.palace.Action;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.SelectEndCardAction;
import com.kegelapps.palace.events.EventSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Ryan on 12/5/2015.
 */
public class Hand {



    public enum HandType {
        HUMAN,
        CPU,
        MAX
    }
    private Deck mDeck; //the deck we are playing from
    private int mID;
    private HandType mType;

    private List<Card> mHiddenCards;
    private List<Card> mEndCards;
    private List<Card> mActiveCards;
    private List<Card> mPlayCards;
    private List<Card> mDiscardCards;


    public Hand(int id, HandType type, Deck deck, BlockingQueue queue) {
        mID = id;
        mType = type;
        mDeck = deck;
        mHiddenCards = new ArrayList<>();
        mEndCards = new ArrayList<>();
        mActiveCards = new ArrayList<>();
        mPlayCards = new ArrayList<>();
        mDiscardCards = new ArrayList<>();
    }

    public void AddHiddenCard(Card card) {
        mHiddenCards.add(card);
        Director.instance().getEventSystem().Fire(EventSystem.EventType.LAYOUT_HIDDEN_CARD, card, getID());
    }

    public void AddActiveCard(Card card) {
        mActiveCards.add(card);
        Collections.sort(mActiveCards);
        Director.instance().getEventSystem().Fire(EventSystem.EventType.LAYOUT_ACTIVE_CARD, card, getID());
    }

    public void AddEndCard(Card card) {
        getEndCards().add(card);
        getActiveCards().remove(card);
        Director.instance().getEventSystem().Fire(EventSystem.EventType.SELECT_END_CARD, card, getID(), getEndCards().size()-1);
    }

    public void RemoveEndCard(Card card) {
        getEndCards().remove(card);
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


    public List<Card> getActiveCards() {
        return mActiveCards;
    }

    public List<Card> getEndCards() {
        return mEndCards;
    }

    public HandType getType() {
        return mType;
    }

    public Deck getDeck() {
        return mDeck;
    }

    public List<Card> GetHiddenCards() {
        return mHiddenCards;
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

    public List<Card> getPlayCards() {
        return mPlayCards;
    }
    public List<Card> getDiscardCards() {
        return mDiscardCards;
    }
}
