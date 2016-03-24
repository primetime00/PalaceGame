package com.kegelapps.palace.loaders;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.google.protobuf.TextFormat;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.loaders.types.PlayerMap;
import com.kegelapps.palace.protos.PlayersProto;

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

        PlayersProto.AllPlayers.Builder builder = readData(parameter);

        PlayerMap mPlayerMap = new PlayerMap();

        readNames(parameter, builder, mPlayerMap);
        return mPlayerMap;
    }

    private void readNames(PlayerParam parameter, PlayersProto.AllPlayers.Builder builder, PlayerMap playerMap) {
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
            try {
                int key = Integer.parseInt(e.getAttribute("id"));
                XmlReader.Element name = e.getChildByName("name");
                String value = name.getText();
                mergeData(key, value, builder, playerMap);
            } catch (Exception ex) {
                throw new RuntimeException("Could not parse names xml");
            }
        }
    }

    private void mergeData(int key, String value, PlayersProto.AllPlayers.Builder builder, PlayerMap playerMap) {
        for (PlayersProto.Player.Builder pb : builder.getPlayersBuilderList()) {
            if (pb.getId() == key) {
                pb.setName(value);
                playerMap.put(key, pb.build());
                break;
            }
        }
    }

    private PlayersProto.AllPlayers.Builder readData(PlayerParam parameter) {
        FileReader reader;
        PlayersProto.AllPlayers.Builder builder = PlayersProto.AllPlayers.newBuilder();
        try {
            FileHandle h = Gdx.files.internal(parameter.dataFile);
            TextFormat.merge(h.reader(), builder);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not find the player file.");
        } catch (IOException e) {
            throw new RuntimeException("Could not parse the player file.");
        }
        return builder;
    }

    static public class PlayerParam extends AssetLoaderParameters<PlayerMap> {
        public String nameFile = "players.xml";
        public String dataFile = "players.dat";
    }

}
