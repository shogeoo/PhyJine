package io.github.shogeo.phyjine.core.colliders;

import io.github.shogeo.phyjine.core.Body;
import io.github.shogeo.phyjine.core.Material;
import io.github.shogeo.phyjine.core.utils.AABB;
import io.github.shogeo.phyjine.core.utils.Vector2D;

public abstract class Collider {

    protected Body owner;

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

    public void setPosition(Vector2D position) {
        this.position = position;
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

    public abstract void updateAabb(Vector2D bodyPosition, double bodyAngle);

    public Body getOwner() {
        return owner;
    }

    public void setOwner(Body owner) {
        this.owner = owner;
    }

    public int getType() {
        return 0;
    }
}
