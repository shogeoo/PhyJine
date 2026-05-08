package io.github.shogeo.phyjine;

import io.github.shogeo.phyjine.colliders.Collider;
import io.github.shogeo.phyjine.utils.Vector2D;

public class Body {
    private final Collider[] colliders;

    private Vector2D position;
    private double angle;

    private final Vector2D localCenterOfMass;

    private Vector2D velocity;
    private double angularVelocity;

    private Vector2D force;
    private double torque;

    private final double mass;
    private final double momentOfInertia;
    private final double inverseMass;
    private final double inverseMomentOfInertia;

    public Body(Vector2D position, double angle, Collider... colliders) {
        this.position = position;
        this.angle = angle;
        this.colliders = colliders;

        this.mass = calculateMass();
        this.inverseMass = 1 / mass;

        this.localCenterOfMass = calculateLocalCenterOfMass();
        this.momentOfInertia = calculateMomentOfInertia();
        this.inverseMomentOfInertia = 1 / momentOfInertia;
    }

    private double calculateMass() {
        double totalMass = 0;
        for (Collider c : colliders) {
            totalMass += c.getMass();
        }
        return totalMass;
    }

    private Vector2D calculateLocalCenterOfMass() {
        double xCOM = 0, yCOM = 0;
        for (Collider c : colliders) {
            xCOM += c.getPosition().getX() * c.getMass();
            yCOM += c.getPosition().getY() * c.getMass();
        }
        return new Vector2D(xCOM / mass, yCOM / mass);
    }

    private double calculateMomentOfInertia() {
        double totalMomentOfInertia = 0;
        for (Collider c : colliders) {
            Vector2D positionOfColliderFromCOM = c.getPosition().subtract(localCenterOfMass);
            double distance2 = positionOfColliderFromCOM.lengthSquared();
            totalMomentOfInertia += c.getMomentOfInertia() + c.getMass() * distance2;
        }
        return totalMomentOfInertia;
    }
}
