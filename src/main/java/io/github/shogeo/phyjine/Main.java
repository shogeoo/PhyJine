package io.github.shogeo.phyjine;

import io.github.shogeo.phyjine.core.Body;
import io.github.shogeo.phyjine.core.Material;
import io.github.shogeo.phyjine.core.PhysicsWorld;
import io.github.shogeo.phyjine.core.colliders.CircleCollider;
import io.github.shogeo.phyjine.core.colliders.PolygonCollider;
import io.github.shogeo.phyjine.core.utils.Vector2D;
import io.github.shogeo.phyjine.renderer.RenderPanel;
import io.github.shogeo.phyjine.renderer.Renderer;

import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final double SIMULATION_STEP = 1.0 / 360.0;
    private static final long FRAME_INTERVAL_NANOS = 1_000_000_000L / 60;
    private static final double MAX_FRAME_TIME = 0.25;
    private static final long SPIN_SAFETY_NANOS = 2_000_000L;

    public static void main(String[] args) throws Exception {
        PhysicsWorld world = createDemoWorld();
        RenderPanel panel = new Renderer(world).start();

        double accumulator = 0.0;
        long previousTime = System.nanoTime();
        long nextRenderTime = previousTime;

        while (true) {
            long now = System.nanoTime();
            double elapsed = Math.min((now - previousTime) / 1_000_000_000.0, MAX_FRAME_TIME);
            previousTime = now;

            if (!world.isPaused()) {
                accumulator += elapsed;
                while (accumulator >= SIMULATION_STEP) {
                    world.step(SIMULATION_STEP);
                    accumulator -= SIMULATION_STEP;
                }
            } else {
                accumulator = 0.0;
            }

            if (now >= nextRenderTime) {
                if (panel.isShowing()) {
                    panel.renderFrame();
                }
                nextRenderTime = now + FRAME_INTERVAL_NANOS;
            }

            long sleepUntil = nextRenderTime;
            if (!world.isPaused()) {
                long timeToNextStep = (long) ((SIMULATION_STEP - accumulator) * 1_000_000_000.0);
                sleepUntil = Math.min(sleepUntil, now + timeToNextStep);
            }
            sleepUntil(sleepUntil);
        }
    }

    private static void sleepUntil(long deadlineNanos) {
        long remaining = deadlineNanos - System.nanoTime();
        if (remaining <= 0) return;
        if (remaining > SPIN_SAFETY_NANOS) {
            long sleepNanos = remaining - SPIN_SAFETY_NANOS;
            try {
                Thread.sleep(sleepNanos / 1_000_000L, (int) (sleepNanos % 1_000_000L));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        while (System.nanoTime() < deadlineNanos) {
            Thread.onSpinWait();
        }
    }

    private static PhysicsWorld createDemoWorld() {
        PhysicsWorld world = new PhysicsWorld(-9.81);

        Material groundMaterial = new Material(0, 0.5, 0.8, 0.6);
        Material wood = new Material(700, 0.4, 0.7, 0.5);
        Material stone = new Material(2500, 0.6, 0.8, 0.6);
        Material steel = new Material(7850, 0.2, 0.4, 0.2);

        Body ground = new Body(
                new Vector2D(0, -25), 0,
                new PolygonCollider(new Vector2D(0, 0), 0, createBoxVertices(100, 50), groundMaterial)
        );
        world.addBody(ground);

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

        double wreckingBallRadius = 0.5;
        Body wreckingBall = new Body(
                new Vector2D(-15, 10), 0,
                new CircleCollider(new Vector2D(0, 0), 0, wreckingBallRadius, steel)
        );
        wreckingBall.setVelocity(new Vector2D(20, 0));
        world.addBody(wreckingBall);

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

        int numDominoes = 10;
        double dominoWidth = 0.2;
        double dominoHeight = 1.5;
        for (int i = 0; i < numDominoes; i++) {
            Body domino = new Body(
                    new Vector2D(-10 - i * dominoHeight * 0.7, dominoHeight / 2), 0,
                    new PolygonCollider(new Vector2D(0, 0), 0, createBoxVertices(dominoWidth, dominoHeight), wood)
            );
            if (i == 0) {
                domino.applyTorque(-5000);
            }
            world.addBody(domino);
        }

        return world;
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
