package io.github.shogeo.phyjine.colliders;

import io.github.shogeo.phyjine.Body;
import io.github.shogeo.phyjine.Material;
import io.github.shogeo.phyjine.utils.AABB;
import io.github.shogeo.phyjine.utils.Vector2D;

public class CircleCollider extends Collider {

    private final double radius;

    public CircleCollider(Body owner, Vector2D position, double angle, double radius, Material material) {
        this.owner = owner;
        this.position = position;
        this.angle = angle;
        this.radius = radius;
        this.material = material;

        this.mass = Math.PI * radius * radius * material.getDensity();

        this.inverseMass = 1 / mass;

        this.momentOfInertia = 0.5 * this.mass * radius * radius;
    }

    public double getRadius() {
        return radius;
    }
    public void updateAabb(Vector2D bodyPosition, double bodyAngle) {
        Vector2D rotatedOffset = this.position.rotate(bodyAngle);

        Vector2D globalPosition = bodyPosition.add(rotatedOffset);

        Vector2D min = new Vector2D(globalPosition.getX() - radius, globalPosition.getY() - radius);
        Vector2D max = new Vector2D(globalPosition.getX() + radius, globalPosition.getY() + radius);

        this.aabb = new AABB(min, max);
    }
}
