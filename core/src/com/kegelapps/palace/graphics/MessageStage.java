package com.kegelapps.palace.graphics;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by keg45397 on 2/4/2016.
 */
public class MessageStage extends Stage {

    private MessageBandView mMessageBand;
    private ChatBoxView mChatBox;

    public MessageStage(Viewport viewport) {
        super(viewport);
        mMessageBand = new MessageBandView();
        mChatBox = new ChatBoxView(15);
        addActor(mMessageBand);
        addActor(mChatBox);

    }

    public MessageBandView getMessageBand() {
        return mMessageBand;
    }
    public ChatBoxView getChatBox() { return  mChatBox; }

    @Override
    public void draw() {
        setVisibility();
        super.draw();
    }

    private void setVisibility() {
        mChatBox.setVisible(!mChatBox.getText().isEmpty());
        mMessageBand.setVisible(!mMessageBand.getText().isEmpty());

    }
}
