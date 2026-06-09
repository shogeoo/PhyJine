package io.github.shogeo.phyjine.core.colliders;

import io.github.shogeo.phyjine.core.Material;
import io.github.shogeo.phyjine.core.utils.AABB;
import io.github.shogeo.phyjine.core.utils.Vector2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PolygonCollider extends Collider {

    private final List<Vector2D> localVertices;

    public PolygonCollider(Vector2D position, double angle, List<Vector2D> vertices, Material material) {
        this.angle = angle;
        this.material = material;

        List<Vector2D> ccwVerts = makeCounterClockwise(vertices);

        double area = 0;
        double cx = 0;
        double cy = 0;
        int n = ccwVerts.size();
        for (int i = 0; i < n; i++) {
            Vector2D curr = ccwVerts.get(i);
            Vector2D next = ccwVerts.get((i + 1) % n);
            double cross = curr.x() * next.y() - next.x() * curr.y();
            area += cross;
            cx += (curr.x() + next.x()) * cross;
            cy += (curr.y() + next.y()) * cross;
        }
        area = 0.5 * Math.abs(area);

        Vector2D centroid = new Vector2D(0, 0);
        if (area > 0) {
            centroid = new Vector2D(cx / (6 * area), cy / (6 * area));
        }

        List<Vector2D> centeredVerts = new ArrayList<>();
        for (Vector2D v : ccwVerts) {
            centeredVerts.add(v.subtract(centroid));
        }
        this.localVertices = centeredVerts;

        this.position = position.add(centroid);

        this.mass = area * material.density();
        this.inverseMass = this.mass > 0 ? 1.0 / this.mass : 0;

        double numerator = 0;
        double denominator = 0;
        for (int i = 0; i < n; i++) {
            Vector2D curr = this.localVertices.get(i);
            Vector2D next = this.localVertices.get((i + 1) % n);
            double cross = curr.x() * next.y() - next.x() * curr.y();
            double factor = curr.x() * curr.x() + curr.x() * next.x() + next.x() * next.x() + curr.y() * curr.y() + curr.y() * next.y() + next.y() * next.y();
            numerator += cross * factor;
            denominator += cross;
        }
        if (denominator != 0) {
            this.momentOfInertia = this.mass * (numerator / (6 * denominator));
        } else {
            this.momentOfInertia = 0;
        }
    }

    private static List<Vector2D> makeCounterClockwise(List<Vector2D> verts) {
        double area = 0;
        int n = verts.size();
        for (int i = 0; i < n; i++) {
            Vector2D curr = verts.get(i);
            Vector2D next = verts.get((i + 1) % n);
            area += curr.x() * next.y() - next.x() * curr.y();
        }
        if (area < 0) {
            List<Vector2D> reversed = new ArrayList<>(verts);
            Collections.reverse(reversed);
            return reversed;
        }
        return verts;
    }

    public List<Vector2D> getLocalVertices() {
        return localVertices;
    }

    public List<Vector2D> getWorldVertices() {
        if (owner == null) {
            List<Vector2D> worldVerts = new ArrayList<>();
            for (Vector2D v : localVertices) {
                worldVerts.add(position.add(v.rotate(angle)));
            }
            return worldVerts;
        }
        Vector2D bodyPosition = owner.getPosition();
        double bodyAngle = owner.getAngle();
        Vector2D rotatedOffset = this.position.rotate(bodyAngle);
        Vector2D globalPosition = bodyPosition.add(rotatedOffset);
        double globalAngle = bodyAngle + this.angle;

        List<Vector2D> worldVerts = new ArrayList<>();
        for (Vector2D v : localVertices) {
            worldVerts.add(globalPosition.add(v.rotate(globalAngle)));
        }
        return worldVerts;
    }

    @Override
    public void updateAabb(Vector2D bodyPosition, double bodyAngle) {
        Vector2D rotatedOffset = this.position.rotate(bodyAngle);
        Vector2D globalPosition = bodyPosition.add(rotatedOffset);
        double globalAngle = bodyAngle + this.angle;

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (Vector2D v : localVertices) {
            Vector2D worldV = globalPosition.add(v.rotate(globalAngle));
            if (worldV.x() < minX) minX = worldV.x();
            if (worldV.y() < minY) minY = worldV.y();
            if (worldV.x() > maxX) maxX = worldV.x();
            if (worldV.y() > maxY) maxY = worldV.y();
        }

        this.aabb = new AABB(new Vector2D(minX, minY), new Vector2D(maxX, maxY));
    }

    @Override
    public int getType() {
        return 2;
    }
}