package com.kegelapps.palace.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.kegelapps.palace.loaders.types.UIAtlas;

/**
 * Created by keg45397 on 5/19/2016.
 */
public class UILoader extends SynchronousAssetLoader<UIAtlas, UILoader.UILoaderParams> {

    public UILoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public UIAtlas load(AssetManager assetManager, String fileName, FileHandle file, UILoaderParams parameter) {
        UIAtlas atlas;
        final String name = "ui.pack";
        if (parameter == null)
            parameter = new UILoaderParams();
        atlas = new UIAtlas(parameter.directory+"/"+name);
        return atlas;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, UILoaderParams parameter) {
        return null;
    }

    static class UILoaderParams extends AssetLoaderParameters<UIAtlas> {
        public String directory = "art/ui/";
    }
}
