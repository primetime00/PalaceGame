package com.kegelapps.palace.engine.ai;

import com.badlogic.gdx.math.MathUtils;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by keg45397 on 3/22/2016.
 */
public class EndCardSelection {
    private Hand mHand;
    private ArrayList<EndCardsItem> mEndItems;

    private Comparator<EndCardSelection.EndCardsItem> mCompare;

    static class EndCardsItem {
        private int points;
        Card[] mCards;

        public EndCardsItem(int points, Card c1, Card c2, Card c3) {
            this.points = points;
            mCards = new Card[3];
            mCards[0] = c1;
            mCards[1] = c2;
            mCards[2] = c3;
        }

        public Card[] getCards() {
            return mCards;
        }

        public int getPoints() {
            return points;
        }
    }

    public EndCardSelection() {
        mCompare = new Comparator<EndCardsItem>() {
            @Override
            public int compare(EndCardsItem o1, EndCardsItem o2) {
                return o1.getPoints() - o2.getPoints();
            }
        };
    }

    public void SetHand(Hand h) {
        mHand = h;
    }

    public EndCardSelection(Hand hand) {
        this.mHand = hand;
        mCompare = new Comparator<EndCardsItem>() {
            @Override
            public int compare(EndCardsItem o1, EndCardsItem o2) {
                return o1.getPoints() - o2.getPoints();
            }
        };
    }

    public void SelectEndCards() {
        if (mEndItems == null)
            mEndItems = new ArrayList<>();
        mEndItems.clear();
        List<Card> mCards = mHand.GetActiveCards();
        for (int i=0; i<mCards.size()-2; ++i) { //          0 1 2 3 4
            for (int j=i+1; j<mCards.size()-1; ++j) {//     1 2 3 4 5
                for (int k=j+1; k<mCards.size(); ++k) {//   2 3 4 5 6
                    mEndItems.add(calculateCards(mCards.get(i), mCards.get(j), mCards.get(k)));
                }
            }
        }
    }

    public Card[] GetRandomCardSet(float percent, boolean top) {
        if (mEndItems == null)
            SelectEndCards();
        Collections.sort(mEndItems, mCompare);
        if (mEndItems == null || mEndItems.size() == 0)
            return null;
        int validSets = (int)Math.ceil(mEndItems.size() * percent);
        int set = (int)(Math.random() * validSets);
        if (!top)
            return mEndItems.get(set).getCards();
        else
            return mEndItems.get(mEndItems.size()-1-set).getCards();
    }

    private EndCardsItem calculateCards(Card c1, Card c2, Card c3) {
        int points = 0;
        for (Card.Rank r : Card.Rank.values()) {
            if (r == Card.Rank.TEN || r == Card.Rank.TWO)
                continue;
            Card top = new Card(Card.Suit.CLUB, r);
            if (Logic.get().TestCard(c1, top) != Logic.ChallengeResult.FAIL) {
                points++;
                if (c1.getRank() == c2.getRank())
                    points++;
                if (c1.getRank() == c3.getRank())
                    points++;
            }
            if (Logic.get().TestCard(c2, top) != Logic.ChallengeResult.FAIL) {
                points++;
                if (c2.getRank() == c1.getRank())
                    points++;
                if (c2.getRank() == c3.getRank())
                    points++;
            }
            if (Logic.get().TestCard(c3, top) != Logic.ChallengeResult.FAIL) {
                points++;
                if (c3.getRank() == c1.getRank())
                    points++;
                if (c3.getRank() == c2.getRank())
                    points++;
            }
        }
        return new EndCardsItem(points, c1,c2,c3);
    }
}
