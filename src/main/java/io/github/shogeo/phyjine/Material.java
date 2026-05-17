package io.github.shogeo.phyjine;

public record Material(double density, double staticFriction, double kineticFriction, double rollingFriction,
                       double restitution) {
}
