package com.anysoftkeyboard.gesturetyping;

public class Point {
    public float x;
    public float y;
    public float weight = 1.0f;
    public float pathDistanceSoFar = -1;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
