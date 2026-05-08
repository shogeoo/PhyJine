package io.github.shogeo.phyjine.colliders;

import io.github.shogeo.phyjine.Material;
import io.github.shogeo.phyjine.utils.Vector2D;

public class CircleCollider extends Collider {

    public double radius;

    public CircleCollider(Vector2D position, double angle, double radius, Material material) {
        this.position = position;
        this.angle = angle;
        this.radius = radius;
        this.material = material;

        this.mass = Math.PI * radius * radius * material.density;

        this.inverseMass = 1 / mass;

        this.momentOfInertia = 0.5 * this.mass * radius * radius;
    }
}