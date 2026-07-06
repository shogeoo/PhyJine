package io.github.shogeo.phyjine.core;

import io.github.shogeo.phyjine.core.colliders.CircleCollider;
import io.github.shogeo.phyjine.core.colliders.Collider;
import io.github.shogeo.phyjine.core.colliders.PolygonCollider;
import io.github.shogeo.phyjine.core.utils.AABB;
import io.github.shogeo.phyjine.core.utils.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class Body {
    private final Collider[] colliders;
    private final double mass;
    private final double momentOfInertia;
    private final double inverseMass;
    private final double inverseMomentOfInertia;
    private AABB aabb;
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

        this.updateAABB();
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

        if (inverseMass == 0) return;

        Vector2D acceleration = force.multiply(inverseMass);
        double angularAcceleration = torque * inverseMomentOfInertia;

        velocity = velocity.add(acceleration.multiply(dt));
        angularVelocity = angularVelocity + (angularAcceleration * dt);

        position = position.add(velocity.multiply(dt));
        angle += angularVelocity * dt;
    }

    public void applyForce(Vector2D force) {
        this.force = this.force.add(force);
    }

    public void applyForce(Vector2D force, Vector2D point) {
        this.force = this.force.add(force);
        Vector2D r = point.subtract(this.position);
        this.torque += r.cross(force);
    }
    public void applyTorque(double torque) {
        this.torque += torque;
    }

    void resetForce() {
        this.force = new Vector2D(0, 0);
    }
    void resetTorque() {
        this.torque = 0;
    }

    public static Body box(Vector2D position, double angle, double width, double height, Material material) {
        double w2 = width / 2;
        double h2 = height / 2;
        List<Vector2D> verts = new ArrayList<>();
        verts.add(new Vector2D(-w2, -h2));
        verts.add(new Vector2D(w2, -h2));
        verts.add(new Vector2D(w2, h2));
        verts.add(new Vector2D(-w2, h2));
        return new Body(position, angle, new PolygonCollider(new Vector2D(0, 0), 0, verts, material));
    }

    public static Body box(double x, double y, double angle, double width, double height, Material material) {
        return box(new Vector2D(x, y), angle, width, height, material);
    }

    public static Body circle(Vector2D position, double angle, double radius, Material material) {
        return new Body(position, angle, new CircleCollider(new Vector2D(0, 0), 0, radius, material));
    }

    public static Body circle(double x, double y, double angle, double radius, Material material) {
        return circle(new Vector2D(x, y), angle, radius, material);
    }

    public static Body polygon(Vector2D position, double angle, List<Vector2D> vertices, Material material) {
        return new Body(position, angle, new PolygonCollider(new Vector2D(0, 0), 0, vertices, material));
    }

    public static Body polygon(double x, double y, double angle, List<Vector2D> vertices, Material material) {
        return polygon(new Vector2D(x, y), angle, vertices, material);
    }

    public static Body regularPolygon(Vector2D position, double angle, int vertexCount, double radius, Material material) {
        List<Vector2D> verts = new ArrayList<>();
        for (int i = 0; i < vertexCount; i++) {
            double a = 2 * Math.PI * i / vertexCount;
            verts.add(new Vector2D(Math.cos(a) * radius, Math.sin(a) * radius));
        }
        return polygon(position, angle, verts, material);
    }

    public static Body regularPolygon(double x, double y, double angle, int vertexCount, double radius, Material material) {
        return regularPolygon(new Vector2D(x, y), angle, vertexCount, radius, material);
    }


    public double getMass() {
        return mass;
    }
    public double getInvMass() {
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

    public void setPosition(Vector2D newPos) {
        this.position = newPos;
    }
    public void setAngle(double angle) {
        this.angle = angle;
    }

    public Vector2D getVelocity() {
        return velocity;
    }
    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    public double getAngularVelocity() {
        return angularVelocity;
    }
    public void setAngularVelocity(double v) {
        this.angularVelocity = v;
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
        double minX = first.min().x();
        double minY = first.min().y();
        double maxX = first.max().x();
        double maxY = first.max().y();

        for (int i = 1; i < colliders.length; i++) {
            AABB cAabb = colliders[i].getAabb();
            minX = Math.min(minX, cAabb.min().x());
            minY = Math.min(minY, cAabb.min().y());
            maxX = Math.max(maxX, cAabb.max().x());
            maxY = Math.max(maxY, cAabb.max().y());
        }

        this.aabb = new AABB(new Vector2D(minX, minY), new Vector2D(maxX, maxY));
    }
}