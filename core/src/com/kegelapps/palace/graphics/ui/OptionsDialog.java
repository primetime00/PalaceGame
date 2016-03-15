package com.kegelapps.palace.graphics.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.FrameView;
import com.kegelapps.palace.protos.OptionProtos;

/**
 * Created by keg45397 on 3/10/2016.
 */
public class OptionsDialog extends FrameView {

    private String mTitle;
    private Table titleTable;
    private Table optionTable;
    private Table actionsTable;
    private Label.LabelStyle style;
    private TextButton.TextButtonStyle buttonStyle;
    private String options[] = {"Resume Game"};

    private Label soundStatus, musicStatus;

    private ChangeListener onChange;

    private enum OptionNames {
        RESUME,
        MUSIC,
        SOUND,
        RESTART,
        QUIT
    };

    private ObjectMap<OptionNames, String> mNameMap;


    public OptionsDialog(String title) {
        style = new Label.LabelStyle();
        style.background = null;
        style.fontColor = Color.WHITE;
        style.font = Director.instance().getAssets().get("default_font", BitmapFont.class);
        titleTable  = new Table();
        optionTable = new Table();
        actionsTable = new Table();
        mTitle = title;

        soundStatus = new Label(Director.instance().getOptions().getSound() ? "On" : "Off", style);
        musicStatus = new Label(Director.instance().getOptions().getMusic() ==false ? "On" : "Off", style);

        setWidth(Director.instance().getScreenWidth() * 0.8f);
        setHeight(Director.instance().getScreenHeight() * 0.8f);

        createActions();

        createNames();

        createTitle();

        createButtons();

        createDialog();
    }

    private void createDialog() {
        add(titleTable).height(Value.percentHeight(0.15f, this)).top();
        row();
        add(optionTable).expand().fill().center();
        row();
        add(actionsTable);
        //add(optionTable).expandX().fillX().prefHeight(Value.percentHeight(0.6f, this)).center();
    }

    private void createActions() {
        onChange = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                OptionProtos.Options opt = Director.instance().getOptions();
                if (actor.getName().equals(mNameMap.get(OptionNames.RESUME))) {
                    Director.instance().getEventSystem().FireLater(EventSystem.EventType.RESUME_GAME);
                }
                else if (actor.getName().equals(mNameMap.get(OptionNames.MUSIC))) {
                    Director.instance().setOptions(opt.toBuilder().setMusic(!opt.getMusic()));
                    mark();
                }
                else if (actor.getName().equals(mNameMap.get(OptionNames.SOUND))) {
                    Director.instance().setOptions(opt.toBuilder().setSound(!opt.getSound()));
                    mark();
                }
                else if (actor.getName().equals(mNameMap.get(OptionNames.RESTART))) {
                    Director.instance().getEventSystem().FireLater(EventSystem.EventType.RESTART_GAME);
                }
                else if (actor.getName().equals(mNameMap.get(OptionNames.QUIT))) {
                }
            }
        };
    }

    private void createTitle() {
        titleTable.add(new Label(mTitle, style)).top().expand();
    }

    private void createNames() {
        mNameMap= new ObjectMap<>();
        mNameMap.put(OptionNames.RESUME, "Resume");
        mNameMap.put(OptionNames.MUSIC, "Music");
        mNameMap.put(OptionNames.SOUND, "Sound FX");
        mNameMap.put(OptionNames.RESTART, "Restart");
        mNameMap.put(OptionNames.QUIT, "Quit");
    }

    private void createButtons() {
        buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.downFontColor = Color.YELLOW;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.font = style.font;

        TextButton btn, snd, mus;



        mus = new TextButton(mNameMap.get(OptionNames.MUSIC), buttonStyle);
        mus.setName(mNameMap.get(OptionNames.MUSIC));
        snd = new TextButton(mNameMap.get(OptionNames.SOUND), buttonStyle);
        snd.setName(mNameMap.get(OptionNames.SOUND));

        Table t = new Table();
        t.add(mus).expandX().left();
        t.add(musicStatus).right();
        t.row();
        t.add(snd).expandX().left();
        t.add(soundStatus).right();
        optionTable.add(t).prefWidth(Math.max(mus.getWidth()+42+musicStatus.getWidth(), snd.getWidth()+42+soundStatus.getWidth()));

        mus.addListener(onChange);

        snd.addListener(onChange);


        btn = new TextButton(mNameMap.get(OptionNames.RESUME), buttonStyle);
        btn.setName(mNameMap.get(OptionNames.RESUME));
        actionsTable.add(btn).width(Value.percentWidth(0.33f, this)).expandY().bottom();
        btn.addListener(onChange);

        btn = new TextButton(mNameMap.get(OptionNames.RESTART), buttonStyle);
        btn.setName(mNameMap.get(OptionNames.RESTART));
        actionsTable.add(btn).width(Value.percentWidth(0.33f, this)).bottom();
        btn.addListener(onChange);

        btn = new TextButton(mNameMap.get(OptionNames.QUIT), buttonStyle);
        btn.setName(mNameMap.get(OptionNames.QUIT));
        actionsTable.add(btn).width(Value.percentWidth(0.33f, this)).bottom().row();
        btn.addListener(onChange);
    }

    @Override
    public void update() {
        soundStatus.setText(Director.instance().getOptions().getSound() ? "On" : "Off");
        musicStatus.setText(Director.instance().getOptions().getMusic()== false ? "On" : "Off");

    }
}
