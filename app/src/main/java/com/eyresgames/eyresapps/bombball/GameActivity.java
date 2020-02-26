package com.eyresgames.eyresapps.bombball;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;


public class GameActivity extends AppCompatActivity {


    //declaring gameview
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //Getting display object
        Display display = getWindowManager().getDefaultDisplay();


        //Getting the screen resolution into point object
        Point size = new Point();
        display.getSize(size);

        //Initializing game view object
        //this time we are also passing the screen size to the GameView constructor
        gameView = new GameView(this, size.x, size.y);

        //adding it to contentview
        setContentView(gameView);
    }

    //pausing the game when activity is paused
    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    //running the game when activity is resumed
    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    @Override
    public void onBackPressed(){
        //do nothing
    }

}