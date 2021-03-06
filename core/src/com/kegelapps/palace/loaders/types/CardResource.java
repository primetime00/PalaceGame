package com.kegelapps.palace.loaders.types;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Disposable;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Card;

/**
 * Created by keg45397 on 3/1/2016.
 */
public class CardResource implements Disposable{

    public enum CardSize {
        TINY,
        SMALL,
        MEDIUM,
        LARGE
    }

    private TextureAtlas mCardAtlas;
    private CardSize mSize;

    private TextureAtlas.AtlasRegion mCardBack = null;

    private Pixmap mCardHighlight;
    private Texture mCardHighlightTexture;

    public CardResource(String directory) {
        String filename = directory;
        //figure this out
        float height = Director.instance().getViewHeight();
        if (height < 700) {
            mSize = CardSize.SMALL;
            filename += String.format("/%s", "cards_small.pack");
        }
        else if (height < 1000) {
            mSize = CardSize.MEDIUM;
            filename += String.format("/%s", "cards_medium.pack");
        }
        else {
            mSize = CardSize.LARGE;
            filename += String.format("/%s", "cards_large.pack");
        }

        this.mCardAtlas = new TextureAtlas(filename);


        int ov = (int)(getWidth() * 0.05f);
        mCardHighlight = new Pixmap(getWidth()-ov, getHeight()-ov, Pixmap.Format.RGBA8888);
        mCardHighlight.setColor(Color.WHITE);
        mCardHighlight.fillRectangle(0, 0, mCardHighlight.getWidth(), mCardHighlight.getHeight());
        mCardHighlightTexture = new Texture(mCardHighlight);
    }

    public TextureAtlas.AtlasRegion getCard(Card.Suit suit, Card.Rank rank) {
        String s = (Card.getRankString(rank)+"_of_"+Card.getSuitString(suit)+"s").toLowerCase();
        if ( rank == Card.Rank.JACK || rank == Card.Rank.QUEEN ||
                rank == Card.Rank.KING)
            s = s+"2";
        return mCardAtlas.findRegion(s);
    }

    public TextureAtlas.AtlasRegion getCardBack() {
        if (mCardBack == null)
            mCardBack = mCardAtlas.findRegion("card_back_logo");
        return mCardBack;
    }

    public TextureAtlas.AtlasRegion getCardBackDeck() {
        return mCardAtlas.findRegion("back_deck_logo");
    }

    public int getWidth() {
        return getCardBack().originalWidth;
    }

    public int getHeight() {
        return getCardBack().originalHeight;
    }

    public Texture getCardHighlight() {
        return mCardHighlightTexture;
    }

    @Override
    public void dispose() {
        mCardAtlas.dispose();
        mCardHighlight.dispose();
        mCardHighlightTexture.dispose();

        mCardBack = null;
        mCardAtlas = null;
        mCardHighlight = null;
        mCardHighlightTexture = null;
    }
}
