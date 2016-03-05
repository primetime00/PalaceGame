package com.kegelapps.palace.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;
import com.kegelapps.palace.graphics.MessageBandView;
import com.kegelapps.palace.graphics.ShadowView;

/**
 * Created by Ryan on 3/4/2016.
 */
public class MessageBandLoader extends SynchronousAssetLoader<MessageBandView.MessageBandTexture, MessageBandLoader.MessageBandParams> {
    public MessageBandLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public MessageBandView.MessageBandTexture load(AssetManager assetManager, String fileName, FileHandle file, MessageBandLoader.MessageBandParams parameter) {
        //not actually loading a file, but generating a pixmap texture
        Pixmap p = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.fillRectangle(0, 0, p.getWidth(), p.getHeight());
        MessageBandView.MessageBandTexture t = new MessageBandView.MessageBandTexture(p);
        return t;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, MessageBandLoader.MessageBandParams parameter) {
        return null;
    }

    static class MessageBandParams extends AssetLoaderParameters<MessageBandView.MessageBandTexture> {

    }

}
