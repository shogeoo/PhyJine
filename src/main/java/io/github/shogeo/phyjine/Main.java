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
        // Задача трёх тел: устойчивая орбита в форме "восьмёрки" (Chenciner–Montgomery).
        // Начальные условия рассчитаны на равные массы тел m = 1 и гравитационную постоянную G = 1.
        double radius = 0.03;
        double density = 1.0 / (Math.PI * radius * radius); // масса каждого коллайдера получается ровно 1
        Material material = new Material(density, 0, 0, 0);

        PhysicsWorld world = new PhysicsWorld(1.0); // G — постоянная всемирного тяготения

        Vector2D[] positions = {
                new Vector2D(-0.97000436, 0.24308753),
                new Vector2D(0.97000436, -0.24308753),
                new Vector2D(0, 0)
        };
        Vector2D[] velocities = {
                new Vector2D(0.4662036850, 0.4323657300),
                new Vector2D(0.4662036850, 0.4323657300),
                new Vector2D(-0.93240737, -0.86473146)
        };

        for (int i = 0; i < positions.length; i++) {
            Body body = Body.circle(positions[i], 0, radius, material);
            body.setVelocity(velocities[i]);
            world.addBody(body);
        }

        return world;
    }
}
