package com.kegelapps.palace.engine;

import com.google.protobuf.Message;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.protos.CardsProtos;

import java.util.ArrayList;
import java.util.Collections;
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
    }

    public void Burn() {
        Director.instance().getEventSystem().Fire(EventSystem.EventType.BURN_CARDS, GetTopCard());
        Clear();
    }

    @Override
    public void ReadBuffer(Message msg) {
        CardsProtos.Played played = (CardsProtos.Played) msg;
        mCards.clear();
        for (CardsProtos.Card protoCard : played.getCardsList()) {
            mCards.add(Card.GetCard(protoCard));
        }
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
}
