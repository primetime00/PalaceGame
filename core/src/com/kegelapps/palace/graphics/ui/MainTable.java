package com.kegelapps.palace.graphics.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.kegelapps.palace.Director;

/**
 * Created by Ryan on 3/14/2016.
 */
public class MainTable extends Table {

    private enum OptionNames {
        NEW,
        OPTIONS,
        ACKNOWLEDGEMENTS,
    };

    private ChangeListener onChange;

    private ObjectMap<OptionNames, String> mNameMap;
    private TextButton.TextButtonStyle buttonStyle;

    public MainTable() {

        onChange = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            }
        };

        createNames();
        createButtons();
    }

    private void createButtons() {
        buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.downFontColor = Color.YELLOW;
        buttonStyle.fontColor = Color.RED;
        buttonStyle.font = Director.instance().getAssets().get("title_font_small", BitmapFont.class);

        float maxWidth = 0.0f;

        TextButton btn;
        btn = new TextButton(mNameMap.get(OptionNames.NEW), buttonStyle); maxWidth = Math.max(maxWidth, btn.getWidth());
        btn.addListener(onChange);
        add(btn).width(maxWidth).row();
        btn = new TextButton(mNameMap.get(OptionNames.OPTIONS), buttonStyle); maxWidth = Math.max(maxWidth, btn.getWidth());
        btn.addListener(onChange);
        add(btn).width(maxWidth).row();
        btn = new TextButton(mNameMap.get(OptionNames.ACKNOWLEDGEMENTS), buttonStyle); maxWidth = Math.max(maxWidth, btn.getWidth());
        btn.addListener(onChange);
        add(btn).width(maxWidth).row();

        setWidth(maxWidth);

    }

    private void createNames() {
        mNameMap= new ObjectMap<>();
        mNameMap.put(OptionNames.NEW, "New Game");
        mNameMap.put(OptionNames.OPTIONS, "Options");
        mNameMap.put(OptionNames.ACKNOWLEDGEMENTS, "Acknowledgements");

    }
}
