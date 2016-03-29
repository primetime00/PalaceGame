package com.kegelapps.palace.scenes;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.animations.AnimationFactory;
import com.kegelapps.palace.audio.AudioManager;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.states.SelectEndCards;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.MessageStage;
import com.kegelapps.palace.graphics.TableView;
import com.kegelapps.palace.graphics.ui.common.StringMap;
import com.kegelapps.palace.protos.OptionProtos;
import com.kegelapps.palace.tween.ActorAccessor;

/**
 * Created by keg45397 on 12/15/2015.
 */
public class GameScene extends Scene {

    private Logic logic;
    private TableView tableView;
    private boolean runLogic;

    private MessageStage mMessageStage;

    public GameScene() {
        super();
    }

    public GameScene(Viewport viewport) {
        super(viewport);
    }

    private void init() {
        logic = Logic.get();
        logic.SetNumberOfPlayers(4);
        logic.Initialize();
        tableView = new TableView(logic.GetTable(), getCardCamera());
        //mMessageStage = new MessageStage(new ScreenViewport());
        mMessageStage = new MessageStage(new ExtendViewport(getViewWidth(), getViewHeight()));
        addActor(tableView);

        getInputMultiplexer().addProcessor(mMessageStage);
        createEvents();
        Director.instance().addResetter(this, 0);

        addListener(new InputListener() {
            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {//ESC pressed
                    Director.instance().getEventSystem().FireLater(EventSystem.EventType.OPTIONS);
                }
                return super.keyUp(event, keycode);
            }
        });
        runLogic = false;
    }

    private void createEvents() {
        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.STATE_CHANGE) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 1 || !(params[0] instanceof State)) {
                    throw new IllegalArgumentException("Invalid parameters for STATE_CHANGE");
                }
                if ((params[0] instanceof SelectEndCards)) {
                    ShowMessage(StringMap.getString("select_end_cards"), 2.0f, Color.CHARTREUSE, false);
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

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.STATE_LOADED) {
            @Override
            public void handle(Object params[]) {
                Director.instance().getAudioManager().SendEvent(AudioManager.AudioEvent.GAME_LOADED);
            }

        });

        Director.instance().getEventSystem().RegisterEvent(new EventSystem.EventListener(EventSystem.EventType.QUIT_GAME) {
            @Override
            public void handle(Object params[]) {
                if (params == null || params.length != 1 || !(params[0] instanceof Boolean)) {
                    throw new IllegalArgumentException("Invalid parameters for QUIT_GAME");
                }
                final float duration = 0.75f;
                final boolean restart = (boolean) params[0];
                runLogic = false;
                if (!restart)
                    Director.instance().getAudioManager().SendEvent(AudioManager.AudioEvent.TRANSITION_TO_MAIN, duration);
                else
                    Director.instance().getAudioManager().SendEvent(AudioManager.AudioEvent.STOP_GAME, duration);
                Timeline ani = Timeline.createParallel();
                ani.push(Tween.to(getRoot(), ActorAccessor.ALPHA, duration).target(0));
                ani.push(Tween.to(mMessageStage.getRoot(), ActorAccessor.ALPHA, duration).target(0));
                ani.setCallbackTriggers(TweenCallback.END);
                ani.setCallback(new TweenCallback() {
                    @Override
                    public void onEvent(int type, BaseTween<?> source) {
                        if (type == END) {
                            Director.instance().getAudioManager().FadeOutSound(new Runnable() {
                                @Override
                                public void run() {
                                    Director.instance().getAudioManager().Reset();
                                    Director.instance().getAudioManager().SetMasterVolume(1.0f);
                                }
                            });
                            if (!restart) {
                                Director.instance().getEventSystem().FireLater(EventSystem.EventType.MAIN_SCREEN);
                            }
                            else {
                                Director.instance().getEventSystem().FireLater(EventSystem.EventType.RESTART_GAME, false);
                            }
                        }
                    }
                });
                ani.start(getTweenManager());
            }

        });


    }

    public void ShowMessage(String message, float duration, Color color) {
        ShowMessage(message, duration, color, false);
    }

    public void ShowMessage(String message, float duration, Color color, boolean pause) {
        if (Logic.get().isSimulate())
            return;
        mMessageStage.getMessageBand().showMessage(message, duration, color, pause);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (runLogic)
            logic.Poll();
    }

    @Override
    protected void initFirstRun() {
        if (tableView == null)
            init();
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

    @Override
    public void dispose() {
        super.dispose();
        if (tableView != null) {
            tableView.dispose();
        }
        mMessageStage.dispose();
        mMessageStage = null;
    }

    @Override
    public void Reset(boolean newGame) {
        super.Reset(newGame);
        getTweenManager().killAll();
        logic.Reset(newGame);
        addActor(tableView);
        mMessageStage.getMessageBand().setText("");
        getRoot().setColor(1,1,1,0);
        mMessageStage.getRoot().setColor(1,1,1,1);
    }

    @Override
    public void enter() {
        super.enter();
        if (runLogic == false) {
            getRoot().setColor(1, 1, 1, 0);
            mMessageStage.getRoot().setColor(1,1,1,1);
            Tween ani = Tween.to(getRoot(), ActorAccessor.ALPHA, 0.75f).target(1);
            ani.setCallbackTriggers(TweenCallback.END).setCallback(new TweenCallback() {
                @Override
                public void onEvent(int type, BaseTween<?> source) {
                    if (type == END)
                        runLogic = true;
                }
            });
            ani.start(getTweenManager());
        }
        CheckForQuickGame();
    }

    private void CheckForQuickGame() {
        tableView.CheckForQuickGame();
    }

    @Override
    public void OptionChanged(OptionProtos.Options option) {
        if (!option.getMusic()) {
            Director.instance().getAudioManager().StopMusic();
        }
        else {
            Director.instance().getAudioManager().PlayMusic();
        }

    }
}
