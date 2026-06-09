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

        Material groundMaterial = new Material(0.0, 0, 0.8, 0.6);
        Material dynamicMaterial = new Material(1.0, 0, 0.6, 0.4);

        // 1. Static Ground
        List<Vector2D> groundVerts = createBoxVertices(15.0, 0.6);
        PolygonCollider groundCollider = new PolygonCollider(new Vector2D(0, 0), 0.0, groundVerts, groundMaterial);
        Body ground = new Body(new Vector2D(0, -5.0), 0.0, groundCollider);
        world.addBody(ground);

        // 2. Static Funnel (Left slope and Right slope)
        List<Vector2D> slopeVerts = createBoxVertices(5.0, 0.4);
        
        PolygonCollider leftSlopeCollider = new PolygonCollider(new Vector2D(0, 0), 0.0, slopeVerts, groundMaterial);
        Body leftSlope = new Body(new Vector2D(-4.0, 1.0), 0.52, leftSlopeCollider); // ~30 degrees
        world.addBody(leftSlope);

        PolygonCollider rightSlopeCollider = new PolygonCollider(new Vector2D(0, 0), 0.0, slopeVerts, groundMaterial);
        Body rightSlope = new Body(new Vector2D(4.0, 1.0), -0.52, rightSlopeCollider); // ~-30 degrees
        world.addBody(rightSlope);

        // 3. Static Hexagonal pegs/obstacles in the middle
        List<Vector2D> pegVerts = createRegularPolygonVertices(6, 0.4);
        PolygonCollider peg1Collider = new PolygonCollider(new Vector2D(0, 0), 0.0, pegVerts, groundMaterial);
        Body peg1 = new Body(new Vector2D(0.0, -1.5), 0.0, peg1Collider);
        world.addBody(peg1);

        PolygonCollider peg2Collider = new PolygonCollider(new Vector2D(0, 0), 0.0, pegVerts, groundMaterial);
        Body peg2 = new Body(new Vector2D(-2.5, -2.5), 0.2, peg2Collider);
        world.addBody(peg2);

        PolygonCollider peg3Collider = new PolygonCollider(new Vector2D(0, 0), 0.0, pegVerts, groundMaterial);
        Body peg3 = new Body(new Vector2D(2.5, -2.5), -0.2, peg3Collider);
        world.addBody(peg3);

        // 4. Stacks of different shapes falling and colliding!
        double startY = 4.0;
        for (int i = 0; i < 8; i++) {
            double offset = (i % 2 == 0) ? 0.1 : -0.1; // Slightly offset each layer to create dynamic sliding/tumbling
            double y = startY + i * 1.5;

            if (i % 4 == 0) {
                // Falling Box
                List<Vector2D> boxVerts = createBoxVertices(0.8, 0.8);
                PolygonCollider boxCollider = new PolygonCollider(new Vector2D(0, 0), 0.0, boxVerts, dynamicMaterial);
                Body box = new Body(new Vector2D(offset, y), 0.1 * i, boxCollider);
                world.addBody(box);
            } else if (i % 4 == 1) {
                // Falling Circle
                CircleCollider circleCollider = new CircleCollider(new Vector2D(0, 0), 0.0, 0.45, dynamicMaterial);
                Body circle = new Body(new Vector2D(offset, y), 0.0, circleCollider);
                world.addBody(circle);
            } else if (i % 4 == 2) {
                // Falling Triangle
                List<Vector2D> triVerts = createRegularPolygonVertices(3, 0.5);
                PolygonCollider triCollider = new PolygonCollider(new Vector2D(0, 0), 0.0, triVerts, dynamicMaterial);
                Body triangle = new Body(new Vector2D(offset, y), 0.05 * i, triCollider);
                world.addBody(triangle);
            } else {
                // Falling Pentagon
                List<Vector2D> pentVerts = createRegularPolygonVertices(5, 0.5);
                PolygonCollider pentCollider = new PolygonCollider(new Vector2D(0, 0), 0.0, pentVerts, dynamicMaterial);
                Body pentagon = new Body(new Vector2D(offset, y), -0.05 * i, pentCollider);
                world.addBody(pentagon);
            }
        }

        // Additional side falling bodies to hit the slopes directly
        for (int i = 0; i < 3; i++) {
            double y = 6.0 + i * 2.0;
            // Left falling circle
            CircleCollider circleL = new CircleCollider(new Vector2D(0, 0), 0.0, 0.4, dynamicMaterial);
            Body bodyL = new Body(new Vector2D(-4.5, y), 0.0, circleL);
            world.addBody(bodyL);

            // Right falling triangle
            List<Vector2D> triVerts = createRegularPolygonVertices(3, 0.5);
            PolygonCollider triR = new PolygonCollider(new Vector2D(0, 0), 0.0, triVerts, dynamicMaterial);
            Body bodyR = new Body(new Vector2D(4.5, y), 0.1, triR);
            world.addBody(bodyR);
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

    private static List<Vector2D> createRegularPolygonVertices(int numSides, double radius) {
        List<Vector2D> verts = new ArrayList<>();
        for (int i = 0; i < numSides; i++) {
            // Start at angle -PI/2 (pointing up) to make regular polygons stand upright
            double angle = -Math.PI / 2 + 2 * Math.PI * i / numSides;
            verts.add(new Vector2D(radius * Math.cos(angle), radius * Math.sin(angle)));
        }
        return verts;
    }
}