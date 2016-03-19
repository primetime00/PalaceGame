package com.kegelapps.palace.graphics.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.kegelapps.palace.CoinResource;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.events.EventSystem;
import com.kegelapps.palace.graphics.FrameView;
import com.kegelapps.palace.graphics.ui.common.StringMap;
import com.kegelapps.palace.graphics.utils.HandUtils;
import com.kegelapps.palace.protos.LogicProtos;

/**
 * Created by keg45397 on 3/2/2016.
 */
public class GameStatsDialog extends FrameView {

    private String mTitle;
    private Table titleTable;
    private Table statsTable;
    private TextButton.TextButtonStyle btnStyle;
    private Label.LabelStyle style;

    private ChangeListener mQuitListener, mRematchListener;


    public GameStatsDialog(String title) {
        style = new Label.LabelStyle();
        style.background = null;
        style.fontColor = Color.WHITE;
        style.font = Director.instance().getAssets().get("default_font", BitmapFont.class);
        titleTable  = new Table();
        statsTable = new Table();
        mTitle = title;

        NinePatch btnUp = new NinePatch (((TextureAtlas) Director.instance().getAssets().get("ui.pack")).findRegion("button"), 8, 8, 8, 8);
        NinePatch btnDn = new NinePatch (((TextureAtlas) Director.instance().getAssets().get("ui.pack")).findRegion("button_down"), 8, 8, 8, 8);


        btnStyle = new TextButton.TextButtonStyle(new NinePatchDrawable(btnUp),
                new NinePatchDrawable(btnDn), null, style.font);

        mRematchListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Director.instance().getEventSystem().FireLater(EventSystem.EventType.QUIT_GAME, true);
            }
        };

        mQuitListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Director.instance().getEventSystem().FireLater(EventSystem.EventType.QUIT_GAME, false);
            }
        };
        setWidth(Director.instance().getScreenWidth()*0.8f);
    }

    @Override
    public void update() {
        super.update();
        //reset();
        titleTable.reset();
        statsTable.reset();
        //lets get out stats

        LogicProtos.Placement places[] = {
                Logic.get().getStats().GetStats(CoinResource.CoinType.GOLD),
                Logic.get().getStats().GetStats(CoinResource.CoinType.SILVER),
                Logic.get().getStats().GetStats(CoinResource.CoinType.BRONZE)
        };

        String placeStrings[] = { StringMap.getString("1st"), StringMap.getString("2nd"), StringMap.getString("3rd")};

        titleTable.add(new Label(mTitle, style)).top().expand();
        add(titleTable).prefHeight(Value.percentHeight(0.2f, this));
        row();

        for (int i=0; i<placeStrings.length; ++i) {
            LogicProtos.Placement place = places[i];
            String name = StringMap.getString("you");
            if (place.getHandID() >= 0) {
                statsTable.add(new Label(placeStrings[i], style)).maxWidth(Value.percentWidth(0.2f, this))
                        .expandX().left();
                if (Logic.get().GetTable().GetHand(place.getHandID()).getIdentity() != null)
                    name = Logic.get().GetTable().GetHand(place.getHandID()).getIdentity().get().getName();
                statsTable.add(new Label(name, style))
                        .maxWidth(Value.percentWidth(0.4f, this)).expandX();
                statsTable.add(new Label(String.format("%d %s", place.getRounds(), StringMap.getString("turns")), style))
                        .maxWidth(Value.percentWidth(0.4f, this)).expandX().right();
                statsTable.row();
            }
        }
        add(statsTable).expandX().fillX().prefHeight(Value.percentHeight(0.6f, this)).center();
        row();
        Table btnTable = new Table();
        TextButton tbRematch = new TextButton(StringMap.getString("rematch"), btnStyle);
        tbRematch.getLabel().setFontScale(0.75f);
        tbRematch.addListener(mRematchListener);
        TextButton tbQuit = new TextButton(StringMap.getString("quit"), btnStyle);
        tbQuit.getLabel().setFontScale(0.75f);
        tbQuit.addListener(mQuitListener);
        btnTable.add(tbRematch).prefWidth(100).height(36);
        btnTable.add().expandX();
        btnTable.add(tbQuit).prefWidth(100).height(36);
        add(btnTable).expandX().fillX().pad(Value.zero, Value.percentWidth(0.1f, this), Value.zero, Value.percentWidth(0.1f, this)).height(32);
    }

    public void setTitle(String title) {
        if (title.equals(mTitle))
            return;
        mTitle = title;
        mark();
    }

}
