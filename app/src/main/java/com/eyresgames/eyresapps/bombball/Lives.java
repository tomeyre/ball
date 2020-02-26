package com.eyresgames.eyresapps.bombball;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by thomaseyre on 01/09/2017.
 */

public class Lives {
    private static final Lives ourInstance = new Lives();

    public static Lives getInstance() {
        return ourInstance;
    }

    public int lives;

    private Lives() {
    }

    public void initializeLives(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("lives", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String lives = "";
        lives = sharedPreferences.getString("lives", lives);
        if(lives.equals("")){
            lives = "5";
            editor.putString("lives", lives);
            editor.commit();
            this.lives = Integer.parseInt(lives);
        }else{
            this.lives = Integer.parseInt(lives);
        }
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("lives", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lives", Integer.toString(lives));
        editor.commit();
        this.lives = lives;
    }
}
