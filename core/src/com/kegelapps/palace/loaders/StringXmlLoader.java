package com.kegelapps.palace.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.kegelapps.palace.CoinResource;

import java.io.IOException;

/**
 * Created by keg45397 on 3/15/2016.
 */
public class StringXmlLoader extends SynchronousAssetLoader<ObjectMap, StringXmlLoader.StringParam> {

    public StringXmlLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, StringParam parameter) {
        return null;
    }

    @Override
    public ObjectMap<String, String> load(AssetManager assetManager, String fileName, FileHandle file, StringParam parameter) {
        if (parameter == null)
            parameter = new StringParam();
        ObjectMap<String, String> mStringMap = new ObjectMap<>();
        XmlReader reader = new XmlReader();
        XmlReader.Element root;
        try {
            root = reader.parse(file);
        } catch (IOException e) {
            throw new RuntimeException("Could not parse string xml");
        }
        Array<XmlReader.Element> items = root.getChildrenByName("string");
        for (XmlReader.Element e : items) {
            String key = e.getAttribute("name", "");
            String value = e.getText().replace("\\n", "\n");
            mStringMap.put(key, value);
        }
        return mStringMap;
    }

    static public class StringParam extends AssetLoaderParameters<ObjectMap> {
    }

}
