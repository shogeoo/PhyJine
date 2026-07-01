package io.github.shogeo.phyjine.renderer;

import io.github.shogeo.phyjine.core.Body;
import io.github.shogeo.phyjine.core.PhysicsWorld;
import io.github.shogeo.phyjine.core.colliders.CircleCollider;
import io.github.shogeo.phyjine.core.colliders.Collider;
import io.github.shogeo.phyjine.core.colliders.PolygonCollider;
import io.github.shogeo.phyjine.core.utils.AABB;
import io.github.shogeo.phyjine.core.utils.Vector2D;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;

public class RenderPanel extends Canvas implements Runnable {

    private static final double UNITS_PER_METER = 100.0;

    private final PhysicsWorld world;
    private final Camera camera;
    private final Line2D.Double line = new Line2D.Double();
    private final Rectangle2D.Double rect = new Rectangle2D.Double();
    private final Ellipse2D.Double ellipse = new Ellipse2D.Double();
    private Point lastMousePos;
    private final boolean renderAABBs = false;
    private volatile boolean running = true;

    public RenderPanel(PhysicsWorld world) {
        this.world = world;
        this.camera = new Camera();
        setBackground(Color.BLACK);
        setFocusable(true);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    lastMousePos = e.getPoint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePos != null && javax.swing.SwingUtilities.isRightMouseButton(e)) {
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

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    world.togglePause();
                }
            }
        });
    }

    public void start() {
        new Thread(this, "RenderThread").start();
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        createBufferStrategy(2);
        BufferStrategy bs = getBufferStrategy();

        while (running) {
            long startTime = System.nanoTime();

            render(bs);

            long elapsed = System.nanoTime() - startTime;
            long targetDelay = 8_333_333L; // 120 FPS target
            if (elapsed < targetDelay) {
                long sleepMs = (targetDelay - elapsed) / 1_000_000L;
                int sleepNs = (int) ((targetDelay - elapsed) % 1_000_000L);
                try {
                    Thread.sleep(sleepMs, sleepNs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                Thread.yield();
            }
        }
    }

    private void render(BufferStrategy bs) {
        do {
            do {
                Graphics2D g2d = (Graphics2D) bs.getDrawGraphics();
                if (g2d == null) return;
                try {
                    g2d.setColor(getBackground());
                    g2d.fillRect(0, 0, getWidth(), getHeight());

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

                    if (world.isPaused()) {
                        drawPauseIndicator(g2d);
                    }
                }
                finally {
                    g2d.dispose();
                }
            } while (bs.contentsRestored());
            bs.show();
        } while (bs.contentsLost());
    }

    private void drawPauseIndicator(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        String text = "PAUSED";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, x, y);
    }

    private void drawGridAndAxes(Graphics2D g2d) {
        double startX = 0;
        double startY = 0;
        double endX = getWidth();
        double endY = getHeight();

        AffineTransform inverse;
        try {
            inverse = camera.getTransform().createInverse();
            java.awt.geom.Point2D minWorld = inverse.transform(new java.awt.geom.Point2D.Double(0, getHeight()), null);
            java.awt.geom.Point2D maxWorld = inverse.transform(new java.awt.geom.Point2D.Double(getWidth(), 0), null);
            startX = minWorld.getX();
            startY = minWorld.getY();
            endX = maxWorld.getX();
            endY = maxWorld.getY();
        } catch (Exception e) {
            startX = -getWidth() * 2;
            startY = -getHeight() * 2;
            endX = getWidth() * 2;
            endY = getHeight() * 2;
        }

        double spacing = UNITS_PER_METER;
        double log2 = Math.log(camera.getScale()) / Math.log(2);
        spacing = UNITS_PER_METER * Math.pow(2, -Math.floor(log2));

        if (camera.getScale() * spacing < 50) {
            spacing *= 2;
        }
        if (camera.getScale() * spacing > 100) {
            spacing /= 2;
        }

        g2d.setColor(new Color(40, 40, 40));
        for (double x = Math.floor(startX / spacing) * spacing; x < endX; x += spacing) {
            line.setLine(x, startY, x, endY);
            g2d.draw(line);
        }
        for (double y = Math.floor(startY / spacing) * spacing; y < endY; y += spacing) {
            line.setLine(startX, y, endX, y);
            g2d.draw(line);
        }

        g2d.setColor(new Color(80, 80, 80));
        line.setLine(0, startY, 0, endY);
        g2d.draw(line);
        line.setLine(startX, 0, endX, 0);
        g2d.draw(line);
    }

    private void drawWorld(Graphics2D g2d) {
        List<Body> bodies;
        synchronized (world) {
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

        if (renderAABBs) {
            AABB bodyAabb = body.getAabb();
            if (bodyAabb != null) {
                g2d.setColor(new Color(220, 0, 220));
                drawAABB(g2d, bodyAabb);
            }
        }

        for (Collider collider : body.getColliders()) {
            drawCollider(g2d, collider, body.getAngle());
        }

        g2d.setColor(Color.WHITE);
        ellipse.setFrame(bodyX - 2 / camera.getScale(), bodyY - 2 / camera.getScale(), 4 / camera.getScale(), 4 / camera.getScale());
        g2d.fill(ellipse);
    }

    private void drawCollider(Graphics2D g2d, Collider collider, double bodyAngle) {
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

            g2d.setColor(new Color(0, 180, 0));
            ellipse.setFrame(globalX - radius, globalY - radius, radius * 2, radius * 2);
            g2d.draw(ellipse);

            double totalAngle = bodyAngle + circle.getAngle();
            line.setLine(globalX, globalY, globalX + radius * Math.cos(totalAngle), globalY + radius * Math.sin(totalAngle));
            g2d.draw(line);
        } else if (collider instanceof PolygonCollider polygon) {
            List<Vector2D> worldVertices = polygon.getWorldVertices();
            g2d.setColor(new Color(0, 150, 255));
            int n = worldVertices.size();
            for (int i = 0; i < n; i++) {
                Vector2D v1 = worldVertices.get(i);
                Vector2D v2 = worldVertices.get((i + 1) % n);
                line.setLine(v1.x() * UNITS_PER_METER, v1.y() * UNITS_PER_METER, v2.x() * UNITS_PER_METER, v2.y() * UNITS_PER_METER);
                g2d.draw(line);
            }
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