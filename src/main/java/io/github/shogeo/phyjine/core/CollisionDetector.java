package io.github.shogeo.phyjine.core;

import io.github.shogeo.phyjine.core.colliders.CircleCollider;
import io.github.shogeo.phyjine.core.colliders.Collider;
import io.github.shogeo.phyjine.core.utils.CollisionManifold;
import io.github.shogeo.phyjine.core.utils.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class CollisionDetector {

    List<CollisionManifold> collisions = new ArrayList<>();

    public void detectCollisions(List<Body> bodies) {

        collisions.clear();

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
        Collider[] b1 = bodyA.getColliders();
        Collider[] b2 = bodyB.getColliders();

        for (Collider c1 : b1) {
            for (Collider c2 : b2) {
                if (c1.getAabb().intersects(c2.getAabb())) {
                    narrowPhase(c1, c2);
                }
            }
        }
    }

    private void narrowPhase(Collider a, Collider b) {
        if (a.getType() == 1 && b.getType() == 1) {
            CircleCollider c1 = (CircleCollider) a;
            CircleCollider c2 = (CircleCollider) b;

            Vector2D position1 = c1.getOwner().getPosition().add(c1.getPosition().rotate(c1.getOwner().getAngle()));
            Vector2D position2 = c2.getOwner().getPosition().add(c2.getPosition().rotate(c2.getOwner().getAngle()));

            Vector2D delta = position2.subtract(position1);
            double distance = delta.length();

            if (distance > c1.getRadius() + c2.getRadius()) {
                return;
            }

            Vector2D normal = delta.normalized();
            double penetration = (c1.getRadius() + c2.getRadius()) - distance;

            Vector2D contactPoint = position1.add(normal.multiply(c1.getRadius()));

            CollisionManifold m = new CollisionManifold(c1, c2, normal, penetration, contactPoint);

            collisions.add(m);
        }
    }

    public List<CollisionManifold> getCollisions() {
        return collisions;
    }
}
