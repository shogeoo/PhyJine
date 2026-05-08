package io.github.shogeo.phyjine;

public class Material {
    private final double density;
    private final double staticFriction;
    private final double kineticFriction;
    private final double rollingFriction;
    private final double restitution;

    public Material(double density, double staticFriction, double kineticFriction, double rollingFriction, double restitution) {
        this.density = density;
        this.staticFriction = staticFriction;
        this.kineticFriction = kineticFriction;
        this.rollingFriction = rollingFriction;
        this.restitution = restitution;
    }

    public double getDensity() {
        return density;
    }

    public double getStaticFriction() {
        return staticFriction;
    }

    public double getKineticFriction() {
        return kineticFriction;
    }

    public double getRollingFriction() {
        return rollingFriction;
    }

    public double getRestitution() {
        return restitution;
    }
}
