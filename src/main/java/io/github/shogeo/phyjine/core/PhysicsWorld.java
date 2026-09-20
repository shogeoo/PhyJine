package io.github.shogeo.phyjine.core;

import io.github.shogeo.phyjine.core.colliders.Collider;
import io.github.shogeo.phyjine.core.utils.CollisionManifold;
import io.github.shogeo.phyjine.core.utils.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class PhysicsWorld {

    private double GRAVITY;

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

    public void removeBody(Body body) {
        bodies.remove(body);
    }

    public List<CollisionManifold> getCollisions() {
        return collisionDetector.getCollisions();
    }

    public void setGravity(double gravity) {
        this.GRAVITY = gravity;
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

//    private void applyGravity() {
//        // всемирное тяготение: каждая пара коллайдеров разных тел притягивается по закону F = G * m1 * m2 / r^2.
//        // Сила приложена в мировой точке коллайдера, поэтому тело из-за возникшего момента может вращаться.
//        for (int i = 0; i < bodies.size(); i++) {
//            Body bodyA = bodies.get(i);
//            for (int j = i + 1; j < bodies.size(); j++) {
//                Body bodyB = bodies.get(j);
//                for (Collider colliderA : bodyA.getColliders()) {
//                    Vector2D positionA = getColliderWorldPosition(bodyA, colliderA);
//                    for (Collider colliderB : bodyB.getColliders()) {
//                        Vector2D positionB = getColliderWorldPosition(bodyB, colliderB);
//
//                        Vector2D delta = positionB.subtract(positionA);
//                        double distanceSquared = delta.lengthSquared();
//                        if (distanceSquared == 0) {
//                            continue; // коллайдеры ровно в одной точке — пропускаем пару
//                        }
//
//                        double distance = Math.sqrt(distanceSquared);
//                        double forceMagnitude = GRAVITY * colliderA.getMass() * colliderB.getMass() / distanceSquared;
//                        Vector2D force = delta.multiply(forceMagnitude / distance);
//
//                        bodyA.applyForce(force, positionA);
//                        bodyB.applyForce(force.multiply(-1), positionB);
//                    }
//                }
//            }
//        }
//    }

    private Vector2D getColliderWorldPosition(Body body, Collider collider) {
        return body.getPosition().add(collider.getPosition().rotate(body.getAngle()));
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