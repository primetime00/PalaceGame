package com.kegelapps.palace.engine;

import com.google.protobuf.Message;
import com.kegelapps.palace.protos.StatusProtos;

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
    public InPlay(StatusProtos.Played played) {
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
    }

    @Override
    public void ReadBuffer(Message msg) {
        StatusProtos.Played played = (StatusProtos.Played) msg;
        mCards.clear();
        for (StatusProtos.Card protoCard : played.getCardsList()) {
            mCards.add(new Card(protoCard));
        }
    }

    @Override
    public Message WriteBuffer() {
        StatusProtos.Played.Builder builder = StatusProtos.Played.newBuilder();
        for (int i = GetCards().size()-1; i>=0; --i) {
            Card c = GetCards().get(i);
            builder.addCards((StatusProtos.Card) c.WriteBuffer());
        }
        return builder.build();
    }
}
