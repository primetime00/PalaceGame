package com.kegelapps.palace;

import com.badlogic.gdx.utils.viewport.Viewport;
import com.kegelapps.palace.engine.Deck;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.Table;
import com.kegelapps.palace.graphics.TableView;

/**
 * Created by keg45397 on 12/15/2015.
 */
public class GameScene extends Scene {

    private Logic logic;
    private Table table;
    private TableView tableView;


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
        addActor(tableView);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        logic.Poll();
    }

    public Logic getLogic() {
        return logic;
    }
}
