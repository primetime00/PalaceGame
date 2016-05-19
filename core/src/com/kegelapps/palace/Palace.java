package com.kegelapps.palace;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.scenes.GameScene;
import com.kegelapps.palace.scenes.UIScene;

public class Palace extends ApplicationAdapter {

	@Override
	public void create () {

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
		Director.instance().dispose();
	}

	@Override
	public void pause() {
		super.pause();
		Gdx.graphics.requestRendering();
	}

	@Override
	public void resume() {
		super.resume();
	}
}
