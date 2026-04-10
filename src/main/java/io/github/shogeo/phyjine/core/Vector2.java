package io.github.shogeo.phyjine.core;

public class Vector2 {
    public double x;
    public double y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2 relative(Vector2 B) {
        return new Vector2(B.x - this.x, B.y - this.y);
    }
}
