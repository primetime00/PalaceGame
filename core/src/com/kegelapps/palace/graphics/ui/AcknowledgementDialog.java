package com.kegelapps.palace.graphics.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.FrameView;
import com.kegelapps.palace.graphics.ui.common.StringMap;
import com.kegelapps.palace.protos.OptionProtos;

/**
 * Created by keg45397 on 3/15/2016.
 */
public class AcknowledgementDialog extends FrameView {
    private String mTitle;
    private Table titleTable;
    private Table textTable;
    private Table actionsTable;
    private Label.LabelStyle style;
    private TextButton.TextButtonStyle buttonStyle;

    private ChangeListener onChange;

    public AcknowledgementDialog(String title) {
        style = new Label.LabelStyle();
        style.background = null;
        style.fontColor = Color.WHITE;
        style.font = Director.instance().getAssets().get("default_font", BitmapFont.class);
        titleTable  = new Table();
        textTable = new Table();
        actionsTable = new Table();
        mTitle = title;

        setWidth(Director.instance().getScreenWidth() * 0.8f);
        setHeight(Director.instance().getScreenHeight() * 0.8f);

        createActions();

        createTitle();

        createButtons();

        createText();

        createDialog();
    }

    private void createText() {
        Label l = new Label("", style);
        l.setText(StringMap.getString("all_acknowledgements"));
        String s = l.getText().toString();
        l.setWrap(true);
        l.setAlignment(Align.center);
        textTable.add(l).width(getWidth()-getPadLeft()-getPadRight());

    }

    private void createDialog() {
        add(titleTable).height(Value.percentHeight(0.15f, this)).top();
        row();
        add(textTable).expand().fill().center();
        row();
        add(actionsTable);
    }

    private void createActions() {
        onChange = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (actor.getName().equals(StringMap.getString("back"))) {
                    Director.instance().getEventSystem().FireLater(EventSystem.EventType.DISMISS_ACKNOWLEDGEMENTS);
                }
            }
        };
    }

    private void createTitle() {
        titleTable.add(new Label(mTitle, style)).top().expand();
    }

    private void createButtons() {
        buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.downFontColor = Color.YELLOW;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.font = style.font;

        TextButton btn;

        btn = new TextButton(StringMap.getString("back"), buttonStyle);
        btn.setName(StringMap.getString("back"));
        actionsTable.add(btn).width(Value.percentWidth(1, this)).expandY().bottom();
        btn.addListener(onChange);
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
        Rectangle r = new Rectangle(getX(), getY(), getWidth(), getHeight());
        Actor a = super.hit(x, y, touchable);
        if (a == null && r.contains(x, y))
            a = this;
        return a;
    }
}
