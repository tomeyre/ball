package com.eyresgames.eyresapps.bombball;

import android.content.Context;
import android.graphics.Color;

import java.util.Random;


public class Player {
    private int x;
    private int y;
    private float xSpeed = 50;
    private float ySpeed = 50;

    private float MAX_SPEED;
    private float MAX_BOOST_SPEED;
    private int frameWidth = 100;

    float moveToX = 0;
    float moveToY = 0;

    int rotationCount = 0;

    int maxX;
    int maxY;

    float xTrajectory;
    float yTrajectory;

    double direction;

    int color;

    boolean speedBoostActive = false;
    boolean isInvincibe = false;
    boolean isSuper = false;

    int radius;

    public Player(Context context, int screenX, int screenY) {

        radius = screenX / 18;

        moveToX = screenX/2;
        moveToY = screenY-screenY/5;

        x = screenX/2 - radius;
        y = screenY-screenY/5;



        maxX = screenX;
        maxY = screenY;

        color = Color.WHITE;

        MAX_SPEED = screenY / 20;
        MAX_BOOST_SPEED = screenY / 10;

    }


    public void update() {
        if(isSuper){
            color = Color.rgb(255,215,0);
        } else if(isInvincibe){
            color = Color.rgb(new Random().nextInt(256 - 100) + 100, new Random().nextInt(256 - 100) + 100, new Random().nextInt(256 - 100) + 100);
        }else {
            color = Color.WHITE;
        }
        if(!speedBoostActive){
            xSpeed = MAX_SPEED;
        }
        else{
            xSpeed = MAX_BOOST_SPEED;
        }
        if(moveToX < radius){
            moveToX = radius;
        }
        if(moveToX > maxX - radius){
            moveToX = maxX - radius;
        }
        xTrajectory = moveToX - getX();
        if(xTrajectory < xSpeed && xTrajectory > - xSpeed){
            if(xTrajectory > 0) {
                xSpeed = xTrajectory;
            }
            else{
                xSpeed = xTrajectory * -1;
            }
        }
        if(!speedBoostActive){
            ySpeed = MAX_SPEED;
        }
        else{
            ySpeed = MAX_BOOST_SPEED;
        }
        if(moveToY < radius){
            moveToY = radius;
        }
        if(moveToY > maxY - radius*2){
            moveToY = maxY - radius*2;
        }
        yTrajectory = moveToY - getY();
        if(yTrajectory < ySpeed && yTrajectory > - ySpeed){
            if(yTrajectory > 0) {
                ySpeed = yTrajectory;
            }
            else{
                ySpeed = yTrajectory * -1;
            }
        }

        direction = Math.atan2(yTrajectory,xTrajectory);

        x += xSpeed * Math.cos(direction);
        y += ySpeed * Math.sin(direction);

    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setMoveToX(float moveToX) {
        this.moveToX = moveToX;
    }
    public void setMoveToY(float moveToY) {
        this.moveToY = moveToY;
    }

    public void setSpeedBoostActive(boolean speedBoostActive) {
        this.speedBoostActive = speedBoostActive;
    }

    public int getColor() {
        return color;
    }

    public void setInvincibe(boolean invincibe) {
        isInvincibe = invincibe;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setSuper(boolean aSuper) {
        isSuper = aSuper;
    }
}