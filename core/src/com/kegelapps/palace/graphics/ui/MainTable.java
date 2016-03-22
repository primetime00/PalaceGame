package com.kegelapps.palace.graphics.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.audio.SoundEvent;
import com.kegelapps.palace.loaders.types.SoundMap;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.ui.common.StringMap;

/**
 * Created by Ryan on 3/14/2016.
 */
public class MainTable extends Table {

    private ChangeListener onChange;

    private TextButton.TextButtonStyle buttonStyle;

    public MainTable() {

        onChange = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Director.instance().getAudioManager().QueueSound(new SoundEvent(Director.instance().getAssets().get("sounds", SoundMap.class).getRandom("button"), 0.0f));
                if (actor.getName().equals(StringMap.getString("new_game")))
                    Director.instance().getEventSystem().FireLater(EventSystem.EventType.MAIN_NEW_GAME);
                else if (actor.getName().equals(StringMap.getString("options")))
                    Director.instance().getEventSystem().FireLater(EventSystem.EventType.MAIN_OPTIONS);
                else if (actor.getName().equals(StringMap.getString("acknowledgements")))
                    Director.instance().getEventSystem().FireLater(EventSystem.EventType.SHOW_ACKNOWLEDGEMENTS);

            }
        };

        createButtons();
    }

    private void createButtons() {
        buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.downFontColor = Color.YELLOW;
        buttonStyle.fontColor = Color.RED;
        buttonStyle.font = Director.instance().getAssets().get("title_font_small", BitmapFont.class);

        float maxWidth = 0.0f;

        TextButton btn;
        btn = new TextButton(StringMap.getString("new_game"), buttonStyle); maxWidth = Math.max(maxWidth, btn.getWidth());
        btn.setName(StringMap.getString("new_game"));
        btn.addListener(onChange);
        add(btn).width(maxWidth).row();
        btn = new TextButton(StringMap.getString("options"), buttonStyle); maxWidth = Math.max(maxWidth, btn.getWidth());
        btn.setName(StringMap.getString("options"));
        btn.addListener(onChange);
        add(btn).width(maxWidth).row();
        btn = new TextButton(StringMap.getString("acknowledgements"), buttonStyle); maxWidth = Math.max(maxWidth, btn.getWidth());
        btn.setName(StringMap.getString("acknowledgements"));
        btn.addListener(onChange);
        add(btn).width(maxWidth).row();

    }
}
