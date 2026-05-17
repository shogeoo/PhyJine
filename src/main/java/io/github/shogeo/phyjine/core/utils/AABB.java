package io.github.shogeo.phyjine.core.utils;

public record AABB(Vector2D min, Vector2D max) {

    public boolean intersects(AABB other) {
        return this.min.x() <= other.max.x() && this.max.x() >= other.min.x() && this.min.y() <= other.max.y() && this.max.y() >= other.min.y();
    }
}
