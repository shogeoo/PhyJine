package io.github.shogeo.phyjine;

import io.github.shogeo.phyjine.colliders.Collider;
import io.github.shogeo.phyjine.utils.Vector2D;

public class Body {
    Collider[] colliders;

    Vector2D position;
    double angle;

    Vector2D localCenterOfMass;

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

        for (int i = 0; i < colliders.length; i++) {
            Collider c = colliders[i];
            mass += c.mass;
        }
        inverseMass = 1 / mass;

        double xCOM = 0, yCOM = 0;

        for (int i = 0; i < colliders.length; i++) {
            Collider c = colliders[i];
            xCOM += c.position.x * c.mass;
            yCOM += c.position.y * c.mass;
        }
        xCOM /= mass;
        yCOM /= mass;

        localCenterOfMass = new Vector2D(xCOM, yCOM);

        for (int i = 0; i < colliders.length; i++) {
            Collider c = colliders[i];
            Vector2D positionOfColliderFromCOM = c.position.subtract(localCenterOfMass);
            double distance2 = positionOfColliderFromCOM.lengthSquared();
            double oneCollider = c.momentOfInertia + c.mass * distance2;
            momentOfInertia += oneCollider;
        }

        inverseMomentOfInertia = 1 / momentOfInertia;
    }
}
