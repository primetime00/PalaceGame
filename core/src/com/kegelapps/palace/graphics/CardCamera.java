package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.OrthographicCamera;

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


}
