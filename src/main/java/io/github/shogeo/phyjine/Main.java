package io.github.shogeo.phyjine;

import io.github.shogeo.phyjine.core.PhysicsWorld;
import io.github.shogeo.phyjine.renderer.Renderer;

public class Main {
    static void main() {
        PhysicsWorld world = new PhysicsWorld(-9.81);

        Renderer renderer = new Renderer(world);
        new Thread(renderer).start();


        //


        long lastIterationStartTime;
        long currentIterationStartTime;
        double dt;
        long epoch = 1_000_000_0000L;
        lastIterationStartTime = System.nanoTime();

        while (true) {
            currentIterationStartTime = System.nanoTime();
            dt = (double) (currentIterationStartTime - lastIterationStartTime) / epoch;
            lastIterationStartTime = currentIterationStartTime;
            world.step(dt);
        }
    }
}