package io.github.shogeo.phyjine.core;

public record Material(double density, double staticFriction, double kineticFriction, double rollingFriction,
                       double restitution) {
}
