package io.github.shogeo.phyjine.renderer;

import io.github.shogeo.phyjine.Body;
import io.github.shogeo.phyjine.colliders.CircleCollider;
import io.github.shogeo.phyjine.colliders.Collider;
import io.github.shogeo.phyjine.utils.AABB;
import io.github.shogeo.phyjine.utils.Vector2D;

import javax.swing.*;
import java.awt.*;
import java.util.List;

class RenderPanel extends JPanel {

    private static final Color GRID_COLOR = new Color(40, 40, 40);
    private static final Color AXIS_COLOR = new Color(100, 100, 100);
    private static final Color COLLIDER_COLOR = new Color(0, 255, 0);
    private static final Color COM_COLOR = Color.YELLOW;
    private static final Color BODY_AABB_COLOR = new Color(128, 0, 128); // Пурпурный
    private static final Color COLLIDER_AABB_COLOR = Color.GRAY;

    private static final double[] NICE_SPACINGS = {0.001, 0.002, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.5, 1, 2, 5, 10, 20, 50, 100, 200, 500, 1000};

    private final Camera camera;
    private List<Body> bodies = List.of();

    RenderPanel(Camera camera) {
        this.camera = camera;
        setBackground(Color.BLACK);
    }

    void setBodies(List<Body> bodies) {
        this.bodies = bodies;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        drawGrid(g2d, w, h);
        drawAxes(g2d, w, h);

        for (Body body : bodies) {
            drawBody(g2d, body, w, h);
        }
    }

    private void drawGrid(Graphics2D g, int w, int h) {
        g.setColor(GRID_COLOR);

        double scale = camera.getZoom() * Camera.UNITS_PER_METER;
        double desiredScreenSpacing = 80.0;
        double worldSpacing = NICE_SPACINGS[NICE_SPACINGS.length - 1];
        for (double s : NICE_SPACINGS) {
            if (s * scale >= desiredScreenSpacing) {
                worldSpacing = s;
                break;
            }
        }

        Vector2D topLeft = camera.screenToWorld(new Point(0, 0), w, h);
        Vector2D bottomRight = camera.screenToWorld(new Point(w, h), w, h);

        double minX = Math.min(topLeft.x(), bottomRight.x());
        double maxX = Math.max(topLeft.x(), bottomRight.x());
        double minY = Math.min(topLeft.y(), bottomRight.y());
        double maxY = Math.max(topLeft.y(), bottomRight.y());

        double startX = Math.floor(minX / worldSpacing) * worldSpacing;
        double startY = Math.floor(minY / worldSpacing) * worldSpacing;

        for (double wx = startX; wx <= maxX + worldSpacing; wx += worldSpacing) {
            Point p = camera.worldToScreen(new Vector2D(wx, 0), w, h);
            g.drawLine(p.x, 0, p.x, h);
        }
        for (double wy = startY; wy <= maxY + worldSpacing; wy += worldSpacing) {
            Point p = camera.worldToScreen(new Vector2D(0, wy), w, h);
            g.drawLine(0, p.y, w, p.y);
        }
    }

    private void drawAxes(Graphics2D g, int w, int h) {
        g.setColor(AXIS_COLOR);
        g.setStroke(new BasicStroke(1.5f));

        Point origin = camera.worldToScreen(new Vector2D(0, 0), w, h);
        g.drawLine(origin.x, 0, origin.x, h);
        g.drawLine(0, origin.y, w, origin.y);

        g.setStroke(new BasicStroke(1.0f));
    }

    private void drawAABB(Graphics2D g, AABB aabb, int w, int h) {
        if (aabb == null) return;
        Point min = camera.worldToScreen(aabb.min(), w, h);
        Point max = camera.worldToScreen(aabb.max(), w, h);
        int rx = Math.min(min.x, max.x);
        int ry = Math.min(min.y, max.y);
        int rw = Math.abs(max.x - min.x);
        int rh = Math.abs(max.y - min.y);
        g.drawRect(rx, ry, rw, rh);
    }

    private void drawBody(Graphics2D g, Body body, int w, int h) {
        // Рисуем AABB тела
        g.setColor(BODY_AABB_COLOR);
        drawAABB(g, body.getAabb(), w, h);

        // Рисуем центр масс
        Vector2D comWorld = body.getPosition();
        Point comScreen = camera.worldToScreen(comWorld, w, h);
        g.setColor(COM_COLOR);
        g.fillOval(comScreen.x - 5, comScreen.y - 5, 10, 10);

        for (Collider collider : body.getColliders()) {
            drawCollider(g, collider, body, w, h);
        }
    }

    private void drawCollider(Graphics2D g, Collider collider, Body body, int w, int h) {
        // Рисуем AABB коллайдера
        g.setColor(COLLIDER_AABB_COLOR);
        drawAABB(g, collider.getAabb(), w, h);

        // Рисуем сам коллайдер
        Vector2D localPos = collider.getPosition();
        Vector2D rotatedOffset = localPos.rotate(body.getAngle());
        Vector2D worldPos = body.getPosition().add(rotatedOffset);

        g.setColor(COLLIDER_COLOR);

        if (collider instanceof CircleCollider c) {
            double scale = camera.getZoom() * Camera.UNITS_PER_METER;
            int screenRadius = (int) Math.round(c.getRadius() * scale);
            Point center = camera.worldToScreen(worldPos, w, h);
            g.drawOval(center.x - screenRadius, center.y - screenRadius, screenRadius * 2, screenRadius * 2);
        } else {
            // Для не-круговых коллайдеров можно нарисовать их AABB еще раз, но другим цветом,
            // или реализовать более сложную отрисовку полигонов.
            // Пока что AABB уже нарисован выше.
        }
    }
}
