package com.eyresgames.eyresapps.bombball;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;


public class GameView extends SurfaceView implements Runnable {

    volatile boolean playing;
    private Thread gameThread = null;
    private Context context;

    int index;
    private long score = 0;

    //an indicator if the game is Over
    public boolean isGameOver;

    private Paint paint;
    private Paint scorePaint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    private int scoreColor;

    private int screenX;
    private int screenY;
    Player player;
    ArrayList<Ball> balls = new ArrayList<>();


    int maxBallsOnScreen = 30;
    int maxSpeedBalls = 4;
    int maxRotationBalls = 6;
    int maxInvincibilityBalls = 1;
    int maxSuperBalls = 1;

    int currentSpeedBalls = 0;
    int currentRotationBalls = 0;
    int currentInvincibilityBalls = 0;
    int currentSuperBalls = 0;

    double rotate = 0;

    int greenBallsEaten = 0;
    Handler handler;
    Runnable r;
    Handler invincibleHandler;
    Runnable invincibleR;
    Handler superHandler;
    Runnable superR;
    long timeSuperActivated = 0;

    boolean speedBoostActive = false;
    boolean playerInvincible = false;
    boolean superActive = false;
    boolean reducingRadius = false;

    int bombs = 6;

    Lives lives;

    int smallBalls;
    int bigBalls;

    int middleOfScreenWidth;
    int middleOfScreenHeight;


    public GameView(Context context, int screenX, int screenY) {
        super(context);
        this.screenX = screenX;
        this.screenY = screenY;
        isGameOver = false;
        this.context = context;

        player = new Player(context, screenX, screenY);

        surfaceHolder = getHolder();
        paint = new Paint();
        scorePaint = new Paint();
        scoreColor = Color.WHITE;

        lives = Lives.getInstance();

        smallBalls = screenX / 18;
        bigBalls = screenX / 16;
        middleOfScreenWidth = screenX / 2;
        middleOfScreenHeight = screenY / 2;

    }

    @Override
    public void run() {
        while (playing) {
            update();
            draw();
            control();
            try {
                gameThread.sleep(1000 / 120);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        long currentTime = System.currentTimeMillis();
        player.update();

        if (balls.size() < maxBallsOnScreen) {
            //---------------------------------------------BALLS
            Random random = new Random();
            if (random.nextInt(10 - 1) + 1 == 5) {
                random = new Random();
                int x = random.nextInt(screenX);
                random = new Random();
                int y = random.nextInt(screenY);
                boolean touches = true;
                /*while (touches) {
                    if (balls.size() == 0) {
                        touches = false;
                    } else {
                        for (int j = 0; j < balls.size(); j++) {
                            if (Math.sqrt((balls.get(j).getX() - x) * (balls.get(j).getX() - x) + (balls.get(j).getY() - y) * (balls.get(j).getY() - y)) <= 100) {
                                x = random.nextInt(screenX);
                                touches = true;
                                break;
                            } else {
                                touches = false;
                            }
                        }
                    }

                }*/

                random = new Random();
                int angle = random.nextInt((360 - 0) + 1);
                if(greenBallsEaten % 100 == 0 && greenBallsEaten != 0){
                    if(greenBallsEaten / 100 == 3) {
                        bombs = 3;
                    }
                    else if(greenBallsEaten / 100 == 2) {
                        bombs = 4;
                    }
                    else if(greenBallsEaten / 100 == 1) {
                        bombs = 5;
                    }
                }
                if(balls.size()>0) {
                    System.out.println(balls.get(0).getxDirection());
                    System.out.println(balls.get(0).getyDirection());
                }
                if (new Random().nextInt(bombs - 1) + 1 == 3) {
                    if(balls.size() == 0 || balls.get(0).yDirection >= 0){
                        balls.add(new Ball(context, screenY, x, -smallBalls, Color.DKGRAY, 0, true, speedBoostActive, superActive, 0, smallBalls, greenBallsEaten/20));
                    }
                    if(balls.size() != 0 && balls.get(0).yDirection < 0){
                        balls.add(new Ball(context, screenY, x, screenY + smallBalls, Color.DKGRAY, 0, true, speedBoostActive, superActive, 0, smallBalls, greenBallsEaten/20));
                    }
                    if(balls.size() != 0 && balls.get(0).xDirection < 0){
                        balls.add(new Ball(context, screenY, screenX + smallBalls, y, Color.DKGRAY, 0, true, speedBoostActive, superActive, 0, smallBalls, greenBallsEaten/20));
                    }
                    if(balls.size() != 0 && balls.get(0).xDirection >= 0){
                        balls.add(new Ball(context, screenY, -smallBalls, y, Color.DKGRAY, 0, true, speedBoostActive, superActive, 0, smallBalls, greenBallsEaten/20));
                    }

                }
                else if (new Random().nextInt(14 - 1) + 1 == 3 && currentRotationBalls < maxRotationBalls) {
                    if(balls.size() == 0 || balls.get(0).yDirection >= 0){
                        balls.add(new Ball(context, screenY, x, -bigBalls, Color.rgb(0, 191, 255), angle, false, speedBoostActive, superActive, 250, bigBalls, greenBallsEaten/20));
                        currentRotationBalls++;

                    }
                    if(balls.size() != 0 && balls.get(0).yDirection < 0){
                        balls.add(new Ball(context, screenY, x, screenY + bigBalls, Color.rgb(0, 191, 255), angle, false, speedBoostActive, superActive, 250, bigBalls, greenBallsEaten/20));
                        currentRotationBalls++;

                    }
                    if(balls.size() != 0 && balls.get(0).xDirection < 0){
                        balls.add(new Ball(context, screenY, screenX + bigBalls, y, Color.rgb(0, 191, 255), angle, false, speedBoostActive, superActive, 250, bigBalls, greenBallsEaten/20));
                        currentRotationBalls++;

                    }
                    if(balls.size() != 0 && balls.get(0).xDirection >= 0){
                        balls.add(new Ball(context, screenY, -bigBalls, y, Color.rgb(0, 191, 255), angle, false, speedBoostActive, superActive, 250, bigBalls, greenBallsEaten/20));
                        currentRotationBalls++;

                    }
                }
                else if (new Random().nextInt(14 - 1) + 1 == 7 && currentSpeedBalls < maxSpeedBalls) {
                    if(balls.size() == 0 || balls.get(0).yDirection >= 0){
                        balls.add(new Ball(context, screenY, x, -bigBalls, Color.YELLOW, 0, false, speedBoostActive, superActive, 250, bigBalls, greenBallsEaten/20));
                        currentSpeedBalls++;

                    }
                    if(balls.size() != 0 && balls.get(0).yDirection < 0){
                        balls.add(new Ball(context, screenY, x, screenY + bigBalls, Color.YELLOW, 0, false, speedBoostActive, superActive, 250, bigBalls, greenBallsEaten/20));
                        currentSpeedBalls++;

                    }
                    if(balls.size() != 0 && balls.get(0).xDirection < 0){
                        balls.add(new Ball(context, screenY, screenX + bigBalls, y, Color.YELLOW, 0, false, speedBoostActive, superActive, 250, bigBalls, greenBallsEaten/20));
                        currentSpeedBalls++;

                    }
                    if(balls.size() != 0 && balls.get(0).xDirection >= 0){
                        balls.add(new Ball(context, screenY, -bigBalls, y, Color.YELLOW, 0, false, speedBoostActive, superActive, 250, bigBalls, greenBallsEaten/20));
                        currentSpeedBalls++;

                    }


                }
                else if (new Random().nextInt(69 - 1) + 1 == 29 && currentInvincibilityBalls < maxInvincibilityBalls) {
                    if(balls.size() == 0 || balls.get(0).yDirection >= 0){
                        balls.add(new Ball(context, screenY, x, -bigBalls, Color.rgb(255, 0, 255), 0, false, speedBoostActive, superActive, 0, bigBalls, greenBallsEaten/20));
                        currentInvincibilityBalls++;

                    }
                    if(balls.size() != 0 && balls.get(0).yDirection < 0){
                        balls.add(new Ball(context, screenY, x, screenY + bigBalls, Color.rgb(255, 0, 255), 0, false, speedBoostActive, superActive, 0, bigBalls, greenBallsEaten/20));
                        currentInvincibilityBalls++;

                    }
                    if(balls.size() != 0 && balls.get(0).xDirection < 0){
                        balls.add(new Ball(context, screenY, screenX + bigBalls, y, Color.rgb(255, 0, 255), 0, false, speedBoostActive, superActive, 0, bigBalls, greenBallsEaten/20));
                        currentInvincibilityBalls++;

                    }
                    if(balls.size() != 0 && balls.get(0).xDirection >= 0){
                        balls.add(new Ball(context, screenY, -bigBalls, y, Color.rgb(255, 0, 255), 0, false, speedBoostActive, superActive, 0, bigBalls, greenBallsEaten/20));
                        currentInvincibilityBalls++;

                    }


                }
                else if (new Random().nextInt(75 - 1) + 1 == 23 && currentSuperBalls < maxSuperBalls) {
                    if(balls.size() == 0 || balls.get(0).yDirection >= 0){
                        balls.add(new Ball(context, screenY, x, -bigBalls, Color.rgb(255, 215, 0), 0, false, speedBoostActive, superActive, 10, bigBalls, greenBallsEaten/20));
                        currentSuperBalls++;

                    }
                    if(balls.size() != 0 && balls.get(0).yDirection < 0){
                        balls.add(new Ball(context, screenY, x, screenY + bigBalls, Color.rgb(255, 215, 0), 0, false, speedBoostActive, superActive, 10, bigBalls, greenBallsEaten/20));
                        currentSuperBalls++;

                    }
                    if(balls.size() != 0 && balls.get(0).xDirection < 0){
                        balls.add(new Ball(context, screenY, screenX + bigBalls, y, Color.rgb(255, 215, 0), 0, false, speedBoostActive, superActive, 10, bigBalls, greenBallsEaten/20));
                        currentSuperBalls++;

                    }
                    if(balls.size() != 0 && balls.get(0).xDirection >= 0){
                        balls.add(new Ball(context, screenY, -bigBalls, y, Color.rgb(255, 215, 0), 0, false, speedBoostActive, superActive, 10, bigBalls, greenBallsEaten/20));
                        currentSuperBalls++;

                    }

                }
                else if(balls.size() < maxBallsOnScreen){
                    if(balls.size() == 0 || balls.get(0).yDirection >= 0){
                        balls.add(new Ball(context, screenY, x, -smallBalls, Color.GREEN, 0, false, speedBoostActive, superActive, 10 * (1 + (greenBallsEaten / 20)), smallBalls, greenBallsEaten/20));
                    }
                    if(balls.size() != 0 && balls.get(0).yDirection < 0){
                        balls.add(new Ball(context, screenY, x, screenY + smallBalls, Color.GREEN, 0, false, speedBoostActive, superActive, 10 * (1 + (greenBallsEaten / 20)), smallBalls, greenBallsEaten/20));
                    }
                    if(balls.size() != 0 && balls.get(0).xDirection < 0){
                        balls.add(new Ball(context, screenY, screenX + smallBalls, y, Color.GREEN, 0, false, speedBoostActive, superActive, 10 * (1 + (greenBallsEaten / 20)), smallBalls, greenBallsEaten/20));
                    }
                    if(balls.size() != 0 && balls.get(0).xDirection >= 0){
                        balls.add(new Ball(context, screenY, -smallBalls, y, Color.GREEN, 0, false, speedBoostActive, superActive, 10 * (1 + (greenBallsEaten / 20)), smallBalls, greenBallsEaten/20));
                    }

                }
            }

        }

        ///-------------------------------------------------------LAG
        for (int i = balls.size() - 1; i >= 0; i--) {
            balls.get(i).update(rotate);
            if (balls.get(i).getX() > screenX + balls.get(i).getRadius()*2 || balls.get(i).getY() > screenY + balls.get(i).getRadius()*2 || balls.get(i).getX() < -balls.get(i).getRadius()*2 || balls.get(i).getY() < -balls.get(i).getRadius()*2) {
                if (balls.get(i).getColor() == Color.YELLOW) {
                    currentSpeedBalls--;
                } else if (balls.get(i).getColor() == Color.rgb(0, 191, 255)) {
                    currentRotationBalls--;
                } else if (balls.get(i).getColor() == Color.rgb(255, 0, 255)) {
                    currentInvincibilityBalls--;
                } else if (balls.get(i).getColor() == Color.rgb(255, 215, 0)) {
                    currentSuperBalls--;
                }
                balls.remove(i);
            } else if (!balls.get(i).isBomb() && Math.sqrt((balls.get(i).getX() - player.getX()) * (balls.get(i).getX() - player.getX()) + (balls.get(i).getY() - player.getY()) * (balls.get(i).getY() - player.getY())) <= player.getRadius() + balls.get(i).getRadius()) {
                if (balls.get(i).getRotation() != 0) {
                    rotate = balls.get(i).getRotation();
                } else if (balls.get(i).getColor() == Color.YELLOW) {
                    player.setSpeedBoostActive(true);
                    speedBoostActive = true;
                    for (int j = 0; j < balls.size(); j++) {
                        balls.get(j).setSpeedBoost(true);
                    }
                    try {
                        handler.removeCallbacks(r);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    handler = new Handler(Looper.getMainLooper());

                    r = new Runnable() {
                        public void run() {
                            player.setSpeedBoostActive(false);
                            speedBoostActive = false;
                            for (int j = 0; j < balls.size(); j++) {
                                balls.get(j).setSpeedBoost(false);
                            }
                        }
                    };

                    handler.postDelayed(r, 10000);
                }
                if (balls.get(i).getColor() == Color.GREEN) {
                    greenBallsEaten++;
                    if (greenBallsEaten % 20 == 0) {
                        int radius = player.getRadius();
                        radius += (screenY / 380) * 2;
                        player.setRadius(radius);
                    }
                }
                if (balls.get(i).getColor() == Color.rgb(255, 0, 255)) {
                    timeSuperActivated = System.currentTimeMillis();
                    playerInvincible = true;
                    player.setInvincibe(true);
                    try {
                        invincibleHandler.removeCallbacks(invincibleR);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    invincibleHandler = new Handler(Looper.getMainLooper());

                    invincibleR = new Runnable() {
                        public void run() {
                            playerInvincible = false;
                            player.setInvincibe(false);
                        }
                    };

                    invincibleHandler.postDelayed(invincibleR, 10000);
                }
                if (balls.get(i).getColor() == Color.rgb(255, 215, 0)) {
                    superActive = true;
                    player.setSuper(true);
                    player.setRadius(screenY / 7);
                    reducingRadius = false;
                    for (int j = 0; j < balls.size(); j++) {
                        balls.get(j).setSuperSpeedBoost(true);
                    }
                    try {
                        superHandler.removeCallbacks(superR);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    superHandler = new Handler(Looper.getMainLooper());

                    superR = new Runnable() {
                        public void run() {
                            reducingRadius = true;
                            for (int j = 0; j < balls.size(); j++) {
                                balls.get(j).setSuperSpeedBoost(false);
                            }
                        }
                    };

                    superHandler.postDelayed(superR, 10000);

                }
                score += balls.get(i).getValue();
                if (balls.get(i).getColor() == Color.YELLOW) {
                    currentSpeedBalls--;
                } else if (balls.get(i).getColor() == Color.rgb(0, 191, 255)) {
                    currentRotationBalls--;
                } else if (balls.get(i).getColor() == Color.rgb(255, 0, 255)) {
                    currentInvincibilityBalls--;
                } else if (balls.get(i).getColor() == Color.rgb(255, 215, 0)) {
                    currentSuperBalls--;
                }
                balls.remove(i);
            } else if (balls.get(i).isBomb() && Math.sqrt((balls.get(i).getX() - player.getX()) * (balls.get(i).getX() - player.getX()) + (balls.get(i).getY() - player.getY()) * (balls.get(i).getY() - player.getY())) >= player.getRadius() + 300 + balls.get(i).getRadius()) {
                balls.get(i).setClose(false);
            } else if (balls.get(i).isBomb() && Math.sqrt((balls.get(i).getX() - player.getX()) * (balls.get(i).getX() - player.getX()) + (balls.get(i).getY() - player.getY()) * (balls.get(i).getY() - player.getY())) <= player.getRadius() + balls.get(i).getRadius()) {
                //balls.remove(i);
                if(superActive){
                    balls.remove(i);
                }
                else if(playerInvincible){
                    //do nothing
                }
                else if (player.getRadius() > smallBalls) {
                    int radius = player.getRadius();
                    radius -= (screenY / 380) * 2;
                    greenBallsEaten -= 20;
                    player.setRadius(radius);
                } else {
                    int currentLives = lives.getLives();
                    currentLives--;
                    lives.setLives(currentLives, context);
                    isGameOver = true;
                    playing = false;
                }
            } else if (balls.get(i).isBomb() && Math.sqrt((balls.get(i).getX() - player.getX()) * (balls.get(i).getX() - player.getX()) + (balls.get(i).getY() - player.getY()) * (balls.get(i).getY() - player.getY())) < player.getRadius() + 300 + balls.get(i).getRadius()) {
                balls.get(i).setClose(true);
            } else if (balls.get(i).getRadius() == 0) {
                if (balls.get(i).getColor() == Color.YELLOW) {
                    currentSpeedBalls--;
                } else if (balls.get(i).getColor() == Color.rgb(0, 191, 255)) {
                    currentRotationBalls--;
                } else if (balls.get(i).getColor() == Color.rgb(255, 0, 255)) {
                    currentInvincibilityBalls--;
                } else if (balls.get(i).getColor() == Color.rgb(255, 215, 0)) {
                    currentSuperBalls--;
                }
                balls.remove(i);
            }
        }
        ///-------------------------------------------------------LAG
        for (int i = balls.size() - 1; i >= 0; i--) {
            if (balls.get(i) != null && balls.get(i).isBomb()) {
                for (int j = balls.size() - 1; j >= 0; j--) {
                    if (j < balls.size() && i < balls.size() && !balls.get(j).isBomb() && Math.sqrt((balls.get(i).getX() - balls.get(j).getX()) * (balls.get(i).getX() - balls.get(j).getX()) + (balls.get(i).getY() - balls.get(j).getY()) * (balls.get(i).getY() - balls.get(j).getY())) <= balls.get(j).getRadius() + balls.get(i).getRadius()) {
                        balls.remove(j);
                    }
                }
            }
        }
        if(reducingRadius){
            int checkRadius = player.getRadius();
            int newRadius = ((smallBalls) + ((greenBallsEaten / 20)*10));
            if(checkRadius > newRadius){
                checkRadius -= (screenY / 380) * 2;
                player.setRadius(checkRadius);
            }
            else {
                player.setSuper(false);
                reducingRadius = false;
                superActive = false;
            }
        }

        System.out.println("in update for :" + (System.currentTimeMillis() - currentTime));
    }

    private void draw() {
        long currentTime = System.currentTimeMillis();
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);

            paint.setColor(player.getColor());
            canvas.drawCircle(player.getX(), player.getY(), player.getRadius(), paint);

            for (int i = 0; i < balls.size(); i++) {
                paint.setColor(balls.get(i).getColor());
                canvas.drawCircle(balls.get(i).getX(), balls.get(i).getY(), balls.get(i).getRadius(), paint);
                if (balls.get(i).getColor() != Color.DKGRAY && balls.get(i).getColor() != Color.GRAY && balls.get(i).getColor() != Color.RED  && balls.get(i).getColor() != Color.rgb(255,215,0)  && balls.get(i).getColor() != Color.rgb(255,0,255) ) {
                    canvas.drawBitmap(
                            textAsBitmap(balls.get(i).getValue() + "", smallBalls - 5, Color.BLACK),
                            balls.get(i).getX() - textAsBitmap(balls.get(i).getValue() + "", smallBalls - 5, Color.BLACK).getWidth() / 2,
                            balls.get(i).getY() - textAsBitmap(balls.get(i).getValue() + "", smallBalls - 5, Color.BLACK).getHeight() / 2,
                            scorePaint);
                }

            }

            //draw game Over when the game is over
            if (isGameOver) {
                SharedPreferences sharedPreferences = context.getSharedPreferences("highScore", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("lastScore", score);
                long one = 0;
                one = sharedPreferences.getLong("1", one);
                long two = 0;
                two = sharedPreferences.getLong("2", two);
                long three = 0;
                three = sharedPreferences.getLong("3", three);
                if (one == 0 || score > one) {
                    if(two != 0){
                        editor.putLong("3", two);
                    }
                    if(one != 0){
                        editor.putLong("2", one);
                    }
                    editor.putLong("1", score);
                    //Games.Leaderboards.submitScore(mGoogleApiClient, LEADERBOARD_ID, 1337);-----------------------------------------------------------------------------------------------
                } else if (two == 0 || score > two) {
                    if(two != 0){
                        editor.putLong("3", two);
                    }
                    editor.putLong("2", score);

                } else if (three == 0 || score > three) {
                    editor.putLong("3", score);
                }
                editor.commit();
                paint.setColor(Color.WHITE);
                paint.setTextSize(screenY/13);
                paint.setTextAlign(Paint.Align.CENTER);

                int yPos = (int) ((middleOfScreenHeight) - ((paint.descent() + paint.ascent()) / 2));
                canvas.drawText("Game Over", middleOfScreenWidth, yPos, paint);

                paint.setTextSize(screenY / 20);
                sharedPreferences = context.getSharedPreferences("highScore", Context.MODE_PRIVATE);
                if (sharedPreferences != null && sharedPreferences.getLong("2", two) != 0 && sharedPreferences.getLong("3", three) != 0) {
                    canvas.drawText("1: " + sharedPreferences.getLong("1", one), middleOfScreenWidth, yPos + screenY/13, paint);
                    canvas.drawText("2: " + sharedPreferences.getLong("2", two), middleOfScreenWidth, yPos + screenY/13 + screenY/20, paint);
                    canvas.drawText("3: " + sharedPreferences.getLong("3", three), middleOfScreenWidth, yPos + screenY/13 + (screenY/20 * 2), paint);
                } else if (sharedPreferences != null && sharedPreferences.getLong("2", two) != 0) {
                    canvas.drawText("1: " + sharedPreferences.getLong("1", one), middleOfScreenWidth, yPos + screenY/13, paint);
                    canvas.drawText("2: " + sharedPreferences.getLong("2", two), middleOfScreenWidth, yPos + screenY/13 + screenY/20, paint);
                    canvas.drawText("3: ", middleOfScreenWidth, yPos + screenY/13 + (screenY/20 * 2), paint);
                } else {
                    canvas.drawText("1: " + sharedPreferences.getLong("1", one), middleOfScreenWidth, yPos + screenY/13, paint);
                    canvas.drawText("2: ", middleOfScreenWidth, yPos + screenY/13 + screenY/20, paint);
                    canvas.drawText("3: ", middleOfScreenWidth, yPos + screenY/13 + (screenY/20 * 2), paint);
                }

                Handler handler = new Handler(Looper.getMainLooper());

                Runnable r = new Runnable() {
                    public void run() {
                        //Intent intent = new Intent(context ,MainActivity.class);
                        //context.startActivity(intent);
                        ((GameActivity)context).finish();
                    }
                };

                handler.postDelayed(r, 2000);

            }


            paint.setColor(Color.WHITE);

            canvas.drawBitmap(
                    textAsBitmap("SCORE : " + score, screenY / 31, scoreColor),
                    screenX / 2 - textAsBitmap("SCORE : " + score, screenY/36, scoreColor).getWidth() / 2,
                    screenY - textAsBitmap("SCORE : " + score, screenY/36, scoreColor).getHeight() * 3,
                    scorePaint);
            if(playerInvincible) {
                canvas.drawBitmap(
                        textAsBitmap("" + (10 + ((timeSuperActivated - System.currentTimeMillis()) / 1000)), screenY / 31, scoreColor),
                        screenX / 2 - textAsBitmap("" + (10 + ((timeSuperActivated - System.currentTimeMillis()) / 1000)), screenY / 31, scoreColor).getWidth()/2,
                        textAsBitmap("" + (10 + ((timeSuperActivated - System.currentTimeMillis()) / 1000)), screenY / 31, scoreColor).getHeight(),
                        scorePaint);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
            System.out.println("in draw for :" + (System.currentTimeMillis() - currentTime));

        }
    }

    private void control() {
        try {
            gameThread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                    player.setMoveToX(event.getX());
                    player.setMoveToY(event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                    player.setMoveToX(event.getX());
                    player.setMoveToY(event.getY());
                break;
            case MotionEvent.ACTION_UP:
                    player.setMoveToX(player.getX());
                    player.setMoveToY(player.getY());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                index = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                event.getX(index);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                index = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                event.getX(index);
                break;
        }
        return true;
    }

    public Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }
}