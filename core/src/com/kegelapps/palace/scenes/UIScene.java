package com.kegelapps.palace.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.audio.AudioManager;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.states.SelectEndCards;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.MessageStage;
import com.kegelapps.palace.graphics.TableView;
import com.kegelapps.palace.graphics.ui.GameStatsDialog;
import com.kegelapps.palace.graphics.ui.OptionsDialog;
import com.kegelapps.palace.graphics.ui.common.StringMap;
import com.kegelapps.palace.protos.OptionProtos;

/**
 * Created by keg45397 on 3/3/2016.
 */
public class UIScene extends Scene {

    private GameStatsDialog mGameStatsDialog;
    private OptionsDialog mOptionsDialog;

    public UIScene() {
        super();
        init();
    }

    public UIScene(Viewport viewport) {
        super(viewport);
        init();
    }

    private void init() {
        mGameStatsDialog = new GameStatsDialog(StringMap.getString("game_over"));
        mOptionsDialog = new OptionsDialog(StringMap.getString("options"));
        createEvents();
    }

    @Override
    public void exit() {
        super.exit();
        clearScene();
    }

    private void createEvents() {
        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.GAME_OVER) {
            @Override
            public void handle(Object params[]) {
                clearScene();
                float x = (getViewport().getScreenWidth() - mGameStatsDialog.getWidth()) / 2.0f;
                float y = (getViewport().getScreenHeight() - mGameStatsDialog.getHeight()) / 2.0f;
                mGameStatsDialog.setPosition(x,y);
                addActor(mGameStatsDialog);
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.OPTIONS) {
            @Override
            public void handle(Object params[]) {
                clearScene();
                float x = (getViewport().getScreenWidth() - mOptionsDialog.getWidth()) / 2.0f;
                float y = (getViewport().getScreenHeight() - mOptionsDialog.getHeight()) / 2.0f;
                mOptionsDialog.setPosition(x,y);
                addActor(mOptionsDialog);
                Director.instance().getAudioManager().SendEvent(AudioManager.AudioEvent.TRANSITION_TO_OPTIONS, 1.0f);
            }
        });

    }

    @Override
    public void OptionChanged(OptionProtos.Options option) {
        if (!option.getMusic()) {
            Director.instance().getAudioManager().StopMusic();
        }
    }
}
