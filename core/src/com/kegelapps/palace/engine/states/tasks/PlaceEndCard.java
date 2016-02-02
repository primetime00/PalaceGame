package com.kegelapps.palace.engine.states.tasks;

import com.badlogic.gdx.math.MathUtils;
import com.google.protobuf.Message;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.protos.StateProtos;

import java.util.Iterator;

/**
 * Created by keg45397 on 1/15/2016.
 */
public class PlaceEndCard extends State {

    private Table mTable;


    public PlaceEndCard(State parent, Table table) {
        super(parent);
        mTable = table;
    }

    @Override
    protected void OnFirstRun() {
        Hand mHand = mTable.getHands().get(0);
        System.out.print("Player " + mHand.getID() + " Select your 3 end cards.\n");
    }

    @Override
    protected boolean OnRun() {
        boolean res = true;
        for (int i=0; i<mTable.getHands().size(); ++i) {
            Hand mHand = mTable.getHands().get(i);
            if (!mHand.GetEndCards().contains(null)) //we have all of our cards
                continue;
            res = false;
            if (mHand.getType() == Hand.HandType.HUMAN) {
                for (Iterator<Card> it = mHand.GetPlayCards().GetPendingCards().iterator(); it.hasNext(); ) {
                    Card c = it.next();
                    it.remove();
                    Card activeCard = mHand.GetActiveCards().get(mHand.GetActiveCards().indexOf(c));
                    mHand.AddEndCard(activeCard);
                    System.out.print("HUMAN SELECTS " + c + "\n");
                }
                mHand.GetPlayCards().Reset();
                for (Iterator<Card> it = mHand.GetDiscardCards().iterator(); it.hasNext(); ) {
                    Card c = it.next();
                    it.remove();
                    Card activeCard = mHand.GetEndCards().get(mHand.GetEndCards().indexOf(c));
                    mHand.RemoveEndCard(activeCard);
                    System.out.print("HUMAN DESELECTS " + c + "\n");
                }
            } else {
                Card c = mHand.GetActiveCards().get(MathUtils.random(mHand.GetActiveCards().size() - 1));
                mHand.AddEndCard(c);
            }
        }
        if (res) {
            if (mStateListener != null)
                mStateListener.onContinueState();
        }
        return res;
    }

    @Override
    public Names getStateName() {
        return Names.PLACE_END_CARD;
    }


    @Override
    public Message WriteBuffer() {
        StateProtos.State s = (StateProtos.State) super.WriteBuffer();

        StateProtos.PlaceEndCardState.Builder builder = StateProtos.PlaceEndCardState.newBuilder();
        s = s.toBuilder().setExtension(StateProtos.PlaceEndCardState.state, builder.build()).build();
        return s;
    }

    @Override
    public void ReadBuffer(Message msg) {
        super.ReadBuffer(msg);
        StateProtos.PlaceEndCardState selectEndCardState = ((StateProtos.State) msg).getExtension(StateProtos.PlaceEndCardState.state);

    }

}
