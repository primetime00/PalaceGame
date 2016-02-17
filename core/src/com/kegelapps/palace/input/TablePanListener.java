package com.kegelapps.palace.input;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.kegelapps.palace.Director;
import com.kegelapps.palace.animations.AnimationBuilder;
import com.kegelapps.palace.animations.AnimationFactory;
import com.kegelapps.palace.animations.CameraAnimation;
import com.kegelapps.palace.engine.Logic;
import com.kegelapps.palace.engine.states.State;
import com.kegelapps.palace.graphics.CardCamera;
import com.kegelapps.palace.graphics.HandView;
import com.kegelapps.palace.graphics.TableView;
import com.kegelapps.palace.graphics.utils.HandUtils;

/**
 * Created by keg45397 on 2/17/2016.
 */
public class TablePanListener extends ActorGestureListener {

    private TableView mTable;
    private long lastTime = 0;
    private TouchPosition mPosition = TouchPosition.NONE;
    private enum TouchPosition {
        TOP,
        LEFT,
        RIGHT,
        NONE
    }


    public TablePanListener(TableView table) {
        super(20, 0.4f, 1.1f, 0.15f);
        mTable = table;
    }

    @Override
    public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
        //a hack
        if (System.currentTimeMillis() - lastTime < 25)
            return;
        lastTime = System.currentTimeMillis();
        CardCamera cam = mTable.getCamera();
        HandView top, bottom, left, right;
        left = mTable.getHandFromSide(HandUtils.HandSide.SIDE_LEFT);
        right = mTable.getHandFromSide(HandUtils.HandSide.SIDE_RIGHT);
        top = mTable.getHandFromSide(HandUtils.HandSide.SIDE_TOP);
        bottom = mTable.getHandFromSide(HandUtils.HandSide.SIDE_BOTTOM);
        float panX, panY;
        panX = -deltaX;
        panY = -deltaY*2;
        switch (mPosition) {
            case TOP:
                if (deltaY >= 0)
                    panY = 0;
                cam.position.add(panX, panY / 1.0f, 0);
                if (cam.position.y + (cam.viewportHeight / 2.0f) > top.getActivePosition().getY() + top.getActivePosition().getHeight())
                    cam.position.y = (top.getActivePosition().getY() + top.getActivePosition().getHeight()) - (cam.viewportHeight / 2.0f);
                if (cam.position.x + (cam.viewportWidth / 2.0f) > right.getActivePosition().getX() + right.getActivePosition().getWidth())
                    cam.position.x = right.getActivePosition().getX() + right.getActivePosition().getWidth() - (cam.viewportWidth / 2.0f);
                else if (cam.position.x - (cam.viewportWidth / 2.0f) < left.getActivePosition().getX())
                    cam.position.x = left.getActivePosition().getX() + (cam.viewportWidth / 2.0f);
                break;
            case RIGHT:
                if (deltaX >= 0)
                    panX = 0;
                cam.position.add(panX / 1.0f, panY, 0);
                if (cam.position.x + (cam.viewportWidth / 2.0f) > right.getActivePosition().getX() + right.getActivePosition().getWidth())
                    cam.position.x = right.getActivePosition().getX() + right.getActivePosition().getWidth() - (cam.viewportWidth / 2.0f);
                if (cam.position.y + (cam.viewportHeight / 2.0f) > top.getActivePosition().getY() + top.getActivePosition().getHeight())
                    cam.position.y = (top.getActivePosition().getY() + top.getActivePosition().getHeight()) - (cam.viewportHeight / 2.0f);
                else if (cam.position.y - (cam.viewportHeight / 2.0f) < bottom.getActivePosition().getY())
                    cam.position.y = bottom.getActivePosition().getY() + (cam.viewportHeight / 2.0f);
                break;
            case LEFT:
                if (deltaX <= 0)
                    panX = 0;
                cam.position.add(panX / 1.0f, panY, 0);
                if (cam.position.x - (cam.viewportWidth / 2.0f) < left.getActivePosition().getX())
                    cam.position.x = left.getActivePosition().getX() + (cam.viewportWidth / 2.0f);
                if (cam.position.y + (cam.viewportHeight / 2.0f) > top.getActivePosition().getY() + top.getActivePosition().getHeight())
                    cam.position.y = (top.getActivePosition().getY() + top.getActivePosition().getHeight()) - (cam.viewportHeight / 2.0f);
                else if (cam.position.y - (cam.viewportHeight / 2.0f) < bottom.getActivePosition().getY())
                    cam.position.y = bottom.getActivePosition().getY() + (cam.viewportHeight / 2.0f);

                break;
            default: break;
        }
    }

    @Override
    public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
        super.touchDown(event, x, y, pointer, button);
        float w = Director.instance().getScreenWidth();
        float h = Director.instance().getScreenHeight();
        CardCamera cam = mTable.getCamera();
        if (mPosition == TouchPosition.NONE) {
            Vector3 pos = mTable.getCamera().unproject(new Vector3(x,y,0));
            if (pos.x > w*0.1f && pos.x < w - (w*0.1f) && pos.y < h*0.25f) { //top center
                mPosition = TouchPosition.TOP;
            }
            else if (pos.x < w*0.25f && pos.y < h - (h*0.20f) && pos.y > h*0.20f) { //Left center
                mPosition = TouchPosition.LEFT;
            }
            else if (pos.x > w-(w*0.25f) && pos.y < h - (h*0.20f) && pos.y > h*0.20f) { //Right center
                mPosition = TouchPosition.RIGHT;
            }
        }
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        super.touchUp(event, x, y, pointer, button);
        CardCamera cam = mTable.getCamera();
        if (mPosition != TouchPosition.NONE) {
            AnimationBuilder builder = AnimationFactory.get().createAnimationBuilder(AnimationFactory.AnimationType.CAMERA);
            builder.setPause(true).setDescription("Move camera to current turn").setTable(mTable).
                    setCameraSide(cam.GetSide()).setCamera(cam).
                    setTweenCalculator(new CameraAnimation.MoveToSide(1.0f, 0.5f)).build().Start();
        }
        mPosition = TouchPosition.NONE;

    }


    @Override
    public boolean handle(Event e) {
        State s = Logic.get().GetMainState();
        if (s == null)
            return false;
        if (s.containsState(State.Names.PLAY_HUMAN_TURN) || s.containsState(State.Names.SELECT_END_CARDS))
            return super.handle(e);
        return false;
    }
}
