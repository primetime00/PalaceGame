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
import com.kegelapps.palace.Director;
import com.kegelapps.palace.GameScene;
import com.kegelapps.palace.animations.*;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.InPlay;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.states.Play;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.tasks.DrawPlayCard;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.utils.CardUtils;
import com.kegelapps.palace.graphics.utils.HandUtils;

/**
 * Created by Ryan on 1/25/2016.
 */
public class InPlayView extends Group implements ReparentViews {

    Rectangle mPlayRectangle;
    InPlay mInPlayCards;
    final private int cardsHorizontal = 5;
    final private float overlapPercentX = 0.1f;
    final private float overlapPercentY = 0.15f;
    Vector2 mNextCardPosition;
    Rectangle mTotalAreaRectangle;

    private ActorGestureListener mGestureListener;

    private HighlightView mHighlightView;

    public InPlayView(InPlay play) {
        super();
        mInPlayCards = play;
        init();
    }

    private void init() {
        mNextCardPosition = new Vector2();
        mPlayRectangle = new Rectangle(0, 0, CardUtils.getCardWidth(), CardUtils.getCardHeight());
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
                if ( !(getParent() instanceof TableView) )
                    throw new RuntimeException("Cannot burn cards without a TableView parent");
                if ( !(getStage() instanceof GameScene) )
                    throw new RuntimeException("Burning cards requires a GameScene Stage");
                GameScene stage = (GameScene) getStage();
                TableView table = (TableView) getParent();
                Play pState = (Play) Logic.get().GetMainState().getState(State.Names.PLAY);
                DrawPlayCard dState = (DrawPlayCard) Logic.get().GetMainState().getState(State.Names.DRAW_PLAY_CARD);
                if (pState == null && dState == null) {
                    throw new RuntimeException("Burn Animation requires Play or DrawPlayCard state to be active!");
                }
                CardCamera.CameraSide side = CardCamera.CameraSide.BOTTOM;
                if (pState != null)
                    side = HandUtils.HandSideToCamera(HandUtils.IDtoSide(pState.getCurrentPlayer(), table));
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
                    String message = (dState != null ? "THAT'S A BURN!" : "BURN!");
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
        Vector2 res = CalculatePositionSizeForCard(mInPlayCards.GetCards().size());
        float x = res.x;
        float y = res.y;
        mPlayRectangle.setWidth(x+CardUtils.getCardWidth() - getX());
        mPlayRectangle.setHeight(y+CardUtils.getCardHeight() - getY());
        mNextCardPosition.set(x, y);
        mTotalAreaRectangle = calculateTotalSize(mPlayRectangle);
        if (mTotalAreaRectangle != null) {
            setPosition(mTotalAreaRectangle.x, mTotalAreaRectangle.y);
            setWidth(mTotalAreaRectangle.getWidth());
            setHeight(mTotalAreaRectangle.getHeight());
        }
    }

    private Rectangle calculateTotalSize(Rectangle mNext) {
        float x_min = 0x7FFFFFFF;
        float x_max = -1;
        float y_min = 0x7FFFFFFF;
        float y_max = -1;
        if (getChildren().size == 0 && mNext == null)
            return null;
        for (Actor a : getChildren()) {
            if (a.getX() < x_min)
                x_min = a.getX();
            if (a.getY() < y_min)
                y_min = a.getY();

            if (a.getRight() > x_max)
                x_max = a.getRight();
            if (a.getTop() > y_max)
                y_max = a.getTop();
        }
        if (mNext.getX() < x_min)
            x_min = mNext.getX();
        if (mNext.getY() < y_min)
            y_min = mNext.getY();

        if (mNext.getWidth()+mNext.getX() > x_max)
            x_max = mNext.getWidth()+mNext.getX();
        if (mNext.getY() + mNext.getHeight() > y_max)
            y_max = mNext.getY() + mNext.getHeight();

        return new Rectangle(x_min, y_min, x_max - x_min, y_max - y_min);
    }

    private Vector2 CalculatePositionSizeForCard(int index) {
        Vector2 res = new Vector2();
        float x = mPlayRectangle.getX();
        float y = mPlayRectangle.getY();
        int size = index;
        int left = size % cardsHorizontal;
        int down = size/cardsHorizontal;
        if (down % 2 == 0) //even
            x = x + (CardUtils.getCardWidth() * overlapPercentX * left);
        else //odd
            x = x + (CardUtils.getCardWidth() * overlapPercentX * (cardsHorizontal-left));
        y = y - (CardUtils.getCardWidth() * overlapPercentY * down);
        res.set(x, y);
        return res;
    }

    public Vector2 GetNextCardPosition() {
        return mNextCardPosition;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        //we don't want to apply a transform
        drawChildren(batch, parentAlpha);
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
}
