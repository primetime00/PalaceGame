package com.kegelapps.palace.graphics.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.kegelapps.palace.graphics.*;

/**
 * Created by Ryan on 1/29/2016.
 */
public class HandUtils {

    public static HandSide IDtoSide(int id, TableView table) {
        return table.getSideFromHand(id);
    }

    public static HandUtils.HandSide CameraSideToHand(CardCamera.CameraSide side) {
        switch (side) {
            case BOTTOM: return HandUtils.HandSide.SIDE_BOTTOM;
            case LEFT: return HandUtils.HandSide.SIDE_LEFT;
            case TOP: return HandUtils.HandSide.SIDE_TOP;
            case RIGHT: return HandUtils.HandSide.SIDE_RIGHT;
            default: return HandUtils.HandSide.SIDE_UNKNOWN;
        }
    }

    public static CardCamera.CameraSide HandSideToCamera(HandSide side) {
        switch (side) {
            case SIDE_BOTTOM: return CardCamera.CameraSide.BOTTOM;
            case SIDE_LEFT: return CardCamera.CameraSide.LEFT;
            case SIDE_TOP: return CardCamera.CameraSide.TOP;
            case SIDE_RIGHT: return CardCamera.CameraSide.RIGHT;
            default: return CardCamera.CameraSide.UNKNOWN;
        }
    }


    public enum HandSide {
        SIDE_BOTTOM,
        SIDE_TOP,
        SIDE_LEFT,
        SIDE_RIGHT,
        SIDE_UNKNOWN
    }

    static public Vector3 LineUpActiveCard(int cardNumber, CardView cardView, TableView table, int id, Rectangle rect, float overlap) {
        HandSide side = table.getSideFromHand(id);
        int cardWidth = CardUtils.getCardTextureWidth();

        //find out card1 X position:
        float width = cardWidth*cardNumber*overlap;
        float posX = rect.getX()+(width);
        float posY = rect.getY()+(width);

        float rotDiff = (cardView.getOriginY() - cardView.getOriginX())/1;
        float sideRot = 90.0f;

        Vector3 res = new Vector3();

        switch (side) {
            default:
            case SIDE_BOTTOM: //bottom
                res.set(posX, rect.getY(), 0);
                break;
            case SIDE_LEFT:
                res.set(rect.getX() - (rotDiff * MathUtils.sinDeg(-sideRot)), posY + (rotDiff * MathUtils.sinDeg(-sideRot)), -sideRot);
                break;
            case SIDE_TOP:
                res.set(posX, rect.getY(), 180);
                break;
            case SIDE_RIGHT:
                res.set(rect.getX() + (rotDiff * MathUtils.sinDeg(sideRot)), posY - (rotDiff * MathUtils.sinDeg(sideRot)), sideRot);
                break;
        }
        return res;
    }

    static public Vector3 LineUpHiddenCard(CardView cardView, TableView table, int id, Rectangle rect) {
        HandSide side = table.getSideFromHand(id);
        float rotDiff = (cardView.getOriginY() - cardView.getOriginX())/1;
        float sideRot = 90.0f;

        Vector3 res = new Vector3();

        switch (side) {
            default:
            case SIDE_BOTTOM: //bottom
                res.set(rect.getX(), rect.getY(), 0);
                break;
            case SIDE_LEFT:
                res.set(rect.getX()-(rotDiff*MathUtils.sinDeg(-sideRot)) , rect.getY()+(rotDiff*MathUtils.sinDeg(-sideRot)), -sideRot);
                break;
            case SIDE_TOP:
                res.set(rect.getX(), rect.getY(), 180);
                break;
            case SIDE_RIGHT:
                res.set(rect.getX()+(rotDiff*MathUtils.sinDeg(sideRot)) , rect.getY()-(rotDiff*MathUtils.sinDeg(sideRot)), sideRot);
                break;
        }
        return res;
    }

    static public Vector3 LineUpEndCard(CardView cardView, TableView table, int id, Rectangle rect, float overlap) {
        return LineUpHiddenCard(cardView, table, id, rect).add(overlap, overlap, 0);
    }

    static public Vector2 GetHandPosition(TableView table, HandSide side) {
        if (side == HandSide.SIDE_UNKNOWN) {
            throw new RuntimeException("Hand side is set to unknown!");
        }
        float gap = CardUtils.getCardHeight() * 0.1f;
        Vector2 res = new Vector2();
        DeckView mDeck = table.getDeck();
        HandView h = table.getHandFromSide(side);
        float camX;
        float camY;
        switch (side) {
            default:
            case SIDE_BOTTOM:
                camX = mDeck.getX()+(mDeck.getWidth()/2.0f);
                camY = h.getActivePosition().getY() + table.getCamera().viewportHeight/2.0f - gap;
                break;
            case SIDE_LEFT:
                camX = (h.getActivePosition().getX() + table.getCamera().viewportWidth/2.0f) - gap;
                camY = mDeck.getY()+(mDeck.getHeight()/2.0f);
                break;
            case SIDE_TOP:
                camX = mDeck.getX()+(mDeck.getWidth()/2.0f);
                camY = (h.getActivePosition().getY()+h.getActivePosition().getHeight()) - table.getCamera().viewportHeight/2.0f + gap;
                break;
            case SIDE_RIGHT:
                camX = (h.getActivePosition().getX() + h.getActivePosition().getWidth() - table.getCamera().viewportWidth/2.0f) + gap;
                camY = mDeck.getY()+(mDeck.getHeight()/2.0f);
                break;
        }
        res.set(camX, camY);
        return res;
    }

    public static void Reparent(Group destView, Actor point) {
        Reparent(destView, null, point);
    }

    public static void Reparent(Group destView, Actor src, Actor point) {
        boolean promote = true;
        Group parent = point.getParent();
        if (destView == parent)
            return;
        if (point.getParent() != destView) {
            Vector2 pos = new Vector2();
            if (src == null) {
                if (parent != null) {
                    if (destView.isAscendantOf(parent)) //the current parent is a child of destView
                        promote = false;
                    if (promote)
                        pos = parent.localToDescendantCoordinates(destView, new Vector2(point.getX(), point.getY()));
                    else
                        pos = parent.localToAscendantCoordinates(destView, new Vector2(point.getX(), point.getY()));
                }
                else
                    pos = point.localToAscendantCoordinates(destView, new Vector2(point.getX(), point.getY()));
            } else {
                //is destView a child of the current parent
                if (parent != null) {
                    if (destView.isAscendantOf(parent)) //the current parent is a child of destView
                        promote = false;
                }
                if (promote)
                    pos = destView.localToDescendantCoordinates(src, new Vector2(point.getX(), point.getY()));
                else
                    pos = src.localToAscendantCoordinates(destView, new Vector2(point.getX(), point.getY()));
            }
            point.remove();
            point.setPosition(pos.x, pos.y);
        }
        destView.addActor(point);
    }

}
