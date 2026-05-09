package io.github.shogeo.phyjine.utils;

public record AABB(Vector2D min, Vector2D max) {

    public boolean intersects(AABB other) {
        if (this.max.getX() < other.min.getX() || this.min.getX() > other.max.getX()) {
            return false;
        }
        return !(this.max.getY() < other.min.getY()) && !(this.min.getY() > other.max.getY());
    }
}
