package io.github.shogeo.phyjine.colliders;

import io.github.shogeo.phyjine.Body;
import io.github.shogeo.phyjine.utils.Vector2D;

public class CircleCollider extends Collider {

    Vector2D position;
    double angle;

    double radius;

    double density;

    double mass;
    double inverseMass;

    double staticFriction, kineticFriction, rollingFriction;
    double restitution;

    public CircleCollider(Vector2D position, double angle, double radius, double density, double staticFriction, double kineticFriction, double rollingFriction, double restitution) {

        this.position = position;
        this.angle = angle;

        this.radius = radius;
        this.density = density;

        double mass = Math.PI * radius * radius;
        double inverseMass = 1 / mass;

        this.staticFriction = staticFriction;
        this.kineticFriction = kineticFriction;
        this.rollingFriction = rollingFriction;
        this.restitution = restitution;
    }
}
