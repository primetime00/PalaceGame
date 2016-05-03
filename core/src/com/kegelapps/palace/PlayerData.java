package com.kegelapps.palace;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;

import java.util.Random;

/**
 * Created by keg45397 on 5/2/2016.
 */
public class PlayerData {
    private String name;
    private int id;
    private float play_multiple;
    private int hidden_order;
    private float end_card_spread;
    ObjectMap<String, CommentData> comments;

    static class CommentData {
        private float rate;
        private String comment;

        public CommentData(String comment, float rate) {
            this.comment = comment;
            this.rate = rate;
        }
    }

    public PlayerData() {
        name = "";
        id = 0;
        play_multiple = 0f;
        hidden_order = 0;
        end_card_spread = 0f;
        comments = new ObjectMap<>();
    }

    public PlayerData(XmlReader.Element root) {
        try {
            id = Integer.parseInt(root.getAttribute("id"));
            name = root.getChildByName("name").getText();
            XmlReader.Element ai = root.getChildByName("ai");
            play_multiple = Float.parseFloat(ai.getAttribute("play_multiple"));
            hidden_order = Integer.parseInt(ai.getAttribute("hidden_order"));
            end_card_spread = Float.parseFloat(ai.getAttribute("end_card_spread"));
            XmlReader.Element comments_element = root.getChildByName("comments");
            comments = new ObjectMap<>();
            for (XmlReader.Element comment : comments_element.getChildrenByName("comment")) {
                comments.put(comment.getAttribute("id"), new CommentData(comment.getText(), Float.parseFloat(comment.getAttribute("chance"))));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not parse names xml");
        }
    }

    public String getComment(String key) {
        float rate = (float) Math.random();
        CommentData data = comments.get(key);
        return rate <= data.rate ? data.comment : "";
    }

    public String getRandomComment(String keyPrefix) {
        float rate = (float) Math.random();
        Array<String> keys = new Array<>();
        for (ObjectMap.Entry<String, CommentData>entry : comments.entries()) {
            if (entry.key.startsWith(keyPrefix))
                keys.add(entry.key);
        }
        if (keys.size == 0)
            return "";
        int val = MathUtils.random(0, keys.size-1);
        CommentData data = comments.get(keys.get(val));
        return rate <= data.rate ? data.comment : "";
    }

    public float getEndCardSpread() {
        return end_card_spread;
    }

    public int getHiddenOrder() {
        return hidden_order;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getPlayMultiple() {
        return play_multiple;
    }
}
