package com.kegelapps.palace;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
import com.kegelapps.palace.engine.Deck;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.engine.states.Play;
import com.kegelapps.palace.engine.states.SelectEndCards;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.engine.states.tasks.DealCard;
import com.kegelapps.palace.engine.states.tasks.TapToStart;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.MessageBandView;
import com.kegelapps.palace.graphics.TableView;
import com.kegelapps.palace.protos.StateProtos;
import com.kegelapps.palace.protos.StatusProtos;
import com.kegelapps.palace.protos.TableProtos;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
        //if (!checkForSave()) {
            table = new Table(new Deck(), 4);
        //}
        tableView = new TableView(table);
        logic.SetTable(table);
        mMessageBand = new MessageBandView();
        addActor(tableView);

        createEvents();
    }

    private boolean checkForSave() {
        StatusProtos.Table tv;
        try {
            FileInputStream fs = new FileInputStream("test.dat");
            CodedInputStream istream = CodedInputStream.newInstance(fs);
            tv = StatusProtos.Table.parseFrom(istream);
        } catch (Exception e) {
            return false;
        }
        table = new Table(tv);
        return true;
    }

    private boolean once = false;
    private boolean state = false;

    private void createEvents() {
        EventSystem.Event mTapDeckEvent = new EventSystem.Event(EventSystem.EventType.STATE_CHANGE) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 1 || !(params[0] instanceof State)) {
                    throw new IllegalArgumentException("Invalid parameters for STATE_CHANGE");
                }
                if ((params[0] instanceof Play)) {
                    Logic.get().getMainState().WriteBuffer();
                }
                if ((params[0] instanceof SelectEndCards)) {
                    if (once == false) {
                        mMessageBand.showMessage("Select 3 End Cards!", 2.0f, Color.CHARTREUSE);
                        once = true;
                        state = true;
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
        if (state) {
            if (table != null) {
                StatusProtos.Status.Builder statBuilder = StatusProtos.Status.newBuilder();
                statBuilder.setTable((StatusProtos.Table) table.WriteBuffer());
                statBuilder.setMainState((StateProtos.State) logic.getMainState().WriteBuffer());
                StatusProtos.Status st = statBuilder.build();
                try {
                    FileOutputStream bs = new FileOutputStream("test.dat");
                    CodedOutputStream output = CodedOutputStream.newInstance(bs);
                    st.writeTo(output);
                    output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            state = false;
        }
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
