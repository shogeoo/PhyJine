package io.github.shogeo.phyjine.colliders;

import io.github.shogeo.phyjine.Material;
import io.github.shogeo.phyjine.utils.Vector2D;

public abstract class Collider {
    public Vector2D position;
    public double angle;

    public double mass;
    public double momentOfInertia;

    public double inverseMass;

    public Material material;
}
