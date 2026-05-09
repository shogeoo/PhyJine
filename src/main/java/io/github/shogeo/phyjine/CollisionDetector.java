package io.github.shogeo.phyjine;

import io.github.shogeo.phyjine.colliders.Collider;

import java.util.List;

public class CollisionDetector {

    public void detectCollisions(List<Body> bodies) {
        for (int i = 0; i < bodies.size(); i++) {
            for (int j = i + 1; j < bodies.size(); j++) {
                Body bodyA = bodies.get(i);
                Body bodyB = bodies.get(j);

                if (bodyA.getAabb().intersects(bodyB.getAabb())) {
                    findCollisionsBetweenColliders(bodyA, bodyB);
                }
            }
        }
    }

    private void findCollisionsBetweenColliders(Body bodyA, Body bodyB) {
        Collider[] collidersA = bodyA.getColliders();
        Collider[] collidersB = bodyB.getColliders();

        for (Collider colliderA : collidersA) {
            for (Collider colliderB : collidersB) {
                if (colliderA.getAabb().intersects(colliderB.getAabb())) {
                    // TODO: Handle collision
                    IO.println("Зафиксировано пересечение коллайдеров");
                }
            }
        }
    }
}
