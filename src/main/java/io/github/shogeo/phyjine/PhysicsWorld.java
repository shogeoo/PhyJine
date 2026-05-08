package io.github.shogeo.phyjine;

import java.util.ArrayList;
import java.util.List;

public class PhysicsWorld {

    private final double gravity;

    private final List<Body> bodies = new ArrayList<>();

    public PhysicsWorld(double gravity) {
        this.gravity = gravity;
    }

    void step(double dt) {
    }

    void addBody(Body body) {
        bodies.add(body);
    }
}
