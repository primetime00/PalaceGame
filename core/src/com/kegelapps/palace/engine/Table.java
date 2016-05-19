package com.kegelapps.palace.engine;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.utilities.Resettable;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.loaders.types.PlayerMap;
import com.kegelapps.palace.protos.CardsProtos;

import java.util.*;

/**
 * Created by keg45397 on 12/7/2015.
 */
public class Table  implements Serializer, Resettable{
    private Deck mDeck; //the deck on the table

    InPlay mPlayCards;

    List<Card> mBurntCards;
    List<Card> mUnplayableCards; //this is the top card just picked up.  It is unplayable in the same round it is picked up

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
        mUnplayableCards = new ArrayList<>();
        mHands = new ArrayList<>();
        ReadBuffer(tableProto);
        Director.instance().addResetter(this);
    }

    public Table(Deck deck, int numberOfPlayers) {
        assert (numberOfPlayers != 3 || numberOfPlayers != 4);
        mDeck = deck;
        mPlayCards = new InPlay();
        mBurntCards = new ArrayList<>();
        mUnplayableCards = new ArrayList<>();
        mHands = new ArrayList<>();
        createHands(numberOfPlayers);
        mFirstDealHand = 0;
        Director.instance().addResetter(this);
    }

    private void createHands(int numberOfPlayers) {
        List<Integer> ids = Director.instance().getAssets().get("players", PlayerMap.class).getRandomIDs();
        for (int i=0; i<numberOfPlayers; ++i) {
            Hand hand;
            if (Logic.get().debug() != null && Logic.get().debug().isCPUPlayOnly())
                hand = new Hand(i, Hand.HandType.CPU);
            else
                hand = new Hand(i, i==0 ? Hand.HandType.HUMAN : Hand.HandType.CPU);
            if (hand.getType() == Hand.HandType.CPU)
                hand.createAI(new Identity(ids.get(i)));
            mHands.add(hand);
        }
    }

    public void generateNewIdentities() {
        List<Integer> ids = Director.instance().getAssets().get("players", PlayerMap.class).getRandomIDs();
        int i=0;
        for (Hand hand : mHands) {
            if (hand.getType() == Hand.HandType.CPU) {
                hand.createAI(new Identity(ids.get(i)));
                ++i;
            }
        }
    }

    public void Load(CardsProtos.Table tableProto) {
        Reset(false);
        ReadBuffer(tableProto);
    }

    public List<Hand> getHands() {
        return mHands;
    }

    public InPlay getInPlay() { return mPlayCards; }

    public void DrawCard() {
        Card c = mDeck.Draw();
        Director.instance().getEventSystem().Fire(EventSystem.EventType.DRAW_PLAY_CARD, c);
        mPlayCards.AddCard(c);
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
            mNumberOfCardsPlayed++;
            boolean isTenBurn = activeCard.getRank() == Card.Rank.TEN && hand.GetPlayCards().GetPendingCards().isEmpty();
            boolean isNumberCardsBurn = mNumberOfCardsPlayed == 4;
            if (isTenBurn)
                mNumberOfCardsPlayed = 0;
            boolean isBurnPlay = isTenBurn || isNumberCardsBurn;
            Director.instance().getEventSystem().Fire(EventSystem.EventType.CARD_PLAY_SUCCESS, activeCard, hand, isBurnPlay);
            mPlayCards.AddCard(activeCard);
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
        boolean isTenBurn = activeCard.getRank() == Card.Rank.TEN && hand.GetPlayCards().GetPendingCards().isEmpty();
        boolean isBurnPlay = isTenBurn;
        Director.instance().getEventSystem().Fire(EventSystem.EventType.SUCCESS_HIDDEN_PLAY, hand.getID(), activeCard);
        mPlayCards.AddCard(activeCard);
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
        AddUnplayableCardsFromStack(h);
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
        mFirstDealHand = 0;
        if (table.hasCurrentTurn())
            mCurrentPlayTurn = table.getCurrentTurn();
        if (table.hasCurrentDeal())
            mCurrentDealTurn = table.getCurrentDeal();
        if (table.hasFirstDeal())
            mFirstDealHand = table.getFirstDeal();
        for (CardsProtos.Card c : table.getUnplayableList() ) {
            mUnplayableCards.add(Card.GetCard(c));
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
        tableBuilder.setCurrentTurn(mCurrentPlayTurn);
        tableBuilder.setCurrentDeal(mCurrentDealTurn);
        tableBuilder.setFirstDeal(mFirstDealHand);
        for (Card c : mUnplayableCards)
            tableBuilder.addUnplayable((CardsProtos.Card) c.WriteBuffer());
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

    public int GetNextPlayerId() {
        mNumberOfCardsPlayed = 0;
        int currentPlayer = getCurrentPlayTurn();
        int numberOfPlayersLeft = getHands().size();
        for (Hand h : mHands) {
            if (!h.HasAnyCards())
                numberOfPlayersLeft--;
        }
        if (numberOfPlayersLeft < 2)
            return -1;
        do { //are we out of the game?
            currentPlayer++;
            currentPlayer %= getHands().size();
        } while (!getHands().get(currentPlayer).HasAnyCards());
        return mHands.get(currentPlayer).getID();
    }

    public int GetNumberOfValidPlayers() {
        int v = 0;
        for (Hand h : mHands) {
            if (h.HasAnyCards())
                v++;
        }
        return v;
    }

    public boolean NextDealTurn() {
        mCurrentDealTurn++;
        mCurrentDealTurn%=mHands.size();
        return mCurrentDealTurn == mFirstDealHand;
    }

    public Hand GetFirstDealHand() {
        return getHands().get(mFirstDealHand);
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
    public void Reset(boolean newGame) {
        mPlayCards.Clear();
        mBurntCards.clear();
        for (Hand h : mHands) {
            h.Reset(newGame);
        }
        mDeck.Reset(newGame);
        mNumberOfCardsPlayed = 0;
        mCurrentPlayTurn = 0;
        mFirstDealHand++;
        mFirstDealHand %= mHands.size();
        mCurrentDealTurn = mFirstDealHand;
        mCurrentPlayTurn = mFirstDealHand + 1;
        mCurrentPlayTurn %= mHands.size();

        if (newGame)
            generateNewIdentities();
    }

    public boolean isEveryPlayerCPU() {
        for (Hand h : mHands) {
            if (h.getType() == Hand.HandType.HUMAN)
                return false;
        }
        return true;
    }

    /*
    public void AddUnplayableCardsFromStack() {
        if (mPlayCards.GetCards().size() == 0)
            return;
        Card c = mPlayCards.GetTopCard();
        ArrayList<Card> cards = (ArrayList<Card>) mPlayCards.GetCards();
        mUnplayableCards.clear();
        for (int i=cards.size()-1; i>=0; --i) {
            Card card = cards.get(i);
            if (card.getRank() == c.getRank())
                mUnplayableCards.add(card);
            else
                break;
        }
    }*/

    public void AddUnplayableCardsFromStack(Hand h) {
        if (mPlayCards.GetCards().size() == 0)
            return;
        mUnplayableCards.clear();
        //if (true)
//            return;
/*        if (h.GetActiveCards().isEmpty()) { //skip turn
            mUnplayableCards.addAll(mPlayCards.GetCards());
            return;
        }
        if (!h.HasHiddenCards() && !h.HasAllEndCards()) { //skip turn
            mUnplayableCards.addAll(mPlayCards.GetCards());
            mUnplayableCards.addAll(h.GetActiveCards());
            return;
        }
        if ( true ) {
            mUnplayableCards.addAll(mPlayCards.GetCards());
            mUnplayableCards.addAll(h.GetActiveCards());
            return;
        }*/
        boolean found;
        for (Card c1 : mPlayCards.GetCards()) {
            found = false;
            for (Card c2: h.GetActiveCards()) {
                if (c1.getRank() == c2.getRank()) {
                    found = true;
                    break;
                }
            }
            if (!found)
                mUnplayableCards.add(c1);
        }
    }


    public List<Card> GetUnplayableCards() {
        return mUnplayableCards;
    }

    public boolean IsUnplayable(Card card) {
        return mUnplayableCards.contains(card);
    }

    public boolean AllCardsUnplayable(int id) {
        Hand hand = GetHand(id);
        if (mUnplayableCards.isEmpty())
            return false;
        for (Card c : hand.GetActiveCards()) {
            if (!IsUnplayable(c))
                return false;
        }
        return true;
    }

    public void SkipTurn() {
        mUnplayableCards.clear();
    }

    public int GetRandomCPUHandID(int exclude) {
        ArrayList<Integer> vals = new ArrayList<>();
        for (Hand hand : getHands()) {
            if (hand.getType() == Hand.HandType.CPU && hand.getID() != exclude)
                vals.add(hand.getID());
        }
        if (vals.size() == 0)
            return -1;
        Random rn = new Random();
        int pick = rn.nextInt(vals.size());
        return vals.get(pick);
    }


    public int GetRandomCPUHandID() {
        return GetRandomCPUHandID(-1);
    }

    public int GetCurrentPlayerId() {
        return getHands().get(mCurrentPlayTurn).getID();
    }

    public int CountActivePlayers() {
        int res = 0;
        for (Hand h : mHands) {
            if (h.HasAnyCards())
                res++;
        }
        return res;
    }

    public ArrayList<Hand> GetActiveHands() {
        ArrayList<Hand> res = new ArrayList<>();
        for (Hand h : mHands) {
            if (h.HasAnyCards())
                res.add(h);
        }
        return res;
    }




}
