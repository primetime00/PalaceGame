package com.kegelapps.palace.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.kegelapps.palace.*;
import com.kegelapps.palace.actions.GraphicActions;
import com.kegelapps.palace.events.TableEvent;
import sun.rmi.runtime.Log;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
import static com.badlogic.gdx.math.Interpolation.*;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.addAction;

/**
 * Created by keg45397 on 12/9/2015.
 */
public class TableView extends Group implements Input.BoundObject {

    private Table mTable;
    private DeckView mDeck;
    private Array<HandView> mHands;
    private Array<CardView> mCards;

    private Pixmap mPixmap;
    private Texture mBackground;

    public TableView(Table table) {
        mTable = table;
        mDeck = new DeckView(table.getDeck());
        mCards = new Array<>();
        mHands = new Array<>();
        for (Hand h : mTable.getHands()) {
            mHands.add(new HandView(h));
        }
        for (Card c : mTable.getDeck().GetCards()) {
            mCards.add(new CardView(c));
        }

        mPixmap = new Pixmap(Director.instance().getVirtualWidth(),Director.instance().getVirtualHeight(), Pixmap.Format.RGB888);
        mPixmap.setColor(Color.GREEN);
        mPixmap.fillRectangle(0, 0, mPixmap.getWidth(), mPixmap.getHeight());
        mBackground = new Texture(mPixmap);
        onScreenSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        addActor(mDeck);
        for (HandView hView : mHands){
            addActor(hView);
        }


        createTableEvents();

        float w = (Director.instance().getScreenWidth() - CardUtils.getCardTextureWidth())/2.0f;
        float h = (Director.instance().getScreenHeight() - CardUtils.getCardTextureHeight())/2.0f;
        mDeck.setPosition(w,h);
    }

    private void createTableEvents() {
        mTable.AddEvent(new TableEvent() {
            @Override
            public void onFirstCardDrawn(OrderedMap data) {
                CardView cardView = CardView.getCardView((Card) data.get("card"));
                cardView.setPosition(mDeck.getX(), mDeck.getY());
                cardView.setSide(CardView.Side.BACK);
                if (findActor(cardView.getCard().toString()) == null)
                    addActor(cardView);
                cardView.addAction(new GraphicActions(false).DrawToActive(mDeck, cardView));
            }

            @Override
            public void onCardDeal(OrderedMap<String, Object> data) {
                CardView cardView = CardView.getCardView((Card) data.get("card"));
                Hand hand =  (Hand) data.get("hand");
                cardView.setPosition(mDeck.getX(), mDeck.getY());
                cardView.setSide(CardView.Side.BACK);
                if (findActor(cardView.getCard().toString()) == null)
                    addActor(cardView);
                cardView.addAction(new GraphicActions(true).DealToHand(mDeck, hand, cardView));
            }
        });
    }

    public void onScreenSize(int w, int h) {
        //mDeck.setPosition( (w - CardUtils.getCardTextureWidth())/2.0f, (h - CardUtils.getCardHeight())/2.0f);
        //mDeck.setSize(CardUtils.getCardTextureWidth(), CardUtils.getCardHeight());
        setBounds(0,0,w,h);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(mBackground, 0, 0);
        super.draw(batch, parentAlpha);
    }



    @Override
    public Rectangle getBounds() {
        return new Rectangle(0,0,800,480);
    }
}
