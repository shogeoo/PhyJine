package io.github.shogeo.phyjine;

import java.util.ArrayList;

public class PhysicsWorld {

    final double GRAVITY;

    ArrayList<Body> bodies = new ArrayList<Body>();

    public PhysicsWorld(double gravity) {
        this.GRAVITY = gravity;
    }

    void step(double dt) {
    }

    void addBody(Body body) {
        bodies.add(body);
    }
}
