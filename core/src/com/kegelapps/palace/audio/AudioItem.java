package com.kegelapps.palace.audio;

import com.badlogic.gdx.audio.Sound;

/**
 * Created by Ryan on 3/21/2016.
 */
public class AudioItem {

    private long id;
    private float time;
    private Sound sound;


    public AudioItem(Sound s, long id) {
        this.id = id;
        this.time = 0;
        this.sound = s;
    }

    public void addTime(float time) {
        this.time += time;
    }

    public boolean isOverTime(float over) {
        return time > over;
    }

    public Sound getSound() {
        return sound;
    }

    public long getID() { return id;}

}
