package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.animations.Animation;
import com.kegelapps.palace.animations.AnimationBuilder;
import com.kegelapps.palace.animations.AnimationFactory;
import com.kegelapps.palace.animations.CardAnimation;
import com.kegelapps.palace.engine.Card;
import com.kegelapps.palace.engine.Hand;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.utils.CardUtils;
import com.kegelapps.palace.graphics.utils.HandUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keg45397 on 12/9/2015.
 */
public class HandView extends Group implements ReparentViews {

    private Hand mHand;
    private Rectangle mHiddenPositions[];
    private Rectangle mActivePosition;

    private float mCardOverlapPercent;
    private float mEndCardOverlapPercent;

    private ActorGestureListener mGestureListener;

    public HandView(Hand hand) {
        super();
        assert(hand == null);
        mHand = hand;

        init();
    }

    private void init() {
        mCardOverlapPercent = 0.75f;
        mEndCardOverlapPercent = 0.05f;

        setupHiddenLayout();
        setupActiveLayout();


        createHandEvents();

        mGestureListener = new ActorGestureListener() {
            @Override
            public void fling(InputEvent event, float velocityX, float velocityY, int button) {
                super.fling(event, velocityX, velocityY, button);
                if (event.getTarget() instanceof CardView) {
                    Card c = ((CardView)event.getTarget()).getCard();
                    if (getHand().GetActiveCards().contains(c) && velocityY > 200.0f) {
                        Logic.get().PlayerSelectCard(getHand(), c);
                    }
                    else if (getHand().GetEndCards().contains(c) && velocityY < -200.0f) {
                        Logic.get().PlayerUnselectCard(getHand(), c);
                    }
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                if ( !(event.getTarget() instanceof CardView) )
                    return;
                CardView cv = (CardView) event.getTarget();
                if (!getHand().GetPlayCards().GetAllCards().contains(cv.getCard())) {
                    Logic.get().PlayerUnSelectAllCards(getHand());
                }
            }

            @Override
            public boolean longPress(Actor actor, float x, float y) {
                boolean res = super.longPress(actor, x, y);
                Actor cardView = null;
                if (getActivePosition().contains(x, y)) {
                    cardView = hit(x, y, true);
                    if (cardView == null || !(cardView instanceof CardView))
                        return false;
                    Card c = ((CardView)cardView).getCard();
                    if (getHand().GetActiveCards().contains(c)) {
                        Logic.get().PlayerSelectAllCards(getHand(), c);
                    }
                }
                return res;
            }
        };
        if (getHand().getType() == Hand.HandType.HUMAN)
            addListener(mGestureListener);
    }

    public float getCardOverlapPercent() {
        return mCardOverlapPercent;
    }
    public float getEndCardOverlapPercent() { return mEndCardOverlapPercent; }

    private void setupActiveLayout() {
        int cardHeight = CardUtils.getCardHeight();
        int cardWidth = CardUtils.getCardWidth();
        switch (mHand.getID()) {
            default:
            case 0: //bottom
                mActivePosition = new Rectangle(0, 0, Director.instance().getScreenWidth(), cardHeight);
                mActivePosition.setY(-cardHeight - (cardHeight*0.10f));
                break;
            case 1: //left
                mActivePosition = new Rectangle(0, 0, cardHeight, Director.instance().getScreenHeight());
                mActivePosition.setX(-cardHeight - (cardHeight*0.10f));
                break;
            case 2: //top
                mActivePosition = new Rectangle(0, Director.instance().getScreenHeight()-cardHeight, Director.instance().getScreenWidth(), cardHeight);
                mActivePosition.setY(Director.instance().getScreenHeight()+ (cardHeight*0.10f));
                break;
            case 3: //right
                mActivePosition = new Rectangle(Director.instance().getScreenWidth()-cardHeight, 0, cardHeight, Director.instance().getScreenHeight());
                mActivePosition.setX(Director.instance().getScreenWidth()+ (cardHeight*0.10f));
                break;
        }
    }

    public Hand getHand() {
        return mHand;
    }

    private void setupHiddenLayout() {
        mHiddenPositions = new Rectangle[3];
        int cardHeight = CardUtils.getCardHeight();
        int cardWidth = CardUtils.getCardWidth();
        int screenWidth = Director.instance().getScreenWidth();
        int screenHeight = Director.instance().getScreenHeight();
        float cardGap = cardWidth * 0.1f;
        float hiddenWidth = cardWidth * 3 + cardGap * 2;
        float startX = (screenWidth - hiddenWidth) / 2.0f;
        float startY = (screenHeight - hiddenWidth) / 2.0f;
        float nextX = cardWidth + cardGap;
        for (int i=0; i<mHiddenPositions.length; ++i) {
            if (i == 0 || i == 2)
                mHiddenPositions[i] = new Rectangle(0, 0, cardWidth, cardHeight);
            else
                mHiddenPositions[i] = new Rectangle(0, 0, cardHeight, cardWidth);
        }
        switch (mHand.getID()) {
            default:
            case 0: //bottom
                mHiddenPositions[0].setPosition(startX, 0);
                mHiddenPositions[1].setPosition(startX + nextX, 0);
                mHiddenPositions[2].setPosition(startX + nextX + nextX, 0);
                break;
            case 1: //left
                mHiddenPositions[0].setPosition(0, startY);
                mHiddenPositions[1].setPosition(0, startY + nextX);
                mHiddenPositions[2].setPosition(0, startY + nextX + nextX);
                break;
            case 2: //top
                mHiddenPositions[0].setPosition(startX, screenHeight-cardHeight);
                mHiddenPositions[1].setPosition(startX + nextX, screenHeight-cardHeight);
                mHiddenPositions[2].setPosition(startX + nextX + nextX, screenHeight-cardHeight);
                break;
            case 3: //right
                mHiddenPositions[0].setPosition(screenWidth - cardHeight, startY);
                mHiddenPositions[1].setPosition(screenWidth - cardHeight, startY + nextX);
                mHiddenPositions[2].setPosition(screenWidth - cardHeight, startY + nextX + nextX);
                break;
        }
    }

    public Rectangle getHiddenPosition(int index) {
        if (index > 2 || index < 0)
            index = 0;
        return mHiddenPositions[index];
    }

    public Rectangle getActivePosition() {
        return mActivePosition;
    }


    private void createHandEvents() {

        EventSystem.EventListener mLayoutHiddenCardEventListener = new EventSystem.EventListener(EventSystem.EventType.LAYOUT_HIDDEN_CARD) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 3 || !(params[0] instanceof Card) || !(params[1] instanceof Integer) || !(params[2] instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid parameters for LAYOUT_HIDDEN_CARD");
                }

                int id = (int) params[1];
                if (getHand().getID() != id)
                    return;
                int pos = (int) params[2];

                CardView cardView = CardView.getCardView((Card) params[0]);

                cardView.getParent().removeActor(cardView);
                addActor(cardView);

                if (getParent() instanceof TableView) {
                    AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                    builder.setPause(false).setDescription("Lining up hidden cards").setTable((TableView) getParent()).setCard(cardView).setHandID(getHand().getID())
                            .setTweenCalculator(new CardAnimation.LineUpHiddenCards(pos)).build().Start();
                }
                cardView.setZIndex(0);
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mLayoutHiddenCardEventListener);

        EventSystem.EventListener mLayoutActiveCardEventListener = new EventSystem.EventListener(EventSystem.EventType.LAYOUT_ACTIVE_CARD) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 2 || !(params[0] instanceof Card) || !(params[1] instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid parameters for LAYOUT_ACTIVE_CARD");
                }

                int id = (int) params[1];
                if (getHand().getID() != id)
                    return;

                CardView cardView = CardView.getCardView((Card) params[0]);
                cardView.getParent().removeActor(cardView);
                addActor(cardView);

                OrganizeCards(true);
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mLayoutActiveCardEventListener);

        EventSystem.EventListener mSelectEndCardEventListener = new EventSystem.EventListener(EventSystem.EventType.SELECT_END_CARD) {
            @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 3 || !(params[0] instanceof Card) || !(params[1] instanceof Integer) || !(params[2] instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid parameters for SELECT_END_CARD");
                }
                int id = (int) params[1];
                if (getHand().getID() != id)
                    return;

                int pos = (int) params[2];

                CardView cardView = CardView.getCardView((Card) params[0]);

                if (getParent() instanceof TableView) {
                    AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                    builder.setPause(false).setDescription("Selecting end cards").setTable((TableView) getParent()).setCard(cardView).setHandID(getHand().getID())
                            .setTweenCalculator(new CardAnimation.SelectEndCard(pos)).build().Start();

                    CardAnimation ca = (CardAnimation) builder.build();
                    int i = 5;
                    i++;
                }

                OrganizeCards(true, true, true, false);
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mSelectEndCardEventListener);

        EventSystem.EventListener mSelectMultipleCards = new EventSystem.EventListener(EventSystem.EventType.SELECT_MULTIPLE_CARDS) {
                @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 1 || !(params[0] instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid parameters for SELECT_MULTIPLE_CARDS");
                }
                int id = (int) params[0];
                if (getHand().getID() != id)
                    return;

                if (getParent() instanceof TableView) {
                    for (Card c : mHand.GetPlayCards().GetAllCards()) {
                        CardView cardView = CardView.getCardView(c);
                        AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                        builder.setPause(false).setDescription("Selecting multiple cards").setTable((TableView) getParent()).setCard(cardView).setHandID(getHand().getID())
                                .setTweenCalculator(new CardAnimation.SelectPendingCard()).build().Start();
                    }
                }
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mSelectMultipleCards);

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.UNSELECT_MULTIPLE_CARDS) {
            @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 1 || !(params[0] instanceof Integer)) {
                    throw new IllegalArgumentException("Invalid parameters for SELECT_MULTIPLE_CARDS");
                }
                int id = (int) params[0];
                if (getHand().getID() != id)
                    return;

                if (getParent() instanceof TableView) {
                    for (Card c : mHand.GetPlayCards().GetAllCards()) {
                        CardView cardView = CardView.getCardView(c);
                        Director.instance().getTweenManager().killTarget(cardView);
                    }
                }
                OrganizeCards(true, true, true, true);
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.DRAW_TURN_END_CARDS) {
            @Override
            public void handle(Object[] params) {
                if (params == null || params.length != 2 || !(params[0] instanceof Integer) || !(params[1] instanceof ArrayList)) {
                    throw new IllegalArgumentException("Invalid parameters for DRAW_TURN_END_CARDS");
                }
                int id = (int) params[0];
                if (getHand().getID() != id)
                    return;
                if (!(getParent() instanceof TableView)) {
                    throw new RuntimeException("Hand must be in a TableView!");
                }
                TableView table = (TableView) getParent();
                List<Card> cards = (List<Card>) params[1];
                for (int i = 0; i< cards.size(); ++i) {
                    Card c = cards.get(i);
                    CardView cardView = CardView.getCardView(c);
                    cardView.remove();
                    addActor(cardView);
                    AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                    builder.setPause(true).setDescription("Drawing and End card").setTable(table).setCard(cardView).setHandID(id);
                    builder.setStartDelay(0.4f);
                    if (i == cards.size()-1) { //last card
                        builder.addStatusListener(new Animation.AnimationStatusListener() {
                            @Override
                            public void onEnd(Animation animation) {
                                OrganizeCards(true, true, false, false);
                            }
                        });
                    }
                    builder.setTweenCalculator(new CardAnimation.DrawEndTurnCard());
                    builder.build().Start();

                }
            }
        });
    }

    public void OrganizeCards(boolean animation) {
        OrganizeCards(animation, true, true, true);
    }

    public void OrganizeCards(boolean animation, boolean active, boolean hidden, boolean end) {
        int zIndex = 0;
        int size = getHand().GetActiveCards().size();
        Rectangle r = getActivePosition();
        float width = (( (size-1) * CardUtils.getCardWidth()) * mCardOverlapPercent) + CardUtils.getCardWidth();
        if (getHand().getID() == 0 || getHand().getID() == 2) {
            r.setWidth(width);
            r.setX( (Director.instance().getScreenWidth() - r.getWidth()) /2.0f);
        }
        else {
            r.setHeight(width);
            r.setY( (Director.instance().getScreenHeight() - r.getHeight()) /2.0f);
        }

        ZSortAllViews();

        if (hidden)
            zIndex = OrganizeHiddenCards(zIndex, animation);
        if (end)
            zIndex = OrganizeEndCards(zIndex);
        if (active)
            zIndex = OrganizeActiveCards(zIndex, animation);
    }

    private void ZSortAllViews() {
        int position = 0;

        Card cards[] = (Card[]) getHand().GetHiddenCards().toArray();
        for (int i=0; i<cards.length; ++i) {
            Card c = cards[i];
            if (c != null) {
                CardView cv = CardView.getCardView(c);
                cv.setZIndex(position++);
            }
        }
        cards = (Card[]) getHand().GetEndCards().toArray();
        for (int i=0; i<cards.length; ++i) {
            Card c = cards[i];
            if (c != null) {
                CardView cv = CardView.getCardView(c);
                cv.setZIndex(position++);
            }
        }
        int size = getHand().GetActiveCards().size();
        for (int i =0; i<size; ++i) {
            CardView cv = CardView.getCardView(getHand().GetActiveCards().get(i));
            cv.setZIndex(position++);
        }
    }

    private int OrganizeActiveCards(int position, boolean animation) {
        if ( !(getParent() instanceof TableView) )
            throw new RuntimeException("Cannot organize cards in a hand without a TableView parent");
        TableView table = (TableView) getParent();
        int size = getHand().GetActiveCards().size();
        for (int i =0; i<size; ++i) {
            CardView cv = CardView.getCardView(getHand().GetActiveCards().get(i));
            if (animation) {
                AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                builder.setPause(false).setDescription("Lining up active cards").setTable(table).setCard(cv).setHandID(getHand().getID())
                        .killPreviousAnimation(cv)
                        .setTweenCalculator(new CardAnimation.LineUpActiveCard(i)).build().Start();
            } else {
                Vector3 pos = HandUtils.LineUpActiveCard(i, cv, table, mHand.getID(), getActivePosition(), getCardOverlapPercent());
                cv.setPosition(pos.x, pos.y);
                cv.setRotation(pos.z);
                if (mHand.getType() == Hand.HandType.CPU)
                    cv.setSide(CardView.Side.BACK);
            }
        }
        return position;
    }

    private int OrganizeEndCards(int position) {
        if ( !(getParent() instanceof TableView) )
            throw new RuntimeException("Cannot organize cards in a hand without a TableView parent");
        Card cards[] = (Card[]) getHand().GetEndCards().toArray();
        TableView table = (TableView) getParent();
        for (int i=0; i<cards.length; ++i) {
            Card c = cards[i];
            if (c != null) {
                CardView cv = CardView.getCardView(c);
                Vector3 pos = HandUtils.LineUpEndCard(cv, table, getHand().getID(), getHiddenPosition(i), CardUtils.getCardWidth() * getEndCardOverlapPercent());
                cv.setPosition(pos.x, pos.y);
                cv.setRotation(pos.z);
            }
        }
        return position;
    }

    private int OrganizeHiddenCards(int position, boolean animation) {
        if ( !(getParent() instanceof TableView) )
            throw new RuntimeException("Cannot organize cards in a hand without a TableView parent");
        Card cards[] = (Card[]) getHand().GetHiddenCards().toArray();
        TableView table = (TableView) getParent();
        for (int i=0; i<cards.length; ++i) {
            Card c = cards[i];
            if (c != null) {
                CardView cv = CardView.getCardView(c);
                if (animation) {
                    AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CARD);
                    builder.setPause(false).setDescription("Lining up active cards").setTable(table).setCard(cv).setHandID(getHand().getID())
                            .setTweenCalculator(new CardAnimation.LineUpHiddenCards(i)).build().Start();
                }
                else {
                    Vector3 pos = HandUtils.LineUpHiddenCard(cv, table, getHand().getID(), getHiddenPosition(i));
                    cv.setPosition(pos.x, pos.y);
                    cv.setRotation(pos.z);
                    cv.setSide(CardView.Side.BACK);
                }
            }
        }
        return position;
    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        super.drawDebug(shapes);
        shapes.set(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.RED);
        shapes.rect(mActivePosition.getX(), mActivePosition.getY(), mActivePosition.getWidth(), mActivePosition.getHeight());
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
        if (getActivePosition().contains(x,y)) {
            Actor a = super.hit(x,y,touchable);
            return a;
        }
        for (int i=0; i<3; i++) {
            Rectangle r = getHiddenPosition(i);
            if (r.contains(x,y)) {
                Actor a = super.hit(x,y,touchable);
                return a;
            }
        }
        return null;
    }

    public ActorGestureListener getGestureListener() {
        return mGestureListener;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }


    @Override
    public void ReparentAllViews() {
        for (Actor c : getChildren()) {
            c.remove();
        }
        Card cards[] = (Card[]) mHand.GetHiddenCards().toArray();
        for (Card card : cards) {
            if (card != null) {
                CardView cv = CardView.getCardView(card);
                addActor(cv);
            }
        }

        cards = (Card[]) mHand.GetEndCards().toArray();
        for (Card card : cards) {
            if (card != null) {
                CardView cv = CardView.getCardView(card);
                addActor(cv);
            }
        }

        for (Card card : mHand.GetActiveCards()) {
            CardView cv = CardView.getCardView(card);
            addActor(cv);
        }

        OrganizeCards(false);
    }
}
