package com.kegelapps.palace;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kegelapps.palace.engine.Deck;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.SelectEndCards;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.tasks.TapToStart;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.MessageBandView;
import com.kegelapps.palace.graphics.TableView;

/**
 * Created by keg45397 on 12/15/2015.
 */
public class GameScene extends Scene {

    private Logic logic;
    private Table table;
    private TableView tableView;

    private MessageBandView mMessageBand;

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
        table = new Table(new Deck(), 4, null);
        logic.SetTable(table);
        tableView = new TableView(table);
        mMessageBand = new MessageBandView();
        addActor(tableView);

        createEvents();
    }

    private boolean once = false;

    private void createEvents() {
        EventSystem.Event mTapDeckEvent = new EventSystem.Event(EventSystem.EventType.STATE_CHANGE) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 1 || !(params[0] instanceof State)) {
                    throw new IllegalArgumentException("Invalid parameters for STATE_CHANGE");
                }
                if ((params[0] instanceof SelectEndCards)) {
                    if (once == false) {
                        mMessageBand.showMessage("Select 3 End Cards!", 2.0f, Color.CHARTREUSE);
                        once = true;
                    }
                }
            }
        };
        Director.instance().getEventSystem().RegisterEvent(mTapDeckEvent);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        logic.Poll();
    }

    @Override
    public void draw() {
        super.draw();
        if (mMessageBand.getText().length() > 0) {
            getBatch().begin();
            mMessageBand.draw(getBatch(), 1.0f);
            getBatch().end();
        }
    }

    public Logic getLogic() {
        return logic;
    }
}
