package com.kegelapps.palace.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.utils.Array;
import com.kegelapps.palace.Card;
import com.kegelapps.palace.Deck;
import com.kegelapps.palace.Input;

/**
 * Created by keg45397 on 12/9/2015.
 */
public class DeckView extends Actor implements Input.BoundObject {

    private Deck mDeck;

    private TextureAtlas.AtlasRegion mDeckBack;

    public DeckView() {
        super();
        mDeck = new Deck();
        init();
    }


    public DeckView(Deck deck) {
        super();
        assert (deck == null);
        mDeck = deck;
        init();
    }

    private void init() {
        mDeckBack = CardUtils.getCardBackDeckRegion();
        setBounds(0,0,mDeckBack.originalWidth, mDeckBack.originalHeight);
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                System.out.print("Down");
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                System.out.print("Up");
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if ( mDeck.CountCards() > 4 ) { //lets draw the stack of cards
            batch.draw(mDeckBack, getX(), getY());
        }
        /*
        else if (mDeck.CountCards() <= 4) { //we will draw cascaded cards?
            setTexture(null);
        }*/

    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle();
    }

}
