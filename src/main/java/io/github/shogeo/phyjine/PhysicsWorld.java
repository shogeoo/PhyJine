package io.github.shogeo.phyjine;

import io.github.shogeo.phyjine.utils.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class PhysicsWorld {

    private final double GRAVITY;
    private final CollisionDetector collisionDetector;

    private final List<Body> bodies = new ArrayList<>();

    public PhysicsWorld(double GRAVITY) {
        this.GRAVITY = GRAVITY;
        this.collisionDetector = new CollisionDetector();
    }

    public void addBody(Body body) {
        bodies.add(body);
    }

    public List<Body> getBodies() {
        return bodies;
    }

    public void step(double dt) {
        resetAccumulators();
        applyGravity();

        for (Body body : bodies) {
            body.integrate(dt);
        }

        for (Body body : bodies) {
            body.updateAABB();
        }

        collisionDetector.detectCollisions(bodies);
    }

    private void applyGravity() {
        for (Body body : bodies) {
            body.applyForce(new Vector2D(0, GRAVITY * body.getMass()));
        }
    }

    private void resetAccumulators() {
        for (Body body : bodies) {
            body.resetForce();
            body.resetTorque();
        }
    }
}
