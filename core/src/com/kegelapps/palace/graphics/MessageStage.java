package com.kegelapps.palace.graphics;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by keg45397 on 2/4/2016.
 */
public class MessageStage extends Stage {

    private MessageBandView mMessageBand;

    public MessageStage(Viewport viewport) {
        super(viewport);
        mMessageBand = new MessageBandView();
        addActor(mMessageBand);
    }

    public MessageBandView getMessageBand() {
        return mMessageBand;
    }

    @Override
    public void draw() {
        if (mMessageBand.getText().isEmpty())
            return;
        super.draw();
    }
}
