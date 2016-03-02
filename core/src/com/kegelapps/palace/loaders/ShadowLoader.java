package com.kegelapps.palace.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.kegelapps.palace.graphics.ShadowView;

/**
 * Created by keg45397 on 3/2/2016.
 */
public class ShadowLoader extends SynchronousAssetLoader <ShadowView.ShadowTexture, ShadowLoader.ShadowParams> {
    public ShadowLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public ShadowView.ShadowTexture load(AssetManager assetManager, String fileName, FileHandle file, ShadowLoader.ShadowParams parameter) {
        //not actually loading a file, but generating a pixmap texture
        Pixmap p = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.fillRectangle(0, 0, p.getWidth(), p.getHeight());
        ShadowView.ShadowTexture t = new ShadowView.ShadowTexture(p);
        return t;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, ShadowLoader.ShadowParams parameter) {
        return null;
    }

    static class ShadowParams extends AssetLoaderParameters<ShadowView.ShadowTexture> {

    }
}
