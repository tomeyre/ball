package com.eyresgames.eyresapps.bombball;

import android.content.Context;
import android.graphics.Color;

/**
 * Created by thomaseyre on 25/08/2017.
 */

public class Ball {
    private float x;
    private float y;
    private float speed = 0;

    private int MAX_SPEED = 25;
    private int MAX_BOOST_SPEED;
    private int MAX_SUPER_SPEED;

    boolean moveRight = false;
    boolean moveLeft = false;

    double rotation = 0;

    int color;

    int radius;

    boolean isBomb;
    boolean isClose = false;

    long previousTime = 0;
    long countdown = 0;

    boolean isSpeedBoost = false;
    boolean isSuperSpeedBoost = false;

    int value;
    int screenY;

    double xDirection;
    double yDirection;


    public Ball(Context context,int screenY, int x, int y, int color, double rotation, boolean isBomb, boolean isSpeedBoost, boolean isSuperSpeedBoost, int value, int radius, int additionalSpeed) {
        speed = MAX_SPEED;

        this.x = x;
        this.y = y;
        this.color = color;
        this.rotation = rotation;
        this.isBomb = isBomb;
        this.isSpeedBoost = isSpeedBoost;
        this.screenY = screenY;
        if(value == 0){
            this.value = 10;
        }else{
            this.value = value;
        }
        this.radius = radius;
        this.isSuperSpeedBoost = isSuperSpeedBoost;
        MAX_SPEED = screenY / 75;
        this.MAX_SPEED += additionalSpeed;
        MAX_BOOST_SPEED = MAX_SPEED + screenY / 200;
        MAX_SUPER_SPEED = MAX_SPEED + screenY / 65;

        previousTime = System.currentTimeMillis();
        countdown = System.currentTimeMillis();
    }


    public void update(double rotateWorld) {
        if(isSpeedBoost){
            speed = MAX_BOOST_SPEED;
        }
        else if(isSuperSpeedBoost){
            speed = MAX_SUPER_SPEED;
        }
        else{
            speed = MAX_SPEED;
        }
        if(isBomb){
            if(System.currentTimeMillis() - countdown >= 800 && System.currentTimeMillis() - countdown <= 7000){
                radius += screenY / 380;
            }else if(System.currentTimeMillis() - countdown > 7000){
                radius = 0;
            }
            if(isClose) {
                if(System.currentTimeMillis() - previousTime > 1000 / 20) {
                    if(color == Color.RED) {
                        color = Color.DKGRAY;
                    }
                    else{
                        color = Color.RED;
                    }
                    previousTime = System.currentTimeMillis();
                }
            }
            else{
                if(System.currentTimeMillis() - previousTime > 1000 / 5) {
                    if(color == Color.DKGRAY) {
                        color = Color.GRAY;
                    }
                    else{
                        color = Color.DKGRAY;
                    }
                    previousTime = System.currentTimeMillis();
                }
            }
        }

        if(rotateWorld != 0){
            double checker = Math.toDegrees(rotateWorld);
            rotateWorld = Math.toRadians(rotateWorld);

            xDirection = Math.sin(rotateWorld) * speed;
            yDirection = Math.cos(rotateWorld) * speed;

            x += xDirection;
            y += yDirection;

        }else {

            y += speed;
        }

    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public double getRotation() {
        return rotation;
    }

    public void setMoveRight(boolean moveRight) {
        this.moveRight = moveRight;
    }

    public void setMoveLeft(boolean moveLeft) {
        this.moveLeft = moveLeft;
    }

    public int getColor() {
        return color;
    }

    public boolean isBomb() {
        return isBomb;
    }

    public void setClose(boolean close) {
        isClose = close;
    }

    public void setSpeedBoost(boolean speedBoost) {
        isSpeedBoost = speedBoost;
    }

    public int getRadius() {
        return radius;
    }

    public int getValue() {
        return value;
    }

    public void setSuperSpeedBoost(boolean superSpeedBoost) {
        isSuperSpeedBoost = superSpeedBoost;
    }

    public void setMAX_SPEED(int MAX_SPEED) {
        this.MAX_SPEED = MAX_SPEED;
    }

    public double getxDirection() {
        return xDirection;
    }

    public void setxDirection(double xDirection) {
        this.xDirection = xDirection;
    }

    public double getyDirection() {
        return yDirection;
    }

    public void setyDirection(double yDirection) {
        this.yDirection = yDirection;
    }
}