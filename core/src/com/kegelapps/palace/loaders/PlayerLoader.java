package com.kegelapps.palace.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.kegelapps.palace.protos.PlayersProto;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by keg45397 on 3/15/2016.
 */
public class PlayerLoader extends SynchronousAssetLoader<ObjectMap, PlayerLoader.PlayerParam> {

    public PlayerLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, PlayerParam parameter) {
        return null;
    }

    @Override
    public ObjectMap<String, String> load(AssetManager assetManager, String fileName, FileHandle file, PlayerParam parameter) {
        if (parameter == null)
            parameter = new PlayerParam();

/*        JsonReader reader = new JsonReader();
        JsonValue root;
        root = reader.parse(file);*/
        InputStream inStream;
        PlayersProto.AllPlayers players;
        try {
            inStream = new FileInputStream(file.file());
            players = PlayersProto.AllPlayers.parseFrom(inStream);
            inStream.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not find the player file.");
        } catch (IOException e) {
            throw new RuntimeException("Could not parse the player file.");
        }

        ObjectMap<String, String> mStringMap = new ObjectMap<>();
        return mStringMap;
    }

    static public class PlayerParam extends AssetLoaderParameters<ObjectMap> {
    }

}
