package com.kegelapps.palace.engine;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.CardsProtos;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryan on 1/25/2016.
 */
public class InPlay implements Serializer{

    private List<Card> mCards;

    public InPlay() {
        mCards = new ArrayList<>();
    }
    public InPlay(CardsProtos.Played played) {
        mCards = new ArrayList<>();
        ReadBuffer(played);
    }

    public List<Card> GetCards() {
        return mCards;
    }

    public Card GetTopCard() {
        if (mCards.size() > 0)
            return mCards.get(mCards.size()-1);
        return null;
    }

    public void AddCard(Card c) {
        mCards.add(c);
        Director.instance().getEventSystem().Fire(EventSystem.EventType.INPLAY_CARDS_CHANGED);
        Logic.log().info(String.format("Adding card %s to in play pile", c));
    }

    public void Burn() {
        Director.instance().getEventSystem().Fire(EventSystem.EventType.BURN_CARDS, GetTopCard());
        Logic.log().info(String.format("Burning cards:"));
        for (Card c : mCards) {
            Logic.log().info(String.format("%s", c));
        }
        Clear();
    }

    @Override
    public void ReadBuffer(Message msg) {
        CardsProtos.Played played = (CardsProtos.Played) msg;
        mCards.clear();
        for (CardsProtos.Card protoCard : played.getCardsList()) {
            mCards.add(Card.GetCard(protoCard));
        }
        Logic.log().info(info());
    }

    @Override
    public Message WriteBuffer() {
        CardsProtos.Played.Builder builder = CardsProtos.Played.newBuilder();
        for (int i = 0; i<GetCards().size(); ++i) {
            Card c = GetCards().get(i);
            builder.addCards((CardsProtos.Card) c.WriteBuffer());
        }
        return builder.build();
    }

    public void Clear() {
        mCards.clear();
        Director.instance().getEventSystem().Fire(EventSystem.EventType.INPLAY_CARDS_CHANGED);
    }

    public String info() {
        String s = String.format("InPlay:\n");
        for (Card c : mCards) {
            s += String.format("\t%s\n", c);
        }
        return s;
    }


}
