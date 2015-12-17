package com.kegelapps.palace;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Ryan on 12/5/2015.
 */
public class Hand extends EventObject {

    enum HandType {
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

    private EndCardsListener mEndCardListener;

    //actions
    private SelectEndCardAction mEndCardAction;

    public interface EndCardsListener {
        void onEndCardDone(Hand hand);
    }


    public Hand(int id, HandType type, Deck deck, BlockingQueue queue) {
        mID = id;
        mType = type;
        mDeck = deck;
        mHiddenCards = new ArrayList<>();
        mEndCards = new ArrayList<>();
        mActiveCards = new ArrayList<>();

        SetupInput();
    }

    private void SetupInput() {
        mEndCardAction = new SelectEndCardAction(this, new Action.OnAction() {
            @Override
            public void onActionComplete() {
                if (mEndCardListener != null) {
                    mEndCardListener.onEndCardDone(Hand.this);
                }
            }
        });
    }

    public void SetEndCardListener(EndCardsListener listener) {
        mEndCardListener = listener;
    }

    public void AddHiddenCard(Card card) {
        mHiddenCards.add(card);
        AddParam("card", card);
        AddParam("hand", this);
        Trigger(EventType.LAYOUT_HIDDEN_CARD);
    }

    public void AddActiveCard(Card card) {
        mActiveCards.add(card);
    }

    public void SelectEndCards() {
        mEndCardAction.Poll();
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
}
