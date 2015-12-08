package com.kegelapps.palace;

import com.badlogic.gdx.utils.Disposable;

/**
 * Created by keg45397 on 12/8/2015.
 */
public class Action implements Disposable{

    protected boolean mDisposed = false;

    public interface OnAction {
        void onActionComplete();
    }

    protected OnAction mActionListener;

    public Action() {

    }

    public void setOnActionComplete(OnAction listener) {
        mActionListener = listener;
    }

    public boolean isDisposed() {
        return mDisposed;
    }

    @Override
    public void dispose() {
        mDisposed = true;
    }

}
