package com.kegelapps.palace.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.kegelapps.palace.graphics.CardView;
import com.kegelapps.palace.graphics.DeckView;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
import static com.badlogic.gdx.math.Interpolation.*;


/**
 * Created by keg45397 on 12/15/2015.
 */
public class GraphicActions {

    static public Action DrawToActive(DeckView deck, final CardView card) {
        return parallel(
                moveTo(deck.getX()+deck.getWidth()+5, deck.getY(), 0.3f, swingIn),
                sequence(delay(0.15f),
                        run(new Runnable() {
                            @Override
                            public void run() {
                                card.setSide(CardView.Side.FRONT);
                            }
                        })
                ));
    }
}
