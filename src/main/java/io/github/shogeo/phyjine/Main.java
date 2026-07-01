package io.github.shogeo.phyjine;

import io.github.shogeo.phyjine.core.Body;
import io.github.shogeo.phyjine.core.Material;
import io.github.shogeo.phyjine.core.PhysicsWorld;
import io.github.shogeo.phyjine.core.colliders.CircleCollider;
import io.github.shogeo.phyjine.core.colliders.PolygonCollider;
import io.github.shogeo.phyjine.core.utils.Vector2D;
import io.github.shogeo.phyjine.renderer.Renderer;

import java.util.ArrayList;
import java.util.List;

public class Main {
    static void main() {
        PhysicsWorld world = new PhysicsWorld(-9.81);

        // --- Materials ---
        // restitution (упругость): 0 - perfectly inelastic, 1 - perfectly elastic
        // friction: static and kinetic friction coefficients
        Material groundMaterial = new Material(0, 0.5, 0.8, 0.6); // Static material, density 0 means infinite mass
        Material wood = new Material(700, 0.4, 0.7, 0.5);
        Material stone = new Material(2500, 0.6, 0.8, 0.6);
        Material steel = new Material(7850, 0.2, 0.4, 0.2);

        // --- Ground ---
        Body ground = new Body(
                new Vector2D(0, -25), 0,
                new PolygonCollider(new Vector2D(0, 0), 0, createBoxVertices(100, 50), groundMaterial)
        );
        world.addBody(ground);

        // --- Tower ---
        int towerHeight = 10;
        double boxWidth = 2.0;
        double boxHeight = 1.0;
        for (int i = 0; i < towerHeight; i++) {
            Material material = (i % 3 == 0) ? stone : wood;
            Body box = new Body(
                    new Vector2D(0, boxHeight / 2 + i * boxHeight), 0,
                    new PolygonCollider(new Vector2D(0, 0), 0, createBoxVertices(boxWidth, boxHeight), material)
            );
            world.addBody(box);
        }

        // --- Wrecking Ball ---
        double wreckingBallRadius = 0.5;
        Body wreckingBall = new Body(
                new Vector2D(-15, 10), 0,
                new CircleCollider(new Vector2D(0, 0), 0, wreckingBallRadius, steel)
        );
        wreckingBall.setVelocity(new Vector2D(20, 0)); // Give it an initial push
        world.addBody(wreckingBall);

        // --- A more complex structure ---
        // Let's build a pyramid
        int pyramidHeight = 5;
        double brickWidth = 1.0;
        double brickHeight = 0.5;
        for (int i = 0; i < pyramidHeight; i++) {
            int numBricks = pyramidHeight - i;
            for (int j = 0; j < numBricks; j++) {
                double x = 10 + (j - (numBricks - 1) / 2.0) * brickWidth;
                double y = brickHeight / 2 + i * brickHeight;
                Body brick = new Body(
                        new Vector2D(x, y), 0,
                        new PolygonCollider(new Vector2D(0, 0), 0, createBoxVertices(brickWidth, brickHeight), stone)
                );
                world.addBody(brick);
            }
        }

        // --- Dominoes ---
        int numDominoes = 10;
        double dominoWidth = 0.2;
        double dominoHeight = 1.5;
        for (int i = 0; i < numDominoes; i++) {
            Body domino = new Body(
                    new Vector2D(-10 - i * dominoHeight * 0.7, dominoHeight / 2), 0,
                    new PolygonCollider(new Vector2D(0, 0), 0, createBoxVertices(dominoWidth, dominoHeight), wood)
            );
            if (i == 0) {
                domino.applyTorque(-5000); // Push the first domino
            }
            world.addBody(domino);
        }


        Renderer renderer = new Renderer(world);
        new Thread(renderer).start();

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

    private static List<Vector2D> createBoxVertices(double width, double height) {
        List<Vector2D> verts = new ArrayList<>();
        double w2 = width / 2;
        double h2 = height / 2;
        verts.add(new Vector2D(-w2, -h2));
        verts.add(new Vector2D(w2, -h2));
        verts.add(new Vector2D(w2, h2));
        verts.add(new Vector2D(-w2, h2));
        return verts;
    }

}