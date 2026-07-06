package io.github.shogeo.phyjine.core.utils;

import io.github.shogeo.phyjine.core.Body;
import io.github.shogeo.phyjine.core.colliders.Collider;

public record CollisionManifold(Collider a, Collider b, Vector2D normal, double penetration, Vector2D contactPoint) {

    public Body bodyA() {
        return a.getOwner();
    }

    public Body bodyB() {
        return b.getOwner();
    }
}