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
import com.kegelapps.palace.graphics.FrameView;

/**
 * Created by keg45397 on 3/10/2016.
 */
public class OptionsDialog extends FrameView {

    private String mTitle;
    private Table titleTable;
    private Table optionTable;
    private Label.LabelStyle style;
    private TextButton.TextButtonStyle buttonStyle;
    private String options[] = {"Resume Game"};

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
        style.font = Director.instance().getAssets().get("FatCow.ttf", BitmapFont.class);
        titleTable  = new Table();
        optionTable = new Table();
        mTitle = title;

        createActions();

        createNames();

        createTitle();

        createButtons();

        createDialog();
    }

    private void createDialog() {
        add(titleTable).prefHeight(Value.percentHeight(0.2f, this));
        row();
        add(optionTable).expandX().fillX().prefHeight(Value.percentHeight(0.6f, this)).center();
    }

    private void createActions() {
        onChange = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (actor.getName().equals(mNameMap.get(OptionNames.RESUME))) {
                }
                else if (actor.getName().equals(mNameMap.get(OptionNames.MUSIC))) {
                }
                else if (actor.getName().equals(mNameMap.get(OptionNames.SOUND))) {
                }
                else if (actor.getName().equals(mNameMap.get(OptionNames.RESTART))) {
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
        mNameMap.put(OptionNames.RESUME, "Resume Game");
        mNameMap.put(OptionNames.MUSIC, "Music");
        mNameMap.put(OptionNames.SOUND, "Sound FX");
        mNameMap.put(OptionNames.RESTART, "Restart Game");
        mNameMap.put(OptionNames.QUIT, "Quit Game");
    }

    private void createButtons() {
        buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.downFontColor = Color.YELLOW;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.font = style.font;

        for (OptionNames name : OptionNames.values()) {
            TextButton btn = new TextButton(mNameMap.get(name), buttonStyle);
            btn.setName(mNameMap.get(name));
            optionTable.add(btn).expandX().row();
            btn.addListener(onChange);
        }
    }
}
