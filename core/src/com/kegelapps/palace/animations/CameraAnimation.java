package com.kegelapps.palace.animations;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.graphics.CardCamera;
import com.kegelapps.palace.graphics.CardView;
import com.kegelapps.palace.tween.CameraAccessor;
import com.kegelapps.palace.tween.CardAccessor;

/**
 * Created by Ryan on 12/24/2015.
 */
public class CameraAnimation implements TweenCallback {

    private boolean mPauseLogic = false;
    private CardCamera.CameraSide mSide;
    private CardCamera mCamera;
    public CameraAnimation(boolean pauseLogic) {
        mPauseLogic = pauseLogic;
    }

    public CameraAnimation() {
        mPauseLogic = false;
    }

    public BaseTween<Timeline> MoveCamera(float duration, float x, float y, float zoom, CardCamera camera, CardCamera.CameraSide side) {
        mCamera = camera;
        mSide = side;
        Timeline animation = Timeline.createParallel();
        animation.setCallback(this);
        animation.setCallbackTriggers(TweenCallback.BEGIN | TweenCallback.END);
        animation.push(Tween.to(camera, CameraAccessor.POSITION_XY, duration).target(x,y));
        animation.push(Tween.to(camera, CameraAccessor.ZOOM, duration).target(zoom));
        animation.start(Director.instance().getTweenManager());
        return animation;
    }

    @Override
    public void onEvent(int type, BaseTween<?> source) {
        if (type == TweenCallback.BEGIN) {
            if (mPauseLogic)
                Logic.get().Pause(true);
        }
        if (type == TweenCallback.END) {
            if (mPauseLogic)
                Logic.get().Pause(false);
            if (mCamera != null)
                mCamera.SetSide(mSide);
        }
    }
}
