package com.kegelapps.palace.engine;

import com.google.protobuf.Message;
import com.kegelapps.palace.graphics.utils.CardUtils;
import com.kegelapps.palace.graphics.utils.HandUtils;
import com.kegelapps.palace.protos.LogicProtos;

/**
 * Created by Ryan on 2/19/2016.
 */
public class Stats implements Serializer{
    private LogicProtos.Stats mStats;

    public Stats() {
        mStats = LogicProtos.Stats.newBuilder().setTotalRounds(0).build();
    }

    public void NextRound() {
        int r = mStats.getTotalRounds()+1;
        mStats = mStats.toBuilder().setTotalRounds(r).build();
    }

    public void SetWinner(CardUtils.CoinType coin, Hand hand) {
        LogicProtos.Stats.Builder statBuilder = mStats.toBuilder();
        LogicProtos.Placement.Builder placeBuild = LogicProtos.Placement.newBuilder();
        placeBuild.setCoinType(coin.ordinal());
        placeBuild.setHandID(hand.getID());
        placeBuild.setRounds(mStats.getTotalRounds());
        switch (coin) {
            case GOLD: statBuilder.setGoldHand(placeBuild.build()); break;
            case SILVER: statBuilder.setSilverHand(placeBuild.build()); break;
            case BRONZE: statBuilder.setBronzeHand(placeBuild.build()); break;
            default: break;
        }
        mStats = statBuilder.build();
    }

    @Override
    public void ReadBuffer(Message msg) {
        mStats = LogicProtos.Stats.newBuilder((LogicProtos.Stats) msg).build();
    }

    @Override
    public Message WriteBuffer() {
        return mStats;
    }

    public void DefineWinner(int id) {
        if (mStats.getGoldHand().getHandID() == -1) { //winner is first place
            SetWinner(CardUtils.CoinType.GOLD, Logic.get().GetTable().GetHand(id));
        }
        else if (mStats.getSilverHand().getHandID() == -1) { //winner is second place
            SetWinner(CardUtils.CoinType.SILVER, Logic.get().GetTable().GetHand(id));
        }
        else if (mStats.getBronzeHand().getHandID() == -1) { //winner is third place
            SetWinner(CardUtils.CoinType.BRONZE, Logic.get().GetTable().GetHand(id));
        }
    }

    public CardUtils.CoinType GetCoinType(int id) {
        if (mStats.getGoldHand().getHandID() == id)
            return CardUtils.CoinType.GOLD;
        if (mStats.getSilverHand().getHandID() == id)
            return CardUtils.CoinType.SILVER;
        if (mStats.getBronzeHand().getHandID() == id)
            return CardUtils.CoinType.BRONZE;
        return null;
    }
}
