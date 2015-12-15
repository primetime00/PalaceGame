package com.kegelapps.palace;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ArrayMap;

import java.util.Iterator;

/**
 * Created by keg45397 on 12/8/2015.
 */
public class Input implements InputProcessor {

    private static Input mInstance = null;

    public interface OnInputLogic {
        void onTouched();
    }

    static public class InputLogicAdapter implements  OnInputLogic{
        private BoundObject mBound;
        public InputLogicAdapter() { mBound = null; }
        public InputLogicAdapter(BoundObject b) { mBound = b; }

        public void process(int screenX, int screenY, int button) {
            if (mBound == null) {
                onTouched();
                return;
            }
            Rectangle rect = mBound.getBounds();
            if (rect.contains(screenX, screenY)) {
                onTouched();
            }
        }
        @Override
        public void onTouched() {
            return;
        }
    }

    public interface BoundObject {
        Rectangle getBounds();
    }

    private ArrayMap<String, InputLogicAdapter> mAdapterList;

    static public Input get() {
        if (mInstance == null)
            mInstance = new Input();
        return mInstance;
    }

    public Input() {
        mAdapterList = new ArrayMap<>();
    }

    public void addInputLogicAdapter(String name, InputLogicAdapter adapter) {
        mAdapterList.put(name, adapter);
    }

    public void removeInputLogicAdapter(String key) {
        mAdapterList.removeKey(key);
    }


    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        for (InputLogicAdapter adapter : mAdapterList.values()) {
            adapter.process(screenX, screenY, button);
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
