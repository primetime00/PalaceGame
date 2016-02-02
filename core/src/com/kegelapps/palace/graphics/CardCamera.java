package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Ryan on 1/29/2016.
 */
public class CardCamera extends OrthographicCamera {

    public enum CameraSide {
        BOTTOM,
        LEFT,
        TOP,
        RIGHT,
        CENTER,
        UNKNOWN
    }

    private CameraSide mCurrentSide;

    public CardCamera() {
        super();
        init();
    }

    public CardCamera(float viewportWidth, float viewportHeight) {
        super(viewportWidth, viewportHeight);
        init();
    }

    private void init() {
        mCurrentSide = CameraSide.CENTER;
    }

    public void SetSide(CameraSide side) {
        mCurrentSide = side;
    }

    public void SetPosition(Vector2 pos, float zoom, CameraSide side) {
        position.set(pos.x, pos.y, 0);
        SetSide(side);
    }


}
