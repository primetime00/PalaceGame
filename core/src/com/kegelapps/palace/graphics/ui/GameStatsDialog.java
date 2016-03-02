package com.kegelapps.palace.graphics.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.kegelapps.palace.CoinResource;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.graphics.FrameView;
import com.kegelapps.palace.protos.LogicProtos;
import sun.rmi.runtime.Log;

/**
 * Created by keg45397 on 3/2/2016.
 */
public class GameStatsDialog extends FrameView {

    Label mLabel;

    public GameStatsDialog() {
        Label.LabelStyle style = new Label.LabelStyle();
        style.background = null;
        style.fontColor = Color.WHITE;
        style.font = Director.instance().getAssets().get("FatCow.ttf", BitmapFont.class);
        Table title  = new Table();
        Table stats = new Table();

        NinePatch btnUp = new NinePatch (((TextureAtlas) Director.instance().getAssets().get("ui.pack")).findRegion("button"), 8, 8, 8, 8);
        NinePatch btnDn = new NinePatch (((TextureAtlas) Director.instance().getAssets().get("ui.pack")).findRegion("button_down"), 8, 8, 8, 8);


        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle(new NinePatchDrawable(btnUp),
                new NinePatchDrawable(btnDn), null, style.font);

        mLabel = new Label("Game Over!", style);

        //lets get out stats
        LogicProtos.Placement p1 = Logic.get().getStats().GetStats(CoinResource.CoinType.GOLD);
        LogicProtos.Placement p2 = Logic.get().getStats().GetStats(CoinResource.CoinType.SILVER);
        LogicProtos.Placement p3 = Logic.get().getStats().GetStats(CoinResource.CoinType.BRONZE);


        title.add(mLabel).top().expand();
        add(title).prefHeight(Value.percentHeight(0.2f, this));
        row();
        if (p1.getHandID() >= 0) {
            stats.add(new Label(String.format("Player %d", p1.getHandID()), style)).maxWidth(Value.percentWidth(0.4f, this)).expandX().left();
            stats.add(new Label("1st", style)).maxWidth(Value.percentWidth(0.2f, this)).expandX();
            stats.add(new Label(String.format("%d Turns", p1.getRounds()), style)).maxWidth(Value.percentWidth(0.4f, this)).expandX().right();
            stats.row();
        }
        if (p2.getHandID() >= 0) {
            stats.add(new Label(String.format("Player %d", p2.getHandID()), style)).maxWidth(Value.percentWidth(0.4f, this)).expandX().left();
            stats.add(new Label("2nd", style)).maxWidth(Value.percentWidth(0.2f, this)).expandX();
            stats.add(new Label(String.format("%d Turns", p2.getRounds()), style)).maxWidth(Value.percentWidth(0.4f, this)).expandX().right();
            stats.row();
        }
        if (p3.getHandID() >= 0) {
            stats.add(new Label(String.format("Player %d", p3.getHandID()), style)).maxWidth(Value.percentWidth(0.4f, this)).expandX().left();
            stats.add(new Label("3rd", style)).maxWidth(Value.percentWidth(0.2f, this)).expandX();
            stats.add(new Label(String.format("%d Turns", p3.getRounds()), style)).maxWidth(Value.percentWidth(0.4f, this)).expandX().right();
        }

        add(stats).expandX().fillX().prefHeight(Value.percentHeight(0.6f, this)).center();
        row();
        Table btnTable = new Table();
        TextButton tbRematch = new TextButton("Rematch", btnStyle);
        tbRematch.getLabel().setFontScale(0.75f);
        TextButton tbQuit = new TextButton("Quit", btnStyle);
        tbQuit.getLabel().setFontScale(0.75f);
        btnTable.add(tbRematch).prefWidth(100).height(36);
        btnTable.add().expandX();
        btnTable.add(tbQuit).prefWidth(100).height(36);
        add(btnTable).expandX().fillX().pad(Value.zero, Value.percentWidth(0.1f, this), Value.zero, Value.percentWidth(0.1f, this)).height(32);


    }

}
