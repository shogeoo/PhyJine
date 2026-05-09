package io.github.shogeo.phyjine;

import io.github.shogeo.phyjine.renderer.Camera;
import io.github.shogeo.phyjine.renderer.RenderWindow;

import javax.swing.SwingUtilities;

public class Main {
    static void main() {
        PhysicsWorld world = new PhysicsWorld(0);

        Camera camera = new Camera();
        RenderWindow window = new RenderWindow("PhyJine", camera);
        SwingUtilities.invokeLater(window::show);

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
