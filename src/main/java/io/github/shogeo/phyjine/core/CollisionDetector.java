package io.github.shogeo.phyjine.core;

import io.github.shogeo.phyjine.core.colliders.CircleCollider;
import io.github.shogeo.phyjine.core.colliders.Collider;
import io.github.shogeo.phyjine.core.colliders.PolygonCollider;
import io.github.shogeo.phyjine.core.utils.CollisionManifold;
import io.github.shogeo.phyjine.core.utils.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class CollisionDetector {

    List<CollisionManifold> collisions = new ArrayList<>();

    private static double[] getProjection(List<Vector2D> vertices, Vector2D axis) {
        double min = vertices.get(0).dot(axis);
        double max = min;
        for (int i = 1; i < vertices.size(); i++) {
            double p = vertices.get(i).dot(axis);
            if (p < min) min = p;
            if (p > max) max = p;
        }
        return new double[]{min, max};
    }

    private static boolean isPointInsidePolygon(Vector2D p, List<Vector2D> vertices) {
        int n = vertices.size();
        for (int i = 0; i < n; i++) {
            Vector2D v1 = vertices.get(i);
            Vector2D v2 = vertices.get((i + 1) % n);
            Vector2D edge = v2.subtract(v1);
            Vector2D toPoint = p.subtract(v1);
            if (edge.cross(toPoint) < -1e-6) {
                return false;
            }
        }
        return true;
    }

    private static Vector2D getSupportPoint(List<Vector2D> vertices, Vector2D direction) {
        double maxDot = -Double.MAX_VALUE;
        Vector2D support = null;
        for (Vector2D v : vertices) {
            double dot = v.dot(direction);
            if (dot > maxDot) {
                maxDot = dot;
                support = v;
            }
        }
        return support;
    }

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
        } else if (a.getType() == 1 && b.getType() == 2) {
            CollisionManifold m = detectCircleVsPolygon((CircleCollider) a, (PolygonCollider) b, false);
            if (m != null) collisions.add(m);
        } else if (a.getType() == 2 && b.getType() == 1) {
            CollisionManifold m = detectCircleVsPolygon((CircleCollider) b, (PolygonCollider) a, true);
            if (m != null) collisions.add(m);
        } else if (a.getType() == 2 && b.getType() == 2) {
            CollisionManifold m = detectPolygonVsPolygon((PolygonCollider) a, (PolygonCollider) b);
            if (m != null) collisions.add(m);
        }
    }

    private CollisionManifold detectCircleVsPolygon(CircleCollider circle, PolygonCollider poly, boolean flip) {
        Vector2D circleCenter = circle.getOwner().getPosition().add(circle.getPosition().rotate(circle.getOwner().getAngle()));
        double radius = circle.getRadius();
        List<Vector2D> verts = poly.getWorldVertices();

        Vector2D closestVertex = null;
        double minDistSq = Double.MAX_VALUE;
        for (Vector2D v : verts) {
            double distSq = v.subtract(circleCenter).lengthSquared();
            if (distSq < minDistSq) {
                minDistSq = distSq;
                closestVertex = v;
            }
        }

        List<Vector2D> axes = new ArrayList<>();
        int n = verts.size();
        for (int i = 0; i < n; i++) {
            Vector2D v1 = verts.get(i);
            Vector2D v2 = verts.get((i + 1) % n);
            Vector2D edge = v2.subtract(v1);
            Vector2D normal = new Vector2D(edge.y(), -edge.x()).normalized();
            axes.add(normal);
        }
        if (closestVertex != null) {
            Vector2D dir = closestVertex.subtract(circleCenter);
            if (dir.lengthSquared() > 1e-8) {
                axes.add(dir.normalized());
            }
        }

        double minOverlap = Double.MAX_VALUE;
        Vector2D collisionNormal = null;

        for (Vector2D axis : axes) {
            double[] projPoly = getProjection(verts, axis);
            double circleProj = circleCenter.dot(axis);
            double minCircle = circleProj - radius;
            double maxCircle = circleProj + radius;

            double overlap = Math.min(projPoly[1], maxCircle) - Math.max(projPoly[0], minCircle);
            if (overlap <= 0) {
                return null;
            }

            if (overlap < minOverlap) {
                minOverlap = overlap;
                collisionNormal = axis;
            }
        }

        if (collisionNormal == null) return null;

        Vector2D polyCenter = verts.stream().reduce(new Vector2D(0, 0), Vector2D::add).multiply(1.0 / verts.size());
        Vector2D dir = polyCenter.subtract(circleCenter);
        if (dir.dot(collisionNormal) < 0) {
            collisionNormal = collisionNormal.multiply(-1);
        }

        Vector2D finalNormal = flip ? collisionNormal.multiply(-1) : collisionNormal;
        Vector2D contactPoint = circleCenter.add(collisionNormal.multiply(radius));

        if (!flip) {
            return new CollisionManifold(circle, poly, finalNormal, minOverlap, contactPoint);
        } else {
            return new CollisionManifold(poly, circle, finalNormal, minOverlap, contactPoint);
        }
    }

    private CollisionManifold detectPolygonVsPolygon(PolygonCollider polyA, PolygonCollider polyB) {
        List<Vector2D> vertsA = polyA.getWorldVertices();
        List<Vector2D> vertsB = polyB.getWorldVertices();

        List<Vector2D> axes = new ArrayList<>();
        int nA = vertsA.size();
        for (int i = 0; i < nA; i++) {
            Vector2D v1 = vertsA.get(i);
            Vector2D v2 = vertsA.get((i + 1) % nA);
            Vector2D edge = v2.subtract(v1);
            axes.add(new Vector2D(edge.y(), -edge.x()).normalized());
        }
        int nB = vertsB.size();
        for (int i = 0; i < nB; i++) {
            Vector2D v1 = vertsB.get(i);
            Vector2D v2 = vertsB.get((i + 1) % nB);
            Vector2D edge = v2.subtract(v1);
            axes.add(new Vector2D(edge.y(), -edge.x()).normalized());
        }

        double minOverlap = Double.MAX_VALUE;
        Vector2D collisionNormal = null;

        for (Vector2D axis : axes) {
            double[] projA = getProjection(vertsA, axis);
            double[] projB = getProjection(vertsB, axis);

            double overlap = Math.min(projA[1], projB[1]) - Math.max(projA[0], projB[0]);
            if (overlap <= 0) {
                return null;
            }

            if (overlap < minOverlap) {
                minOverlap = overlap;
                collisionNormal = axis;
            }
        }

        if (collisionNormal == null) return null;

        Vector2D centerA = vertsA.stream().reduce(new Vector2D(0, 0), Vector2D::add).multiply(1.0 / nA);
        Vector2D centerB = vertsB.stream().reduce(new Vector2D(0, 0), Vector2D::add).multiply(1.0 / nB);
        Vector2D dir = centerB.subtract(centerA);
        if (dir.dot(collisionNormal) < 0) {
            collisionNormal = collisionNormal.multiply(-1);
        }

        List<Vector2D> contactPoints = new ArrayList<>();
        for (Vector2D v : vertsA) {
            if (isPointInsidePolygon(v, vertsB)) {
                contactPoints.add(v);
            }
        }
        for (Vector2D v : vertsB) {
            if (isPointInsidePolygon(v, vertsA)) {
                contactPoints.add(v);
            }
        }

        Vector2D contactPoint;
        if (!contactPoints.isEmpty()) {
            double sumX = 0, sumY = 0;
            for (Vector2D cp : contactPoints) {
                sumX += cp.x();
                sumY += cp.y();
            }
            contactPoint = new Vector2D(sumX / contactPoints.size(), sumY / contactPoints.size());
        } else {
            Vector2D supportA = getSupportPoint(vertsA, collisionNormal);
            Vector2D supportB = getSupportPoint(vertsB, collisionNormal.multiply(-1));
            contactPoint = supportA.add(supportB).multiply(0.5);
        }

        return new CollisionManifold(polyA, polyB, collisionNormal, minOverlap, contactPoint);
    }

    public List<CollisionManifold> getCollisions() {
        return collisions;
    }
}
