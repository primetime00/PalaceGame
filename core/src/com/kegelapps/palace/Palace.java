package com.kegelapps.palace;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kegelapps.palace.graphics.CardUtils;

public class Palace extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	Logic logic;

	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("card_back.jpg");
		CardUtils.loadCards("cards.jpg");
        logic = new Logic();
        Gdx.input.setInputProcessor(Input.get());
	}

	@Override
	public void render () {
        logic.Poll();
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();
	}
}
