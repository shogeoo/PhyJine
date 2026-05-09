package io.github.shogeo.phyjine;

import io.github.shogeo.phyjine.colliders.Collider;
import io.github.shogeo.phyjine.utils.Vector2D;

public class Body {
    private final Collider[] colliders;

    private final Vector2D localCenterOfMass;

    private Vector2D position;
    private double angle;

    private final double mass;
    private final double momentOfInertia;

    private final double inverseMass;
    private final double inverseMomentOfInertia;

    private Vector2D velocity;
    private double angularVelocity;

    private double angularAcceleration;

    private Vector2D force;
    private double torque;

    public Body(Vector2D position, double angle, Collider... colliders) {
        this.position = position;
        this.angle = angle;
        this.colliders = colliders;

        this.mass = calculateMass();
        this.inverseMass = 1 / mass;

        this.localCenterOfMass = calculateLocalCenterOfMass();
        this.momentOfInertia = calculateMomentOfInertia();
        this.inverseMomentOfInertia = 1 / momentOfInertia;

        this.velocity = new Vector2D(0, 0);
        this.force = new Vector2D(0, 0);
        this.angularVelocity = 0;
        this.torque = 0;
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

    void integrate(double dt) {
        Vector2D acceleration = force.multiply(inverseMass);
        double angularAcceleration = torque * inverseMomentOfInertia;

        position = (position.add(velocity.multiply(dt))).add(acceleration.multiply(0.5 * dt * dt));
        angle = angle + (angularVelocity * dt) + (0.5 * angularAcceleration * dt * dt);

        velocity = velocity.add(acceleration.multiply(dt));
        angularVelocity = angularVelocity + (angularAcceleration * dt);
    }

    void applyForce(Vector2D force) {
        this.force = this.force.add(force);
    }
    void applyTorque(double torque) {
        this.torque += torque;
    }

    void resetForce() {
        this.force = new Vector2D(0, 0);
    }
    void resetTorque() {
        this.torque = 0;
    }


    double getMass() {
        return mass;
    }
    double getMomentOfInertia() {
        return momentOfInertia;
    }

    public double getInverseMass() {
        return inverseMass;
    }
    public double getInverseMomentOfInertia() {
        return inverseMomentOfInertia;
    }

    public Vector2D getPosition() {
        return position;
    }
    public double getAngle() {
        return angle;
    }

    public Vector2D getVelocity() {
        return velocity;
    }
    public double getAngularVelocity() {
        return angularVelocity;
    }
}
