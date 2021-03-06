package com.kegelapps.palace.animations;

import aurelienribon.tweenengine.*;
import com.badlogic.gdx.math.Vector2;
import com.kegelapps.palace.graphics.CardCamera;
import com.kegelapps.palace.graphics.utils.HandUtils;
import com.kegelapps.palace.graphics.TableView;
import com.kegelapps.palace.graphics.utils.InPlayUtils;
import com.kegelapps.palace.tween.CameraAccessor;

import java.util.List;

/**
 * Created by Ryan on 12/24/2015.
 */
public class CameraAnimation extends Animation {

    private boolean mPauseLogic = false;
    private CardCamera.CameraSide mSide;
    private CardCamera mCamera;
    private TableView mTable;

    public CameraAnimation(boolean pauseLogic, List<AnimationStatus> listeners, BaseTween<Timeline> timeLineAnimation, Animation child, String description, AnimationFactory.AnimationType type, Object killPrevious, CardCamera mCamera, CardCamera.CameraSide mSide, TableView table) {
        super(pauseLogic, listeners, timeLineAnimation, child, description, type, killPrevious);
        this.mCamera = mCamera;
        this.mSide = mSide;
        this.mTable = table;
    }

    static public class MoveCamera extends TweenProcessor {

        float mX, mY, mZoom;
        float mDuration;

        public MoveCamera(float x, float y, float zoom, float duration) {
            mX =x;
            mY = y;
            mZoom = zoom;
            mDuration = duration;
        }

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            CardCamera mCamera = builder.getCamera();
            CardCamera.CameraSide mSide = builder.getCameraSide();
            mAnimation.beginParallel();
            mAnimation.push(Tween.to(mCamera, CameraAccessor.POSITION_XY, mDuration).target(mX,mY));
            mAnimation.push(Tween.to(mCamera, CameraAccessor.ZOOM, mDuration).target(mZoom));
            return mAnimation.end();
        }
    }

    static public class MoveToSide extends TweenProcessor {

        float mZoom;
        float mDuration;

        public MoveToSide(float zoom, float duration) {
            mZoom = zoom;
            mDuration = duration;
        }

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            if (builder.getTable() == null)
                throw new RuntimeException("MoveToSide animation requires a TableView!");
            TweenEquation eq = TweenEquations.easeInOutQuart;
            CardCamera mCamera = builder.getCamera();
            CardCamera.CameraSide mSide = builder.getCameraSide();
            Vector2 pos = HandUtils.GetHandPosition(builder.getTable(), HandUtils.CameraSideToHand(mSide));
            mAnimation.push(Tween.set(mCamera, CameraAccessor.POSITION_XY).target(mCamera.position.x, mCamera.position.y));
            mAnimation.beginParallel();
            mAnimation.push(Tween.to(mCamera, CameraAccessor.POSITION_XY, mDuration).target(pos.x,pos.y).ease(eq));
            mAnimation.push(Tween.to(mCamera, CameraAccessor.ZOOM, mDuration).target(mZoom));
            return mAnimation.end();
        }
    }

    static public class ZoomToPlayCards extends TweenProcessor {

        float mZoom;
        float mDuration;

        public ZoomToPlayCards(float zoom, float duration) {
            mZoom = zoom;
            mDuration = duration;
        }

        @Override
        public BaseTween<Timeline> calculate(AnimationBuilder builder) {
            if (builder.getTable() == null)
                throw new RuntimeException("ZoomToPlayCards animation requires a TableView!");
            TweenEquation eq = TweenEquations.easeNone;
            CardCamera mCamera = builder.getCamera();
            CardCamera.CameraSide mSide = builder.getCameraSide();
            Vector2 pos = InPlayUtils.getCenterCameraPoint(builder.getTable().getPlayView());
            mAnimation.beginParallel();
            mAnimation.push(Tween.to(mCamera, CameraAccessor.POSITION_XY, mDuration).target(pos.x,pos.y).ease(eq));
            mAnimation.push(Tween.to(mCamera, CameraAccessor.ZOOM, mDuration).target(mZoom));
            return mAnimation.end();
        }
    }

    @Override
    public AnimationBuilder toBuilder() {
        return super.toBuilder().setCamera(mCamera).setCameraSide(mSide).setTable(mTable);
    }



    @Override
    public void onEnd() {
        mCamera.SetSide(mSide);
    }

}
