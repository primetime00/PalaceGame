package com.kegelapps.palace.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.kegelapps.palace.PlayerData;
import com.kegelapps.palace.loaders.types.PlayerMap;

import java.io.*;

/**
 * Created by keg45397 on 3/15/2016.
 */
public class PlayerLoader extends SynchronousAssetLoader<PlayerMap, PlayerLoader.PlayerParam> {

    public PlayerLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, PlayerParam parameter) {
        return null;
    }

    @Override
    public PlayerMap load(AssetManager assetManager, String fileName, FileHandle file, PlayerParam parameter) {
        if (parameter == null)
            parameter = new PlayerParam();

        PlayerMap mPlayerMap = new PlayerMap();

        readData(parameter, mPlayerMap);
        return mPlayerMap;
    }

    private void readData(PlayerParam parameter, PlayerMap playerMap) {
        FileReader reader;
        XmlReader xmlParser = new XmlReader();
        XmlReader.Element root;
        try {
            root = xmlParser.parse(Gdx.files.internal(parameter.nameFile).reader());
        } catch (IOException e) {
            throw new RuntimeException("Could not parse names xml");
        }
        Array<XmlReader.Element> items = root.getChildrenByName("player");
        for (XmlReader.Element e : items) {
            PlayerData data = new PlayerData(e);
            playerMap.put(data.getId(), data);
        }
    }

    static public class PlayerParam extends AssetLoaderParameters<PlayerMap> {
        public String nameFile = "players.xml";
    }

}
