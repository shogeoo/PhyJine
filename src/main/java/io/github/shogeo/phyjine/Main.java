package io.github.shogeo.phyjine;

import io.github.shogeo.phyjine.core.Body;
import io.github.shogeo.phyjine.core.Material;
import io.github.shogeo.phyjine.core.PhysicsWorld;
import io.github.shogeo.phyjine.core.colliders.CircleCollider;
import io.github.shogeo.phyjine.core.utils.Vector2D;
import io.github.shogeo.phyjine.renderer.Renderer;

public class Main {
    static void main() {
        PhysicsWorld world = new PhysicsWorld(0);

        Renderer renderer = new Renderer(world);
        new Thread(renderer).start();



        Material m = new Material(7800, 0.6, 0.75, 0.5);
        CircleCollider c1 = new CircleCollider(new Vector2D(0, 4), 0, 1, m);
        CircleCollider c2 = new CircleCollider(new Vector2D(0, -4), 0, 1, m);
        Body b = new Body(new Vector2D(0, 0), 0, c1, c2);

        world.addBody(b);

        CircleCollider c3 = new CircleCollider(new Vector2D(0, 4), 0, 1, m);
        CircleCollider c4 = new CircleCollider(new Vector2D(0, -4), 0, 1, m);
        Body b2 = new Body(new Vector2D(10, -8), 0, c3, c4);

        world.addBody(b2);

        b.setVelocity(new Vector2D(5, 0));


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