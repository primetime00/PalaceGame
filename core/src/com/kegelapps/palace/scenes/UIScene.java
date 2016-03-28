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
    }

    public UIScene(Viewport viewport) {
        super(viewport);
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

    @Override
    protected void initFirstRun() {
        init();
    }

    private void createEvents() {
        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.GAME_OVER) {
            @Override
            public void handle(Object params[]) {
                clearScene();
                //float x = (getViewWidth() - mGameStatsDialog.getWidth()) / 2.0f;
                //float y = (getViewHeight() - mGameStatsDialog.getHeight()) / 2.0f;
                //mGameStatsDialog.setPosition(x,y);
                addActor(mGameStatsDialog);
                mGameStatsDialog.mark();
            }
        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.OPTIONS) {
            @Override
            public void handle(Object params[]) {
                clearScene();
                //float x = (getViewWidth() - mOptionsDialog.getWidth()) / 2.0f;
                //float y = (getViewHeight() - mOptionsDialog.getHeight()) / 2.0f;
                //mOptionsDialog.setPosition(x,y);
                addActor(mOptionsDialog);
                mOptionsDialog.setScreenPercent(0.8f, 0.8f);
                mOptionsDialog.mark();
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
