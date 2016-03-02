package com.kegelapps.palace.engine;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.states.Play;
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

    private int mCurrentPlayer = 0;
    private int mNumberOfCardsPlayed = 0;

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
            if (mNumberOfCardsPlayed > 0) { //we've played 1 card
                if (mPlayCards.GetTopCard().getRank() != activeCard.getRank()) //we played a 2 first and then other cards
                    mNumberOfCardsPlayed = 0;
            }
            mPlayCards.AddCard(activeCard);
            hand.RemoveCard(activeCard);
            mNumberOfCardsPlayed++;
            boolean isTenBurn = activeCard.getRank() == Card.Rank.TEN && hand.GetPlayCards().GetPendingCards().isEmpty();
            boolean isNumberCardsBurn = mNumberOfCardsPlayed == 4;
            if (isTenBurn)
                mNumberOfCardsPlayed = 0;
            boolean isBurnPlay = isTenBurn || isNumberCardsBurn;
            Director.instance().getEventSystem().Fire(EventSystem.EventType.CARD_PLAY_SUCCESS, activeCard, hand, isBurnPlay);
            if (res != Logic.ChallengeResult.SUCCESS_BURN && isBurnPlay)
                res = Logic.ChallengeResult.SUCCESS_BURN;
        }
        if (res == Logic.ChallengeResult.SUCCESS_AGAIN && hand.HasAnyCards() == false)
            return Logic.ChallengeResult.SUCCESS;
        return res;
    }

    public void Burn() {
        mPlayCards.Burn();
        mNumberOfCardsPlayed = 0;
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
        if (table.hasCurrentTurn())
            mCurrentPlayer = table.getCurrentTurn();
    }

    @Override
    public Message WriteBuffer() {
        CardsProtos.Table.Builder tableBuilder = CardsProtos.Table.newBuilder();
        tableBuilder.setDeck((CardsProtos.Deck) mDeck.WriteBuffer());
        tableBuilder.setPlayed((CardsProtos.Played) mPlayCards.WriteBuffer());
        for (Hand hand : mHands) {
            tableBuilder.addHands((CardsProtos.Hand) hand.WriteBuffer());
        }
        tableBuilder.setCurrentTurn(mCurrentPlayer);
        return tableBuilder.build();
    }

    public boolean NextTurn() {
        mNumberOfCardsPlayed = 0;
        int prevTurn = mCurrentPlayer;
        do { //are we out of the game?
            mCurrentPlayer++;
            mCurrentPlayer %= getHands().size();
        } while (!getHands().get(mCurrentPlayer).HasAnyCards());
        if (mCurrentPlayer == prevTurn) //we are done, there is only one player left
            return false;
        if (prevTurn > mCurrentPlayer)
            Logic.get().getStats().NextRound();
        return true;
    }

    public int getCurrentPlayer() {
        return mCurrentPlayer;
    }

    public Hand GetHand(int id) {
        for (Hand h : getHands()) {
            if (id == h.getID())
                return h;
        }
        return null;
    }
}
