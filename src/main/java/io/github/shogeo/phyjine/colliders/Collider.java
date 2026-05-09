package io.github.shogeo.phyjine.colliders;

import io.github.shogeo.phyjine.Material;
import io.github.shogeo.phyjine.utils.AABB;
import io.github.shogeo.phyjine.utils.Vector2D;

public abstract class Collider {
    protected Vector2D position;
    protected double angle;

    protected double mass;
    protected double momentOfInertia;

    protected double inverseMass;

    protected Material material;

    protected AABB aabb;

    public Vector2D getPosition() {
        return position;
    }

    public double getAngle() {
        return angle;
    }

    public double getMass() {
        return mass;
    }

    public double getMomentOfInertia() {
        return momentOfInertia;
    }

    public double getInverseMass() {
        return inverseMass;
    }

    public Material getMaterial() {
        return material;
    }

    public AABB getAabb() {
        return aabb;
    }
}
