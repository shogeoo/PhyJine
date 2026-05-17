package io.github.shogeo.phyjine;

import io.github.shogeo.phyjine.core.Body;
import io.github.shogeo.phyjine.core.Material;
import io.github.shogeo.phyjine.core.PhysicsWorld;
import io.github.shogeo.phyjine.core.colliders.CircleCollider;
import io.github.shogeo.phyjine.core.utils.Vector2D;
import io.github.shogeo.phyjine.renderer.Camera;
import io.github.shogeo.phyjine.renderer.RenderWindow;

import javax.swing.*;

public class Main {
    static void main() {
        PhysicsWorld world = new PhysicsWorld(0);

        Camera camera = new Camera();
        RenderWindow window = new RenderWindow("PhyJine", camera);
        SwingUtilities.invokeLater(window::show);

        Material m = new Material(7800, 0.75, 0.5, 0.05, 0.8);

        // Тело 1 (слева)
        CircleCollider c1_1 = new CircleCollider(new Vector2D(0, 2), 0, 1, m);
        CircleCollider c1_2 = new CircleCollider(new Vector2D(0, -2), 0, 1, m);
        Body b1 = new Body(new Vector2D(-5, 0), 0, c1_1, c1_2);
        b1.setVelocity(new Vector2D(5, 0));
        world.addBody(b1);

        // Тело 2 (справа)
        CircleCollider c2_1 = new CircleCollider(new Vector2D(0, 2), 0, 1, m);
        CircleCollider c2_2 = new CircleCollider(new Vector2D(0, -2), 0, 1, m);
        // Смещаем второе тело вниз на расстояние, равное диаметру коллайдера (2 * радиус)
        // чтобы верхний коллайдер второго тела был на уровне нижнего коллайдера первого
        Body b2 = new Body(new Vector2D(5, -4), 0, c2_1, c2_2);
        b2.setVelocity(new Vector2D(-5, 0));
        world.addBody(b2);

        long lastIterationStartTime;
        long currentIterationStartTime;
        double dt;

        long epoch = 1_000_000_000L;

        lastIterationStartTime = System.nanoTime();

        while (true) {

            currentIterationStartTime = System.nanoTime();
            dt = (double) (currentIterationStartTime - lastIterationStartTime) / epoch;
            lastIterationStartTime = currentIterationStartTime;

            world.step(0.0000001);

            window.render(world.getBodies());
        }
    }
}