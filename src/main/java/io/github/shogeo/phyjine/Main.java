package io.github.shogeo.phyjine;

import io.github.shogeo.phyjine.colliders.CircleCollider;
import io.github.shogeo.phyjine.renderer.Camera;
import io.github.shogeo.phyjine.renderer.RenderWindow;
import io.github.shogeo.phyjine.utils.Vector2D;

import javax.swing.*;

public class Main {
    static void main() {
        PhysicsWorld world = new PhysicsWorld(0);

        Camera camera = new Camera();
        RenderWindow window = new RenderWindow("PhyJine", camera);
        SwingUtilities.invokeLater(window::show);

        Material m = new Material(7800, 0.75, 0.5, 0.05, 0.8);

        CircleCollider c1 = new CircleCollider(new Vector2D(3, 4), 0, 1.5, m);
        CircleCollider c2 = new CircleCollider(new Vector2D(-3, -4), 0, 1.5, m);

        Body b = new Body(new Vector2D(0, 0), 0, c1, c2);

        world.addBody(b);

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

            window.render(world.getBodies());
        }
    }
}
