package com.kegelapps.palace.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Array;

/**
 * Created by keg45397 on 3/1/2016.
 */
public class FontLoader extends SynchronousAssetLoader<BitmapFont, FontLoader.FontParams> {

    private FreeTypeFontGenerator generator;
    private FreeTypeFontGenerator.FreeTypeFontParameter param;

    public FontLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public BitmapFont load(AssetManager assetManager, String fileName, FileHandle file, FontParams parameter) {
        generator = new FreeTypeFontGenerator(new FileHandle(fileName));
        param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        if (parameter == null)
            parameter = new FontParams();
        param.size = parameter.size;
        BitmapFont fnt = generator.generateFont(param);
        generator.dispose();
        return fnt;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, FontParams parameter) {
        return null;
    }

    static public class FontParams extends AssetLoaderParameters<BitmapFont> {
        public int size = 45;
    }
}
