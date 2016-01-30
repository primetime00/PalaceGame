package com.kegelapps.palace;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.graphics.*;

public class Palace extends ApplicationAdapter {
	GameScene mGameScene;

	@Override
	public void create () {
		CardUtils.loadCards(CardUtils.CardSize.TINY);

        Logic.get().SetNumberOfPlayers(4);
        Logic.get().Initialize();
        mGameScene = new GameScene(new ExtendViewport(800,480));
        Director.instance().setScene(mGameScene);
	}


	@Override
	public void render () {
        Director.instance().update();
	}

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

	@Override
	public void dispose() {
		super.dispose();
		mGameScene.dispose();
	}
}
