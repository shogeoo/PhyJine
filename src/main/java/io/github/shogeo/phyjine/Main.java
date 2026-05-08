package io.github.shogeo.phyjine;

import io.github.shogeo.phyjine.colliders.CircleCollider;
import io.github.shogeo.phyjine.utils.Vector2D;

public class Main {
    static void main() {

        long lastIterationStartTime;
        long currentIterationStartTime;
        double dt;

        long epoch = 1_000_000_000L;

        PhysicsWorld main = new PhysicsWorld(-9.81);

        Material test = new Material(1000, 0.75, 0.5, 0.05, 0);

        CircleCollider c = new CircleCollider(new Vector2D(0, 0), 0, 0.5, test);
        Body b = new Body(new Vector2D(0, 0), 0, c);

        main.addBody(b);

        lastIterationStartTime = System.nanoTime();

        while (true) {
            currentIterationStartTime = System.nanoTime();
            dt = (double) (currentIterationStartTime - lastIterationStartTime) / epoch;
            lastIterationStartTime = currentIterationStartTime;

            main.step(dt);


        }
    }
}