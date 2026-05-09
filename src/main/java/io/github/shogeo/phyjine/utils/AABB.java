package io.github.shogeo.phyjine.utils;

public class AABB {
    public Vector2D min;
    public Vector2D max;

    public AABB(Vector2D min, Vector2D max) {
        this.min = min;
        this.max = max;
    }

    public Vector2D getMin() {
        return min;
    }

    public Vector2D getMax() {
        return max;
    }

    public boolean intersects(AABB other) {
        if (this.max.x() < other.min.x() || this.min.x() > other.max.x()) {
            return false;
        }
        return !(this.max.y() < other.min.y()) && !(this.min.y() > other.max.y());
    }
}
