package com.kegelapps.palace;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.kegelapps.palace.graphics.CardUtils;
import com.kegelapps.palace.graphics.CardView;
import com.kegelapps.palace.graphics.DeckView;
import com.kegelapps.palace.graphics.TableView;

public class Palace extends ApplicationAdapter {
	private OrthographicCamera mCamera;
	GameScene mGameScene;

	@Override
	public void create () {
		CardUtils.loadCards(CardUtils.CardSize.TINY);
		mGameScene = new GameScene(new ExtendViewport(800,480));
        Director.instance().setScene(mGameScene);
	}


	@Override
	public void render () {
        Director.instance().update();

/*		mCamera.update();

		batch.setProjectionMatrix(mCamera.combined);
		batch.begin();
		tableView.draw(batch);
		batch.end();*/
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
