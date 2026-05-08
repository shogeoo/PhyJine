package io.github.shogeo.phyjine;

import io.github.shogeo.phyjine.colliders.Collider;
import io.github.shogeo.phyjine.utils.Vector2D;

public class Body {
    Collider[] colliders;

    Vector2D position;
    double angle;

    Vector2D velocity;
    double angularVelocity;

    Vector2D force;
    double torque;

    double mass, momentOfInertia;
    double inverseMass, inverseMomentOfInertia;

    public Body(Vector2D position, double angle, Collider... colliders) {
        this.position = position;
        this.angle = angle;
        this.colliders = colliders;
    }
}
