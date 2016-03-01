package com.kegelapps.palace;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.states.SelectEndCards;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.MessageStage;
import com.kegelapps.palace.graphics.TableView;

/**
 * Created by keg45397 on 12/15/2015.
 */
public class GameScene extends Scene {

    private Logic logic;
    private TableView tableView;

    private MessageStage mMessageStage;

    public GameScene() {
        super();
        init();
    }

    public GameScene(Viewport viewport) {
        super(viewport);
        init();
    }

    private void init() {
        logic = Logic.get();
        tableView = new TableView(logic.GetTable(), getCardCamera());
        mMessageStage = new MessageStage(new ScreenViewport());
        addActor(tableView);

        createEvents();
    }

    private boolean once = false;
    private boolean state = false;

    private void createEvents() {
        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.STATE_CHANGE) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 1 || !(params[0] instanceof State)) {
                    throw new IllegalArgumentException("Invalid parameters for STATE_CHANGE");
                }
                if ((params[0] instanceof SelectEndCards)) {
                    if (once == false) {
                        mMessageStage.getMessageBand().showMessage("Select 3 End Cards!", 2.0f, Color.CHARTREUSE, false);
                        once = true;
                        state = true;
                    }
                }
            }

        });
        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.SHOW_MESSAGE) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 3 || !(params[0] instanceof String) || !(params[1] instanceof Float) || !(params[2] instanceof Color)) {
                    throw new IllegalArgumentException("Invalid parameters for SHOW_MESSAGE");
                }
                String message = (String) params[0];
                float duration = (float) params[1];
                Color color = (Color) params[2];
                mMessageStage.getMessageBand().showMessage(message, duration, color, false);
            }

        });

    }

    public void ShowMessage(String message, float duration, Color color) {
        ShowMessage(message, duration, color, false);
    }

    public void ShowMessage(String message, float duration, Color color, boolean pause) {
        mMessageStage.getMessageBand().showMessage(message, duration, color, pause);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        logic.Poll();
        if (state) {
            //logic.SaveState();
            state = false;
        }
    }

    @Override
    public void draw() {
        getCardCamera().update();
        super.draw();
        //lets draw the messageband hud
        mMessageStage.draw();
    }

    public Logic getLogic() {
        return logic;
    }
}
