package com.kegelapps.palace.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.kegelapps.palace.BitmapFontWriter;

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

        BitmapFont fnt = null;
        //is the font there?
        try {
            fnt = new BitmapFont(getFontFile(fileName+".fnt"));
        } catch (GdxRuntimeException e) {
            fnt = generateFontWriteFiles(fileName, Gdx.files.internal(parameter.filename), parameter, 1024, 1024);
        }

        //BitmapFont fnt = generateFont(parameter);


        return fnt;
    }

    private BitmapFont generateFont(FontParams parameter) {
        generator = new FreeTypeFontGenerator(Gdx.files.internal(parameter.filename));
        param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        //param.size = (int) (parameter.size * Gdx.graphics.getDensity());
        param.size = parameter.size;
        if (parameter.border > 0) {
            param.borderWidth = parameter.border;// * Gdx.graphics.getDensity();
            param.borderColor = Color.BLACK;
        }
        BitmapFont fnt = generator.generateFont(param);
        generator.dispose();
        return fnt;
    }

    private BitmapFont generateFontWriteFiles(String fontName, FileHandle fontFile, FontParams param, int pageWidth, int pageHeight) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);

        FreeTypeFontGenerator.FreeTypeFontParameter ftfparam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        if (param.border > 0) {
            ftfparam.borderWidth = param.border;
            ftfparam.borderColor = Color.BLACK;
        }
        ftfparam.size = param.size;

        PixmapPacker packer = new PixmapPacker(pageWidth, pageHeight, Pixmap.Format.RGBA8888, 2, false);
        ftfparam.packer = packer;
        FreeTypeFontGenerator.FreeTypeBitmapFontData fontData = generator.generateData(ftfparam);

        Array<PixmapPacker.Page> pages = packer.getPages();
        TextureRegion[] texRegions = new TextureRegion[pages.size];
        for (int i=0; i<pages.size; i++) {
            PixmapPacker.Page p = pages.get(i);
            Texture tex = new Texture(new PixmapTextureData(p.getPixmap(), p.getPixmap().getFormat(), false, false, true)) {
                @Override
                public void dispose () {
                    super.dispose();
                    getTextureData().consumePixmap().dispose();
                }
            };
            tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            texRegions[i] = new TextureRegion(tex);
        }
        BitmapFont font = new BitmapFont(fontData, new Array<TextureRegion>(texRegions), false);
        saveFontToFile(font, param.size, fontName, packer);
        generator.dispose();
        packer.dispose();
        return font;
    }

    private boolean saveFontToFile(BitmapFont font, int fontSize, String fontName, PixmapPacker packer) {
        FileHandle fontFile = getFontFile(fontName + ".fnt"); // .fnt path
        FileHandle pixmapDir = getFontFile(fontName); // png dir path
        BitmapFontWriter.setOutputFormat(BitmapFontWriter.OutputFormat.Text);

        String[] pageRefs = BitmapFontWriter.writePixmaps(packer.getPages(), pixmapDir, fontName);
        // here we must add the png dir to the page refs
        for (int i = 0; i < pageRefs.length; i++) {
            pageRefs[i] = fontName + "/" + pageRefs[i];
        }
        BitmapFontWriter.writeFont(font.getData(), pageRefs, fontFile, new BitmapFontWriter.FontInfo(fontName, fontSize), 1, 1);
        return true;
    }

    private static FileHandle getFontFile(String filename) {
        return Gdx.files.local("generated-fonts/" + filename);
    }

    private BitmapFont findFont(String filename) {
        return null;
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
