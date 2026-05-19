package io.github.shogeo.phyjine.renderer;

import io.github.shogeo.phyjine.core.Body;
import io.github.shogeo.phyjine.core.PhysicsWorld;
import io.github.shogeo.phyjine.core.colliders.CircleCollider;
import io.github.shogeo.phyjine.core.colliders.Collider;
import io.github.shogeo.phyjine.core.utils.AABB;
import io.github.shogeo.phyjine.core.utils.Vector2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class RenderPanel extends JPanel {

    private static final double UNITS_PER_METER = 100.0;

    private final PhysicsWorld world;
    private final Camera camera;
    // Pre-allocated objects for drawing to reduce garbage collection
    private final Line2D.Double line = new Line2D.Double();
    private final Rectangle2D.Double rect = new Rectangle2D.Double();
    private final Ellipse2D.Double ellipse = new Ellipse2D.Double();
    private Point lastMousePos;
    private final boolean renderAABBs = false;

    public RenderPanel(PhysicsWorld world) {
        this.world = world;
        this.camera = new Camera();
        setBackground(Color.BLACK);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    lastMousePos = e.getPoint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePos != null && SwingUtilities.isRightMouseButton(e)) {
                    int dx = e.getX() - lastMousePos.x;
                    int dy = e.getY() - lastMousePos.y;
                    camera.pan(dx, dy);
                    lastMousePos = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    lastMousePos = null;
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double zoomFactor = Math.pow(1.1, -e.getWheelRotation());
                camera.zoom(zoomFactor, e.getPoint());
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (!camera.isInitialized()) {
            camera.initialize(getWidth(), getHeight());
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform savedTransform = g2d.getTransform();

        g2d.transform(camera.getTransform());

        g2d.setStroke(new BasicStroke(1.0f / (float) camera.getScale()));

        drawGridAndAxes(g2d);
        drawWorld(g2d);

        g2d.setTransform(savedTransform);
    }

    private void drawGridAndAxes(Graphics2D g2d) {
        Rectangle clipBounds = g2d.getClipBounds();
        if (clipBounds == null) return;

        double startX = clipBounds.getX();
        double startY = clipBounds.getY();
        double endX = startX + clipBounds.getWidth();
        double endY = startY + clipBounds.getHeight();

        // Dynamic grid spacing based on powers of 2
        double log2 = Math.log(camera.getScale()) / Math.log(2);
        double spacing = UNITS_PER_METER * Math.pow(2, -Math.floor(log2));

        if (camera.getScale() * spacing < 50) {
            spacing *= 2;
        }
        if (camera.getScale() * spacing > 100) {
            spacing /= 2;
        }


        // Grid lines
        g2d.setColor(new Color(40, 40, 40));
        for (double x = Math.floor(startX / spacing) * spacing; x < endX; x += spacing) {
            line.setLine(x, startY, x, endY);
            g2d.draw(line);
        }
        for (double y = Math.floor(startY / spacing) * spacing; y < endY; y += spacing) {
            line.setLine(startX, y, endX, y);
            g2d.draw(line);
        }

        // Axes (brighter gray)
        g2d.setColor(new Color(80, 80, 80));
        line.setLine(0, startY, 0, endY); // Y-axis
        g2d.draw(line);
        line.setLine(startX, 0, endX, 0); // X-axis
        g2d.draw(line);
    }

    private void drawWorld(Graphics2D g2d) {
        List<Body> bodies;
        synchronized (world) {
            // Defensive copy to avoid ConcurrentModificationException
            bodies = new ArrayList<>(world.getBodies());
        }

        for (Body body : bodies) {
            drawBody(g2d, body);
        }
    }

    private void drawBody(Graphics2D g2d, Body body) {
        Vector2D bodyPos = body.getPosition();
        double bodyX = bodyPos.x() * UNITS_PER_METER;
        double bodyY = bodyPos.y() * UNITS_PER_METER;

        // Draw Body AABB
        if (renderAABBs) {
            AABB bodyAabb = body.getAabb();
            if (bodyAabb != null) {
                g2d.setColor(new Color(220, 0, 220)); // Magenta/Pink
                drawAABB(g2d, bodyAabb);
            }
        }

        for (Collider collider : body.getColliders()) {
            drawCollider(g2d, collider, body.getAngle());
        }

        // Draw center of mass on top
        g2d.setColor(Color.WHITE);
        ellipse.setFrame(bodyX - 2 / camera.getScale(), bodyY - 2 / camera.getScale(), 4 / camera.getScale(), 4 / camera.getScale());
        g2d.fill(ellipse);
    }

    private void drawCollider(Graphics2D g2d, Collider collider, double bodyAngle) {
        // Draw Collider AABB
        if (renderAABBs) {
            AABB colliderAabb = collider.getAabb();
            if (colliderAabb != null) {
                g2d.setColor(Color.GRAY);
                drawAABB(g2d, colliderAabb);
            }
        }

        if (collider instanceof CircleCollider circle) {
            Vector2D bodyPos = circle.getOwner().getPosition();
            Vector2D localPos = circle.getPosition();
            Vector2D globalPos = bodyPos.add(localPos.rotate(bodyAngle));

            double globalX = globalPos.x() * UNITS_PER_METER;
            double globalY = globalPos.y() * UNITS_PER_METER;
            double radius = circle.getRadius() * UNITS_PER_METER;

            // Draw circle
            g2d.setColor(new Color(0, 180, 0)); // A nice green
            ellipse.setFrame(globalX - radius, globalY - radius, radius * 2, radius * 2);
            g2d.draw(ellipse);

            // Draw angle indicator
            double totalAngle = bodyAngle + circle.getAngle();
            line.setLine(globalX, globalY, globalX + radius * Math.cos(totalAngle), globalY + radius * Math.sin(totalAngle));
            g2d.draw(line);
        }
    }

    private void drawAABB(Graphics2D g2d, AABB aabb) {
        double minX = aabb.min().x() * UNITS_PER_METER;
        double minY = aabb.min().y() * UNITS_PER_METER;
        double width = (aabb.max().x() - aabb.min().x()) * UNITS_PER_METER;
        double height = (aabb.max().y() - aabb.min().y()) * UNITS_PER_METER;
        rect.setFrame(minX, minY, width, height);
        g2d.draw(rect);
    }
}