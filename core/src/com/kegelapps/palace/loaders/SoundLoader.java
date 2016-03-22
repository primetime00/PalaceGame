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
import com.kegelapps.palace.loaders.types.SoundMap;

import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Ryan on 3/19/2016.
 */
public class SoundLoader extends SynchronousAssetLoader<SoundMap, SoundLoader.SoundParams> {

    public SoundLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public SoundMap load(AssetManager assetManager, String fileName, FileHandle file, SoundLoader.SoundParams parameter) {
        if (parameter == null)
            parameter = new SoundParams();
        SoundMap mSoundMap = new SoundMap();
        FileReader reader;
        XmlReader xmlReader = new XmlReader();
        XmlReader.Element root;
        try {
            reader = new FileReader(parameter.filename);
            root = xmlReader.parse(reader);
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not parse string xml");
        }
        String dir = root.getAttribute("directory", "");

        try {
            Array<XmlReader.Element> items = root.getChildrenByName("sounds");
            for (XmlReader.Element soundCategory : items) { //category
                String key = soundCategory.getAttribute("category", "");
                SoundMap.SoundList sList = new SoundMap.SoundList();
                for (XmlReader.Element soundFile : soundCategory.getChildrenByName("sound")) { //filename
                    String filename = soundFile.getAttribute("filename");
                    try {
                        sList.add(Gdx.audio.newSound(Gdx.files.internal(dir + "\\" + filename)));
                    } catch (GdxRuntimeException e) {
                        throw new RuntimeException(String.format("Could not load the sound file: %s\\%s", dir,filename));
                    }
                }
                mSoundMap.put(key, sList);
            }
            } catch (GdxRuntimeException e) {
                throw new RuntimeException("Could not parse the sound xml file.");
        }
        return mSoundMap;

    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, SoundLoader.SoundParams parameter) {
        return null;
    }

    static class SoundParams extends AssetLoaderParameters<SoundMap> {
        public String filename = "sounds.xml";
    }

}
