package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.animations.*;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.InPlay;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.states.Play;
import com.kegelapps.palace.engine.states.State;
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

    public InPlayView(InPlay play) {
        super();
        mInPlayCards = play;
        init();
    }

    private void init() {
        mNextCardPosition = new Vector2();
        mPlayRectangle = new Rectangle(0, 0, CardUtils.getCardWidth(), CardUtils.getCardHeight());
        createEvents();
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
                TableView table = (TableView) getParent();
                Play pState = (Play) Logic.get().GetMainState().getState(State.Names.PLAY);
                if (pState == null)
                    throw new RuntimeException("Burn Animation requires Play state to be active!");
                CardCamera.CameraSide side = HandUtils.HandSideToCamera(HandUtils.IDtoSide(pState.getCurrentPlayer(), table));
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

                    if (i == cardSize-1) { //this is the last card
                        AnimationBuilder cameraBuilder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CAMERA);
                        cameraBuilder.setPause(true).setDescription("Move Back to hand").setTable(table).setCamera(table.getCamera()).
                                setCameraSide(side).setTweenCalculator(new CameraAnimation.MoveToSide(1.0f, 0.5f));

                        burnBuilder.setNextAnimation(cameraBuilder.build());
                    }
                    burnBuilder.build().Start();
                }
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
        setWidth(mPlayRectangle.getWidth());
        setHeight(mPlayRectangle.getHeight());
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
    public void act(float delta) {
        super.act(delta);
        mPlayRectangle.setPosition(getX(), getY());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        //we don't want to apply a transform
        drawChildren(batch, parentAlpha);
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
}
