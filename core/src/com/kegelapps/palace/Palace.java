package com.kegelapps.palace;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.graphics.CardUtils;

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
