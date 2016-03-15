package com.kegelapps.palace.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
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
        if (parameter == null)
            parameter = new FontParams();
        generator = new FreeTypeFontGenerator(new FileHandle(parameter.filename));
        param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = (int) (parameter.size * Gdx.graphics.getDensity());
        if (parameter.border > 0) {
            param.borderWidth = parameter.border * Gdx.graphics.getDensity();
            param.borderColor = Color.BLACK;
        }
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
        public int border = 0;
        public String filename = "FatCow.ttf";
    }
}
