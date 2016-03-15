package com.kegelapps.palace.graphics.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.ui.common.StringMap;
import com.kegelapps.palace.protos.OptionProtos;

/**
 * Created by keg45397 on 3/15/2016.
 */
public class OptionsTable extends Table {

    private ChangeListener onChange;

    private TextButton.TextButtonStyle buttonStyle;
    private Label.LabelStyle labelStyle;
    private Label mMusicLabel, mSoundLabel;

    public OptionsTable() {

        onChange = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                OptionProtos.Options opt = Director.instance().getOptions();
                if (actor.getName().equals(StringMap.getString("music"))) {
                    Director.instance().setOptions(opt.toBuilder().setMusic(!opt.getMusic()));
                    invalidate();
                }
                else if (actor.getName().equals(StringMap.getString("sound"))) {
                    Director.instance().setOptions(opt.toBuilder().setSound(!opt.getSound()));
                    invalidate();
                }
                else if (actor.getName().equals(StringMap.getString("back"))) {
                    Director.instance().getEventSystem().FireLater(EventSystem.EventType.OPTIONS_BACK);
                }
            }
        };

        createLabels();
        createButtons();
    }

    private void createLabels() {
        labelStyle = new Label.LabelStyle();
        labelStyle.background = null;
        labelStyle.fontColor = Color.RED;
        labelStyle.font = Director.instance().getAssets().get("title_font_small", BitmapFont.class);

        mMusicLabel = new Label(Director.instance().getOptions().getMusic() ?
                StringMap.getString("on") :
                StringMap.getString("off"),
                labelStyle);
        mSoundLabel = new Label(Director.instance().getOptions().getSound() ?
                StringMap.getString("on") :
                StringMap.getString("off"),
                labelStyle);
    }

    private void createButtons() {
        buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.downFontColor = Color.YELLOW;
        buttonStyle.fontColor = Color.RED;
        buttonStyle.font = Director.instance().getAssets().get("title_font_small", BitmapFont.class);

        float maxWidth = 0.0f;

        Table t = new Table();
        TextButton btn;
        btn = new TextButton(StringMap.getString("music"), buttonStyle); maxWidth = Math.max(maxWidth, btn.getWidth());
        btn.setName(StringMap.getString("music"));
        btn.addListener(onChange);
        t.add(btn).expandX().left();
        t.add(mMusicLabel).right();
        t.row();

        btn = new TextButton(StringMap.getString("sound"), buttonStyle); maxWidth = Math.max(maxWidth, btn.getWidth());
        btn.setName(StringMap.getString("sound"));
        btn.addListener(onChange);
        t.add(btn).expandX().left();
        t.add(mSoundLabel).right();

        add(t).expandX().fillX();
        row();

        btn = new TextButton(StringMap.getString("back"), buttonStyle); maxWidth = Math.max(maxWidth, btn.getWidth());
        btn.setName(StringMap.getString("back"));
        btn.addListener(onChange);
        add(btn).width(maxWidth).row();

    }

    @Override
    public void invalidate() {
        super.invalidate();
        mMusicLabel.setText(Director.instance().getOptions().getMusic() ?
                StringMap.getString("on") :
                StringMap.getString("off"));
        mSoundLabel.setText(Director.instance().getOptions().getSound() ?
                StringMap.getString("on") :
                StringMap.getString("off"));
    }
}
