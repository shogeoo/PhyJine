package io.github.shogeo.phyjine.core;

import io.github.shogeo.phyjine.core.utils.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class PhysicsWorld {

    private final double GRAVITY;

    private final CollisionDetector collisionDetector;
    private final CollisionSolver collisionSolver;

    private final List<Body> bodies = new ArrayList<>();

    private boolean isPaused = false;

    public PhysicsWorld(double GRAVITY) {
        this.GRAVITY = GRAVITY;
        this.collisionDetector = new CollisionDetector();
        this.collisionSolver = new CollisionSolver();
    }

    public void addBody(Body body) {
        bodies.add(body);
    }

    public List<Body> getBodies() {
        return bodies;
    }

    public void step(double dt) {
        if (isPaused) return;

        resetAccumulators();
        applyGravity();

        for (Body body : bodies) {
            body.updateAABB();
        }

        collisionDetector.detectCollisions(bodies);

        collisionSolver.solve(collisionDetector.getCollisions());

        for (Body body : bodies) {
            body.integrate(dt);
        }
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

    public void togglePause() {
        isPaused = !isPaused;
    }

    public boolean isPaused() {
        return isPaused;
    }
}