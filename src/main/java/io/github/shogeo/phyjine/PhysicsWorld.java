package io.github.shogeo.phyjine;

import io.github.shogeo.phyjine.utils.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class PhysicsWorld {

    private final double gravity;
    private final CollisionDetector collisionDetector;

    private final List<Body> bodies = new ArrayList<>();

    public PhysicsWorld(double gravity) {
        this.gravity = gravity;
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
            body.applyForce(new Vector2D(0, gravity * body.getMass()));
        }
    }

    private void resetAccumulators() {
        for (Body body : bodies) {
            body.resetForce();
            body.resetTorque();
        }
    }
}
