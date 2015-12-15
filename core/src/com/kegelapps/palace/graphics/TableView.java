package com.kegelapps.palace.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.kegelapps.palace.Card;
import com.kegelapps.palace.Hand;
import com.kegelapps.palace.Input;
import com.kegelapps.palace.Table;

/**
 * Created by keg45397 on 12/9/2015.
 */
public class TableView extends Sprite implements Input.BoundObject {

    private Table mTable;
    private DeckView mDeck;

    public TableView(Table table) {
        mTable = table;
        mDeck = new DeckView(table.getDeck());
        onScreenSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Input.get().addInputLogicAdapter("TableView", new Input.InputLogicAdapter(this) {
            @Override
            public void onTouched() {
                super.onTouched();
            }
        });

        Input.get().addInputLogicAdapter("TableView", new Input.InputLogicAdapter(mDeck) {
            @Override
            public void onTouched() {
                super.onTouched();
            }
        });

        mTable.setTableListener(new Table.TableListener() {
            @Override
            public void onDealCard(Hand hand, Card c) {
                CardView card = mDeck.getCardView(c);
                card.setPosition(mDeck.getX(), mDeck.getY());
            }
        });


    }

    public void onScreenSize(int w, int h) {
        mDeck.setPosition( (w - CardUtils.getCardTextureWidth())/2.0f, (h - CardUtils.getCardHeight())/2.0f);
        mDeck.setSize(CardUtils.getCardTextureWidth(), CardUtils.getCardHeight());
    }

    @Override
    public void draw(Batch batch) {
        mDeck.draw(batch);
        if (getTexture() != null)
            super.draw(batch);
    }

    @Override
    public Rectangle getBounds() {
        return getBoundingRectangle();
    }
}
