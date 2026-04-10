package io.github.shogeo.phyjine.core;

public class Transform {
    public Vector2 position = new Vector2(0, 0);
    public Rotation rotation = new Rotation();

    public Transform(Vector2 position, Rotation rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public static class Rotation {
        public double angle = 0;
    }
}