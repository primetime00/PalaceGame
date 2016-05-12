package com.kegelapps.palace.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.kegelapps.palace.loaders.types.CardResource;

/**
 * Created by keg45397 on 3/1/2016.
 */
public class CardLoader extends SynchronousAssetLoader<CardResource, CardLoader.CardParams> {

    public CardLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public CardResource load(AssetManager assetManager, String fileName, FileHandle file, CardParams parameter) {
        if (parameter == null)
            parameter = new CardParams();
        CardResource r = new CardResource(parameter.directory);
        return r;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, CardParams parameter) {
        return null;
    }

    static public class CardParams extends AssetLoaderParameters<CardResource> {
        public String directory = "art/cards/";
    }

}
