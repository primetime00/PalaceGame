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
import com.kegelapps.palace.graphics.TableView;

public class Palace extends ApplicationAdapter {
	private OrthographicCamera mCamera;
	SpriteBatch batch;
	Texture img;
	Logic logic;
	Table table;
	TableView tableView;

	@Override
	public void create () {
		mCamera = new OrthographicCamera();
		mCamera.setToOrtho(false, 800, 480);
		batch = new SpriteBatch();
		img = new Texture("cards_tiny.jpg");
		CardUtils.loadCards(CardUtils.CardSize.TINY);
        logic = new Logic();
		table = new Table(new Deck(), 4, null);
		tableView = new TableView(table);
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
		tableView.draw(batch);
		batch.end();
	}

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (tableView != null)
            tableView.onScreenSize(width, height);
    }
}
