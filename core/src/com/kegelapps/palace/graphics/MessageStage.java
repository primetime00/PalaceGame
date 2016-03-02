package com.kegelapps.palace.graphics;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kegelapps.palace.graphics.ui.GameStatsDialog;

/**
 * Created by keg45397 on 2/4/2016.
 */
public class MessageStage extends Stage {

    private MessageBandView mMessageBand;
    private GameStatsDialog mDialog;

    public MessageStage(Viewport viewport) {
        super(viewport);
        mMessageBand = new MessageBandView();
        mDialog = new GameStatsDialog();
        addActor(mMessageBand);
/*        addActor(mDialog);
        float x = (getViewport().getScreenWidth() - mDialog.getWidth())/2.0f;
        float y = (getViewport().getScreenHeight() - mDialog.getHeight())/2.0f;
        mDialog.setPosition(x, y);*/

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
