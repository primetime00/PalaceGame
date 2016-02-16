package com.kegelapps.palace.engine;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.CardsProtos;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keg45397 on 12/7/2015.
 */
public class Table  implements Serializer{
    private Deck mDeck; //the deck on the table

    InPlay mPlayCards;

    List<Card> mBurntCards;

    //should the table have the hands?
    List<Hand> mHands;

    public interface TableListener {
        void onDealCard(Hand hand, Card c);
    }

    public Table (CardsProtos.Table tableProto) {
        mPlayCards = new InPlay();
        mBurntCards = new ArrayList<>();
        mHands = new ArrayList<>();
        ReadBuffer(tableProto);
    }

    public Table(Deck deck, int numberOfPlayers) {
        assert (numberOfPlayers != 3 || numberOfPlayers != 4);
        mDeck = deck;
        mPlayCards = new InPlay();
        mBurntCards = new ArrayList<>();
        mHands = new ArrayList<>();
        for (int i=0; i<numberOfPlayers; ++i) {
            mHands.add(new Hand(i, i==0 ? Hand.HandType.HUMAN : Hand.HandType.CPU));
        }
    }

    public List<Hand> getHands() {
        return mHands;
    }

    public InPlay getInPlay() { return mPlayCards; }

    public void DrawCard() {
        Card c = mDeck.Draw();
        mPlayCards.AddCard(c);
        Director.instance().getEventSystem().Fire(EventSystem.EventType.DRAW_PLAY_CARD, c);
    }

    public Card GetTopPlayCard() {
        return mPlayCards.GetTopCard();
    }

    public List<Card> GetPlayCards() {
        return mPlayCards.GetCards();
    }



    public Deck getDeck() {
        return mDeck;
    }

    public Logic.ChallengeResult AddPlayCard(Hand hand, Card activeCard) {
        Logic.ChallengeResult res = Logic.get().ChallengeCard(activeCard);
        if (res == Logic.ChallengeResult.FAIL) {
            Director.instance().getEventSystem().Fire(EventSystem.EventType.CARD_PLAY_FAILED, activeCard, hand);
        }
        else {
            mPlayCards.AddCard(activeCard);
            hand.RemoveCard(activeCard);
            Director.instance().getEventSystem().Fire(EventSystem.EventType.CARD_PLAY_SUCCESS, activeCard, hand);
        }
        if (res == Logic.ChallengeResult.SUCCESS_AGAIN && hand.HasAnyCards() == false)
            return Logic.ChallengeResult.SUCCESS;
        return res;
    }

    public void Burn() {
        mPlayCards.Burn();
    }

    public void PickUpStack(int id) {
        Hand h = getHands().get(id);
        h.PickUpStack(mPlayCards);
    }


    public void DrawEndTurnCards(int player) {
        Hand h = mHands.get(player);
        if (h == null)
            throw new RuntimeException(String.format("Could not find the hand %d", player));
        h.DrawEndTurnCards(mDeck);
    }



    @Override
    public void ReadBuffer(Message msg) {
        CardsProtos.Table table = (CardsProtos.Table) msg;
        mDeck = new Deck(table.getDeck());
        mPlayCards = new InPlay(table.getPlayed());
        mHands.clear();
        for (CardsProtos.Hand handProto : table.getHandsList()) {
            mHands.add(new Hand(handProto));
        }
    }

    @Override
    public Message WriteBuffer() {
        CardsProtos.Table.Builder tableBuilder = CardsProtos.Table.newBuilder();
        tableBuilder.setDeck((CardsProtos.Deck) mDeck.WriteBuffer());
        tableBuilder.setPlayed((CardsProtos.Played) mPlayCards.WriteBuffer());
        for (Hand hand : mHands) {
            tableBuilder.addHands((CardsProtos.Hand) hand.WriteBuffer());
        }
        return tableBuilder.build();
    }


}
