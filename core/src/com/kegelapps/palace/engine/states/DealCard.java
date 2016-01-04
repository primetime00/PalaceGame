package com.kegelapps.palace.engine.states;

import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Deck;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.events.EventSystem;

/**
 * Created by Ryan on 12/23/2015.
 */
public class DealCard extends State{

    private Hand mHand;
    private Deck mDeck;
    private Card mCard;
    private Runnable mDoneRunnable;
    private CardState mCardState;

    private boolean mHidden;

    enum CardState {
        DEALING,
        ACCEPTING,
        DONE
    };


    public DealCard(Hand hand, Deck deck, boolean hidden, Runnable done) {
        super();
        mHand = hand;
        mDeck = deck;
        mDoneRunnable = done;
        mCardState = CardState.DEALING;
        mHidden = hidden;
    }

    @Override
    public boolean Run() {
        super.Run();
        switch (mCardState) {
            default:
            case DEALING:
                System.out.print("Dealing " + (mHidden ? "hidden" : "shown") + " card to player " + mHand.getID() + "\n");
                mCard = mDeck.Draw();
                Director.instance().getEventSystem().Fire(EventSystem.EventType.DEAL_CARD, mCard, mHand);
                mCardState = CardState.ACCEPTING;
                return false;
            case ACCEPTING:
                if (mCard == null)
                    throw new NullPointerException("Card is null!");
                if (mHidden)
                    mHand.AddHiddenCard(mCard);
                else
                    mHand.AddActiveCard(mCard);
                mCardState = CardState.DONE;
                return false;
            case DONE:
                if (mDoneRunnable != null) {
                    mDoneRunnable.run();
                }
                mHand = null;
                mDeck = null;
                mDoneRunnable = null;
                setStatus(Status.DONE);
                return true;
        }
    }
}
