package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Align;
import com.kegelapps.palace.CardResource;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.Resettable;
import com.kegelapps.palace.graphics.ui.common.StringMap;
import com.kegelapps.palace.scenes.GameScene;
import com.kegelapps.palace.animations.*;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.InPlay;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.states.Play;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.dealtasks.DrawPlayCard;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.utils.HandUtils;

/**
 * Created by Ryan on 1/25/2016.
 */
public class InPlayView extends Group implements ReparentViews, Resettable {

    InPlay mInPlayCards;
    final private int cardsHorizontal = 5;
    final private float overlapPercentX = 0.1f;
    final private float overlapPercentY = 0.15f;
    private DeckView mDeck;

    private int mCardWidth, mCardHeight;
    private Vector2 mNextPosition;

    private ActorGestureListener mGestureListener;

    private HighlightView mHighlightView;

    public InPlayView(InPlay play) {
        super();
        mInPlayCards = play;
        mCardHeight = Director.instance().getAssets().get("cards_tiny.pack", CardResource.class).getHeight();
        mCardWidth = Director.instance().getAssets().get("cards_tiny.pack", CardResource.class).getWidth();
        init();
    }

    private void init() {
        setWidth(mCardWidth);
        setHeight(mCardHeight);
        mHighlightView = new HighlightView();
        mHighlightView.setColor(Color.RED);
        mNextPosition = new Vector2();
        createGestures();
        createEvents();
    }

    public void setReferenceDeck(DeckView deck) {
        mDeck = deck;
    }

    private void createGestures() {
        mGestureListener = new ActorGestureListener() {

            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                super.tap(event, x, y, count, button);
                if (count >= 1) {
                    if (button == 0) {
                        Logic.get().Request(State.Names.PLAY_HUMAN_TURN, Logic.RequestType.SELECT_PLAYCARDS);
                    }
                }
            }
        };
        addListener(mGestureListener);
    }

    private void createEvents() {
        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.INPLAY_CARDS_CHANGED) {
            @Override
            public void handle(Object[] params) {
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.BURN_CARDS) {
            @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 1 || !(params[0] instanceof Card)) {
                    throw new IllegalArgumentException("Invalid parameters for BURN_CARDS");
                }
                if ( !(getParent() instanceof TableView) )
                    throw new RuntimeException("Cannot burn cards without a TableView parent");
                if ( !(getStage() instanceof GameScene) )
                    throw new RuntimeException("Burning cards requires a GameScene Stage");
                GameScene stage = (GameScene) getStage();
                TableView table = (TableView) getParent();
                Card topCard = (Card) params[0];
                Play pState = (Play) Logic.get().GetMainState().getState(State.Names.PLAY);
                DrawPlayCard dState = (DrawPlayCard) Logic.get().GetMainState().getState(State.Names.DRAW_PLAY_CARD);
                if (pState == null && dState == null) {
                    throw new RuntimeException("Burn Animation requires Play or DrawPlayCard state to be active!");
                }
                CardCamera.CameraSide side = CardCamera.CameraSide.BOTTOM;
                if (pState != null)
                    side = HandUtils.HandSideToCamera(HandUtils.IDtoSide(table.getTable().getCurrentPlayTurn(), table));
                int cardSize = mInPlayCards.GetCards().size();
                for (int i=0; i<cardSize; ++i) {
                    Card c = mInPlayCards.GetCards().get(i);
                    final CardView cv = CardView.getCardView(c);
                    AnimationBuilder burnBuilder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                    burnBuilder.setPause(true).setDescription("Burning cards").setTable(table).setCard(cv).setCamera(table.getCamera())
                            .setTweenCalculator(new CardAnimation.BurnCard())
                            .addStatusListener(new Animation.AnimationStatusListener() {
                                @Override
                                public void onEnd(Animation animation) {
                                    cv.remove(); //lets remove the card from the table!
                                }
                            });

                    if (i == cardSize-1 && table.getCamera().GetSide() != side) { //this is the last card
                        AnimationBuilder cameraBuilder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CAMERA);
                        cameraBuilder.setPause(true).setDescription("Move Back to hand").setTable(table).setCamera(table.getCamera()).
                                setCameraSide(side).setTweenCalculator(new CameraAnimation.MoveToSide(1.0f, 0.5f));
                        cameraBuilder.setEndDelay(0.5f);

                        burnBuilder.setNextAnimation(cameraBuilder.build());
                    }
                    //lets show the burn message!
                    String message = StringMap.getString("dramatic_burn");
                    if (topCard != null) {
                        if (topCard.getRank() == Card.Rank.TEN)
                            message = StringMap.getString("burn");
                        else
                            message = StringMap.getString("4_burn");
                    }
                    stage.ShowMessage(message, 1.0f, Color.FIREBRICK);

                    burnBuilder.build().Start();
                }
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.HIGHLIGHT_PLAY) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 1 || !(params[0] instanceof Boolean)) {
                    throw new IllegalArgumentException("Invalid parameters for HIGHLIGHT_PLAY");
                }
                setHighlight((Boolean) params[0]);
            }
        });


    }

    public Vector2 GetNextPosition() {
        int size = mInPlayCards.GetCards().size();
        int width = mCardWidth;
        int rows = (size/cardsHorizontal)+1;
        int currentRow = rows-1;
        int lastCol = size % cardsHorizontal;
        float height = (width * overlapPercentY * (currentRow));
        if (size == 0)
            return new Vector2(0,0);
        float y = -currentRow*width*overlapPercentY;
        float x;
        if (currentRow %2 == 0)
            x = width * overlapPercentX * lastCol;
        else
            x = width * overlapPercentX * (cardsHorizontal-(lastCol+1));
        y+=getHeight() - mCardHeight;
        return new Vector2(x,y);
    }

    public void OrganizeCards() {
        int size = mInPlayCards.GetCards().size();
        float width = Director.instance().getAssets().get("cards_tiny.pack", CardResource.class).getWidth();
        int rows = (size/cardsHorizontal)+1;
        int lastCol = size % cardsHorizontal;
        float height = (width * overlapPercentY * (size/(cardsHorizontal+1)));
        setHeight(height+mCardHeight);
        float startY = getHeight() - mCardHeight;
        int i = 0, x=0, y=0;
        int col = 0;
        for (y=0; y<rows; ++y) {
            if (y == rows-1)
                col = lastCol;
            else
                col = cardsHorizontal;
            if (y % 2 == 0) { //even
                for (x = 0; x < col; ++x) {
                    CardView cv = CardView.getCardView(mInPlayCards.GetCards().get(i));
                    HandUtils.Reparent(this, cv);
                    cv.setPosition(width * overlapPercentX * x, startY);
                    i++;
                }
            }
            else { //odd
                for (x = 0; x < col; ++x) {
                    CardView cv = CardView.getCardView(mInPlayCards.GetCards().get(i));
                    HandUtils.Reparent(this, cv);
                    cv.setPosition(width * overlapPercentX * (cardsHorizontal-1-x), startY);
                    i++;
                }
            }
            startY-=(width * overlapPercentY);
        }
        float nextX = 0.0f;
        float nextY = 0.0f;
        if (x < cardsHorizontal) {
            y = rows - 1;
            nextY = startY + (width * overlapPercentY);
            if (y % 2 == 0) //even
                nextX = width * overlapPercentX * x;
            else
                nextX = width * overlapPercentX * (cardsHorizontal-1-x);
        }
        else {
            x = 0;
            y = rows;
            nextY = startY;
            if (y % 2 == 0) //even
                nextX = width * overlapPercentX * x;
            else
                nextX = width * overlapPercentX * (cardsHorizontal-1-x);
        }
        mNextPosition.set(nextX, nextY);


        if (rows-1 == 0)
            setWidth(width + (width * overlapPercentX * (lastCol-1)));
        else
            setWidth(width + (width * overlapPercentX * (cardsHorizontal-1)));

        //reposition
        if (mDeck == null)
            return;
        setY(mDeck.getY() - (getHeight() - mCardHeight));
    }

    public Vector2 GetAbsoluteNextCardPosition() {
        Vector2 v = GetNextPosition();
        if (getParent() == null)
            return v;
        return localToAscendantCoordinates(getParent(), new Vector2(v));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (mHighlightView.isVisible())
            mHighlightView.draw(batch, this);

    }

    public InPlay getInPlay() {
        return mInPlayCards;
    }


    @Override
    public void ReparentAllViews() {
        OrganizeCards();
    }

    public void setHighlight(boolean highlight) {
        if (highlight)
            mHighlightView.show();
        else
            mHighlightView.hide();
    }

    @Override
    public String toString() {
        return "InPlayView";
    }

    @Override
    public void Reset() {
        for (Actor a : getChildren()) {
            a.remove();
        }
        getChildren().clear();
    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        super.drawDebug(shapes);
        shapes.setColor(Color.BLUE.r, Color.BLUE.g, Color.BLUE.b, 0.5f);
        shapes.set(ShapeRenderer.ShapeType.Filled);
        shapes.rect(getX(), getY(), getWidth(), getHeight());
        shapes.setColor(Color.YELLOW.r, Color.YELLOW.g, Color.YELLOW.b, 1.0f);
        shapes.set(ShapeRenderer.ShapeType.Filled);
//        Vector2 pos = (new Vector2(mTotalAreaRectangle.x+getX(), mTotalAreaRectangle.y+getY()));
//        shapes.rect(pos.x, pos.y, mTotalAreaRectangle.getWidth(), mTotalAreaRectangle.getHeight());

    }
}
