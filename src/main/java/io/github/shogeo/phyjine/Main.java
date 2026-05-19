package io.github.shogeo.phyjine;

import io.github.shogeo.phyjine.core.PhysicsWorld;

public class Main {
    static void main() {
        PhysicsWorld world = new PhysicsWorld(-9.81);

        long lastIterationStartTime;
        long currentIterationStartTime;
        double dt;
        long epoch = 1_000_000_000L;
        lastIterationStartTime = System.nanoTime();

        while (true) {
            currentIterationStartTime = System.nanoTime();
            dt = (double) (currentIterationStartTime - lastIterationStartTime) / epoch;
            lastIterationStartTime = currentIterationStartTime;
            world.step(dt);
        }
    }
}