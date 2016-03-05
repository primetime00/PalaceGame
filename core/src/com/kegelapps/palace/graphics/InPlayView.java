package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.kegelapps.palace.CardResource;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.Resettable;
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

    Rectangle mPlayRectangle;
    InPlay mInPlayCards;
    final private int cardsHorizontal = 5;
    final private float overlapPercentX = 0.1f;
    final private float overlapPercentY = 0.15f;
    Vector2 mNextCardPosition;
    Rectangle mTotalAreaRectangle;

    private int mCardWidth, mCardHeight;

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
        mNextCardPosition = new Vector2();
        mPlayRectangle = new Rectangle(0, 0, mCardWidth, mCardHeight);
        mHighlightView = new HighlightView();
        mHighlightView.setColor(Color.RED);
        createGestures();
        createEvents();
    }

    private void createGestures() {
        mGestureListener = new ActorGestureListener() {

            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                super.tap(event, x, y, count, button);
                if (count >= 1) {
                    if (button == 0) {
                        Logic.get().Request(State.Names.PLAY_HUMAN_TURN);
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

                CalculatePositionAndSize();
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
                    String message = "THAT'S A BURN!";
                    if (topCard != null) {
                        if (topCard.getRank() == Card.Rank.TEN)
                            message = "BURN!";
                        else
                            message = "4 IN A ROW BURN!";
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


    private void CalculatePositionAndSize() {
        Vector2 res = CalculatePositionSizeForCard(mInPlayCards.GetCards().size()-1);
        float x = res.x;
        float y = res.y;
        mPlayRectangle.setWidth(x+mCardWidth - getX());
        mPlayRectangle.setHeight(y+mCardHeight - getY());
        mNextCardPosition.set(x, y);
        mTotalAreaRectangle = calculateTotalSize();
        if (mTotalAreaRectangle != null) {
            Vector2 pos = this.localToAscendantCoordinates(getParent(), new Vector2(mTotalAreaRectangle.x, mTotalAreaRectangle.y));
            //setPosition(pos.x, pos.y);
            setWidth(mTotalAreaRectangle.getWidth());
            setHeight(mTotalAreaRectangle.getHeight());
        }
    }

    private Rectangle calculateTotalSize() {
        Rectangle totalRect = null;
        for (int i=mInPlayCards.GetCards().size(); i>0; i--) {
            Vector2 pos = CalculatePositionSizeForCard(i);
            if (i ==mInPlayCards.GetCards().size() )
                totalRect = new Rectangle(pos.x, pos.y, mCardWidth, mCardHeight);
            else
                totalRect = totalRect.merge(new Rectangle(pos.x, pos.y, mCardWidth, mCardHeight));
        }
        return totalRect;
    }
    public Vector2 CalculateAbsolutePositionSizeForCard(int index) {
        Vector2 v = CalculatePositionSizeForCard(index);
        v.add(getX(), getY());
        return v;
    }

    private Vector2 CalculatePositionSizeForCard(int index) {
        Vector2 res = new Vector2();
        float x = 0;//mPlayRectangle.getX();
        float y = 0;//mPlayRectangle.getY();
        int size = index;
        int left = size % cardsHorizontal;
        int down = size/cardsHorizontal;
        int width = Director.instance().getAssets().get("cards_tiny.pack", CardResource.class).getWidth();
        if (down % 2 == 0) //even
            x = x + (width * overlapPercentX * left);
        else //odd
            x = x + (width * overlapPercentX * (cardsHorizontal-left));
        y = y - (width * overlapPercentY * down);
        res.set(x, y);
        return res;
    }

    public Vector2 GetNextCardPosition() {
        return mNextCardPosition;
    }

    public Vector2 GetAbsoluteNextCardPosition() {
        if (getParent() == null)
            return mNextCardPosition;
        Vector2 pos = localToAscendantCoordinates(getParent(), new Vector2(mNextCardPosition));
        return pos;
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
        int i = 0;
        for (Actor c : getChildren()) {
            c.remove();
        }
        mPlayRectangle.setPosition(getX(), getY());
        for (Card c : mInPlayCards.GetCards()) {
            CardView cv = CardView.getCardView(c);
            addActor(cv);
            Vector2 pos = CalculatePositionSizeForCard(i++);
            cv.setPosition(pos.x, pos.y);
        }
        CalculatePositionAndSize();
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
        Vector2 v = CalculatePositionSizeForCard(0);
        mNextCardPosition.set(v.x, v.y);
    }
}
