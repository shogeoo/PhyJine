package io.github.shogeo.phyjine;

import io.github.shogeo.phyjine.core.Body;
import io.github.shogeo.phyjine.core.Material;
import io.github.shogeo.phyjine.core.PhysicsWorld;
import io.github.shogeo.phyjine.core.utils.Vector2D;
import io.github.shogeo.phyjine.renderer.RenderPanel;
import io.github.shogeo.phyjine.renderer.Renderer;

public class Main {

    private static final double SIMULATION_STEP = 1.0 / 5760.0;
    private static final long FRAME_INTERVAL_NANOS = 1_000_000_000L / 60;
    private static final double MAX_FRAME_TIME = 0.25;
    private static final long SPIN_SAFETY_NANOS = 2_000_000L;

    public static void main(String[] args) throws Exception {
        PhysicsWorld world = new PhysicsWorld(-9.81);
        RenderPanel panel = new Renderer(world).start();

        setPhysicsScence(world);

        double accumulator = 0.0;
        long previousTime = System.nanoTime();
        long nextRenderTime = previousTime;

        while (true) {
            long now = System.nanoTime();

            if (panel.consumeResetRequested()) {
                world.getBodies().clear();
                setPhysicsScence(world);
                panel.clearSceneRenderData();

                accumulator = 0.0;
                previousTime = now;
                nextRenderTime = now;
            }

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

    private static void setPhysicsScence(PhysicsWorld world){
        Material staticMaterial = new Material(
                0.0,    // density -> static body
                0.2,    // restitution
                0.8,    // static friction
                0.6     // kinetic friction
        );

        Material bouncyMaterial = new Material(
                1.0,
                0.85,
                0.4,
                0.25
        );

        Material normalMaterial = new Material(
                1.0,
                0.3,
                0.6,
                0.4
        );

        Material slipperyMaterial = new Material(
                1.0,
                0.1,
                0.05,
                0.02
        );


// Ground
        world.addBody(
                Body.box(
                        0.0, -3.0,
                        0.0,
                        12.0, 0.5,
                        staticMaterial
                )
        );


// Two static ramps
        world.addBody(
                Body.box(
                        -3.5, -1.4,
                        Math.toRadians(-15),
                        4.0, 0.25,
                        staticMaterial
                )
        );

        world.addBody(
                Body.box(
                        3.5, -0.5,
                        Math.toRadians(18),
                        4.0, 0.25,
                        staticMaterial
                )
        );


// Bouncy circle
        Body circle = Body.circle(
                -3.5, 3.5,
                0.0,
                0.45,
                bouncyMaterial
        );
        circle.setVelocity(new Vector2D(1.8, 0.0));
        world.addBody(circle);


// Rotating box
        Body box = Body.box(
                0.0, 4.5,
                Math.toRadians(20),
                1.2, 1.2,
                normalMaterial
        );
        box.setVelocity(new Vector2D(0.4, 0.0));
        box.setAngularVelocity(1.5);
        world.addBody(box);


// Polygon
        Body polygon = Body.regularPolygon(
                new Vector2D(2.0, 5.5),
                0.0,
                5,
                0.65,
                normalMaterial
        );
        polygon.setAngularVelocity(-2.0);
        world.addBody(polygon);


// Slippery body to make friction difference visible
        Body slipperyBox = Body.box(
                -1.5, 1.5,
                Math.toRadians(5),
                0.9, 0.9,
                slipperyMaterial
        );
        slipperyBox.setVelocity(new Vector2D(3.0, 0.0));
        world.addBody(slipperyBox);


// Stack of boxes
        for (int i = 0; i < 4; i++) {
            Body stackBox = Body.box(
                    1.0,
                    -2.4 + i * 0.65,
                    0.0,
                    0.6, 0.6,
                    normalMaterial
            );

            world.addBody(stackBox);
        }
    }
}
