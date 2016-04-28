package com.kegelapps.palace.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader;
import com.kegelapps.palace.loaders.types.MusicMap;

import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Ryan on 3/21/2016.
 */
public class MusicLoader extends SynchronousAssetLoader<MusicMap, MusicLoader.MusicParam> {

    public MusicLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public MusicMap load(AssetManager assetManager, String fileName, FileHandle file, MusicLoader.MusicParam parameter) {
        if (parameter == null)
            parameter = new MusicParam();
        MusicMap mMusicMap = new MusicMap();
        XmlReader xmlReader = new XmlReader();
        XmlReader.Element root;
        try {
            root = xmlReader.parse(Gdx.files.internal(parameter.filename).reader());
        } catch (IOException e) {
            throw new RuntimeException("Could not parse music xml");
        }
        String dir = root.getAttribute("directory", "");

        try {
            Array<XmlReader.Element> items = root.getChildrenByName("music");
            for (XmlReader.Element musicItem : items) { //category
                String key = musicItem.getAttribute("name", "");
                String value = musicItem.getAttribute("file", "");
                try {
                    mMusicMap.put(key, Gdx.audio.newMusic(Gdx.files.internal(dir + "\\" + value)));
                } catch (GdxRuntimeException e) {
                    throw new RuntimeException(String.format("Could not load the music file: %s\\%s", dir,value));
                }
            }
        } catch (GdxRuntimeException e) {
            throw new RuntimeException("Could not parse the music xml file.");
        }
        return mMusicMap;

    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, MusicLoader.MusicParam parameter) {
        return null;
    }

    static class MusicParam extends AssetLoaderParameters<MusicMap> {
        public String filename = "music.xml";
    }
}
