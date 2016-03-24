package com.kegelapps.palace.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.kegelapps.palace.CardResource;
import com.kegelapps.palace.CoinResource;

/**
 * Created by keg45397 on 3/1/2016.
 */
public class CoinLoader extends SynchronousAssetLoader<CoinResource, CoinLoader.CoinParams> {

    public CoinLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public CoinResource load(AssetManager assetManager, String fileName, FileHandle file, CoinParams parameter) {
        if (parameter == null)
        parameter = new CoinParams();
        CoinResource r = new CoinResource(new TextureAtlas(file), parameter.scale);
        return r;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, CoinParams parameter) {
        return null;
    }

    static public class CoinParams extends AssetLoaderParameters<CoinResource> {
        public float scale = 0.5f;
    }

}
