package io.github.shogeo.phyjine;

import io.github.shogeo.phyjine.colliders.Collider;
import io.github.shogeo.phyjine.utils.AABB;
import io.github.shogeo.phyjine.utils.Vector2D;

public class Body {
    private final Collider[] colliders;
    private AABB aabb;

    private final double mass;
    private final double momentOfInertia;
    private final double inverseMass;
    private final double inverseMomentOfInertia;
    private Vector2D position;
    private double angle;
    private Vector2D velocity;
    private double angularVelocity;

    private Vector2D force;
    private double torque;

    public Body(Vector2D position, double angle, Collider... colliders) {
        this.position = position;
        this.angle = angle;
        this.colliders = colliders;

        for (Collider c : colliders) {
            c.setOwner(this);
        }

        this.aabb = new AABB(position, position);

        this.mass = calculateMass();
        this.inverseMass = this.mass > 0 ? 1 / mass : 0;

        Vector2D localCenterOfMass = calculateLocalCenterOfMass();

        this.position = this.position.add(localCenterOfMass.rotate(this.angle));

        for (Collider c : colliders) {
            c.setPosition(c.getPosition().subtract(localCenterOfMass));
        }

        this.momentOfInertia = calculateMomentOfInertia();
        this.inverseMomentOfInertia = this.momentOfInertia > 0 ? 1 / momentOfInertia : 0;

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
        if (mass == 0) {
            return new Vector2D(0, 0);
        }
        double xCOM = 0, yCOM = 0;
        for (Collider c : colliders) {
            xCOM += c.getPosition().x() * c.getMass();
            yCOM += c.getPosition().y() * c.getMass();
        }
        return new Vector2D(xCOM / mass, yCOM / mass);
    }

    private double calculateMomentOfInertia() {
        double totalMomentOfInertia = 0;
        for (Collider c : colliders) {
            double distance2 = c.getPosition().lengthSquared();
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

    public AABB getAabb() {
        return aabb;
    }

    public Collider[] getColliders() {
        return colliders;
    }

    public void updateAABB() {
        if (colliders.length == 0) return;

        for (Collider c : colliders) {
            c.updateAabb(this.position, this.angle);
        }

        AABB first = colliders[0].getAabb();
        double minX = first.getMin().getX();
        double minY = first.getMin().getY();
        double maxX = first.getMax().getX();
        double maxY = first.getMax().getY();

        for (int i = 1; i < colliders.length; i++) {
            AABB cAabb = colliders[i].getAabb();
            minX = Math.min(minX, cAabb.getMin().getX());
            minY = Math.min(minY, cAabb.getMin().getY());
            maxX = Math.max(maxX, cAabb.getMax().getX());
            maxY = Math.max(maxY, cAabb.getMax().getY());
        }

        this.aabb = new AABB(new Vector2D(minX, minY), new Vector2D(maxX, maxY));
    }
}
