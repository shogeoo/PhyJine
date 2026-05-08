package io.github.shogeo.phyjine;

public class Material {
    public final double density;

    public final double staticFriction, kineticFriction, rollingFriction;
    public final double restitution;

    public Material(double density, double staticFriction, double kineticFriction, double rollingFriction, double restitution) {
        this.density = density;
        this.staticFriction = staticFriction;
        this.kineticFriction = kineticFriction;
        this.rollingFriction = rollingFriction;
        this.restitution = restitution;
    }
}
