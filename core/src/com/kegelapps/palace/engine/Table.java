package com.kegelapps.palace.engine;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.Resettable;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.CardsProtos;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keg45397 on 12/7/2015.
 */
public class Table  implements Serializer, Resettable{
    private Deck mDeck; //the deck on the table

    InPlay mPlayCards;

    List<Card> mBurntCards;

    //should the table have the hands?
    List<Hand> mHands;

    private int mCurrentPlayTurn = 1;
    private int mCurrentDealTurn = 0;
    private int mNumberOfCardsPlayed = 0;
    private int mFirstDealHand;

    public interface TableListener {
        void onDealCard(Hand hand, Card c);
    }

    public Table (CardsProtos.Table tableProto) {
        mPlayCards = new InPlay();
        mBurntCards = new ArrayList<>();
        mHands = new ArrayList<>();
        ReadBuffer(tableProto);
        mFirstDealHand = 0;
        Director.instance().addResetter(this);
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
        mFirstDealHand = 0;
        Director.instance().addResetter(this);
    }

    public void Load(CardsProtos.Table tableProto) {
        Reset();
        ReadBuffer(tableProto);
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
            hand.RemoveCard(activeCard);
            mPlayCards.AddCard(activeCard);
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

    public Logic.ChallengeResult AddHiddenPlayCard(Hand hand, Card activeCard) {
        Logic.ChallengeResult res = Logic.get().ChallengeCard(activeCard);
        hand.RemoveCard(activeCard);
        mPlayCards.AddCard(activeCard);
        boolean isTenBurn = activeCard.getRank() == Card.Rank.TEN && hand.GetPlayCards().GetPendingCards().isEmpty();
        boolean isBurnPlay = isTenBurn;
        Director.instance().getEventSystem().Fire(EventSystem.EventType.SUCCESS_HIDDEN_PLAY, hand.getID(), activeCard);
        if (isBurnPlay)
            res = Logic.ChallengeResult.SUCCESS_BURN;
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
            mCurrentPlayTurn = table.getCurrentTurn();
        if (table.hasCurrentDeal())
            mCurrentDealTurn = table.getCurrentDeal();
        if (table.hasFirstDeal())
            mFirstDealHand = table.getFirstDeal();
    }

    @Override
    public Message WriteBuffer() {
        CardsProtos.Table.Builder tableBuilder = CardsProtos.Table.newBuilder();
        tableBuilder.setDeck((CardsProtos.Deck) mDeck.WriteBuffer());
        tableBuilder.setPlayed((CardsProtos.Played) mPlayCards.WriteBuffer());
        for (Hand hand : mHands) {
            tableBuilder.addHands((CardsProtos.Hand) hand.WriteBuffer());
        }
        tableBuilder.setCurrentTurn(mCurrentPlayTurn);
        tableBuilder.setCurrentDeal(mCurrentDealTurn);
        tableBuilder.setFirstDeal(mFirstDealHand);
        return tableBuilder.build();
    }

    public boolean NextPlayTurn() {
        mNumberOfCardsPlayed = 0;
        int numberOfPlayersLeft = getHands().size();
        for (Hand h : mHands) {
            if (!h.HasAnyCards())
                numberOfPlayersLeft--;
        }
        if (numberOfPlayersLeft < 2)
            return false;
        do { //are we out of the game?
            mCurrentPlayTurn++;
            mCurrentPlayTurn %= getHands().size();
            if (mCurrentPlayTurn == ( (mFirstDealHand+1)%mHands.size() ))
                Logic.get().getStats().NextRound();
        } while (!getHands().get(mCurrentPlayTurn).HasAnyCards());
        return true;
    }

    public boolean NextDealTurn() {
        mCurrentDealTurn++;
        mCurrentDealTurn%=mHands.size();
        return mCurrentDealTurn == mFirstDealHand;
    }

    public int getCurrentPlayTurn() {
        return mCurrentPlayTurn;
    }
    public int getCurrentDealTurn() { return mCurrentDealTurn; }

    public Hand GetHand(int id) {
        for (Hand h : getHands()) {
            if (id == h.getID())
                return h;
        }
        return null;
    }

    @Override
    public void Reset() {
        mPlayCards.Clear();
        mBurntCards.clear();
        for (Hand h : mHands) {
            h.Reset();
        }
        mDeck.Reset();
        mNumberOfCardsPlayed = 0;
        mCurrentPlayTurn = 0;
        mFirstDealHand++;
        mFirstDealHand %= mHands.size();
        mCurrentDealTurn = mFirstDealHand;
        mCurrentPlayTurn = mFirstDealHand + 1;
        mCurrentPlayTurn %= mHands.size();
    }

}
