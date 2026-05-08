package io.github.shogeo.phyjine.colliders;

import io.github.shogeo.phyjine.Material;
import io.github.shogeo.phyjine.utils.Vector2D;

public class CircleCollider extends Collider {

    Vector2D position;
    double angle;

    double radius;

    double mass;
    double inverseMass;

    final Material material;

    public CircleCollider(Vector2D position, double angle, double radius, Material material) {

        this.position = position;
        this.angle = angle;

        this.radius = radius;

        double mass = Math.PI * radius * radius * material.density;
        double inverseMass = 1 / mass;

        this.material = material;
    }
}
