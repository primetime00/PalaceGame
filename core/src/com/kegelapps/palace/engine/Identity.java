package com.kegelapps.palace.engine;

import com.kegelapps.palace.Director;
import com.kegelapps.palace.PlayerData;
import com.kegelapps.palace.engine.ai.EndCardSelection;
import com.kegelapps.palace.loaders.types.PlayerMap;
import com.kegelapps.palace.protos.PlayersProto;

/**
 * Created by keg45397 on 3/17/2016.
 */
public class Identity {
    private int id;
    private PlayerData mPlayerData;

    public Identity(int id) {
        this.id = id;
        mPlayerData = Director.instance().getAssets().get("players", PlayerMap.class).get(id);
    }



    public PlayerData get() { return mPlayerData;}
}
