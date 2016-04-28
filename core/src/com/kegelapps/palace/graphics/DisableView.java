package com.kegelapps.palace.graphics;

import com.badlogic.gdx.graphics.Color;

/**
 * Created by keg45397 on 4/27/2016.
 */
public class DisableView extends HighlightView {

    public DisableView() {
        super();
        mColor = Color.GRAY;
        setAlpha(mMaxAlpha);
        mMinAlpha = 0.6f;
        mDuration = 1.0f;
    }


}
