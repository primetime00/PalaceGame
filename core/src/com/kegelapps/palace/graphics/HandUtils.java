package com.kegelapps.palace.graphics;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Ryan on 1/29/2016.
 */
public class HandUtils {

    public static HandSide IDtoSide(int id) {
        switch (id) {
            default:
            case 0 : return HandSide.SIDE_BOTTOM;
            case 1: return HandSide.SIDE_LEFT;
            case 2: return HandSide.SIDE_TOP;
            case 3: return HandSide.SIDE_RIGHT;
        }
    }

    public enum HandSide {
        SIDE_BOTTOM,
        SIDE_TOP,
        SIDE_LEFT,
        SIDE_RIGHT
    }

    static public Vector3 LineUpActiveCard(int cardNumber, CardView cardView, HandSide side, Rectangle rect, float overlap) {
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

    static public Vector3 LineUpHiddenCard(CardView cardView, HandSide side, Rectangle rect) {
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

    static public Vector3 LineUpEndCard(CardView cardView, HandSide side, Rectangle rect, float overlap) {
        return LineUpHiddenCard(cardView, side, rect).add(overlap, overlap, 0);
    }


}
