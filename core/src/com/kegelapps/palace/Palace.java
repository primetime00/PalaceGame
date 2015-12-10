package com.kegelapps.palace;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kegelapps.palace.graphics.CardUtils;
import com.kegelapps.palace.graphics.CardView;
import com.kegelapps.palace.graphics.DeckView;

public class Palace extends ApplicationAdapter {
	private OrthographicCamera mCamera;
	SpriteBatch batch;
	Texture img;
	Logic logic;
	DeckView deck;

	@Override
	public void create () {
		mCamera = new OrthographicCamera();
		mCamera.setToOrtho(false, 800, 480);
		batch = new SpriteBatch();
		img = new Texture("cards_tiny.jpg");
		CardUtils.loadCards(CardUtils.CardSize.TINY);
        logic = new Logic();
		deck = new DeckView();
        Gdx.input.setInputProcessor(Input.get());
	}

	@Override
	public void render () {
        logic.Poll();
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		mCamera.update();

		batch.setProjectionMatrix(mCamera.combined);
		batch.begin();
		//batch.draw(CardUtils.getCardTexture(0), 0, 0);
		deck.draw(batch);
		batch.end();
	}
}
