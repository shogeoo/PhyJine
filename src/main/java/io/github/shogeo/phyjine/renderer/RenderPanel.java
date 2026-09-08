package io.github.shogeo.phyjine.renderer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import io.github.shogeo.phyjine.core.Body;
import io.github.shogeo.phyjine.core.PhysicsWorld;
import io.github.shogeo.phyjine.core.colliders.CircleCollider;
import io.github.shogeo.phyjine.core.colliders.Collider;
import io.github.shogeo.phyjine.core.colliders.PolygonCollider;
import io.github.shogeo.phyjine.core.utils.AABB;
import io.github.shogeo.phyjine.core.utils.Vector2D;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;

public class RenderPanel extends GLCanvas implements GLEventListener {

    private static final double UNITS_PER_METER = 100.0;

    private final PhysicsWorld world;
    private final Camera camera = new Camera();

    private Point lastMousePos;
    private boolean renderAABBs = false;

    public RenderPanel(PhysicsWorld world) {
        super(createCapabilities());
        this.world = world;
        setFocusable(true);
        setIgnoreRepaint(true);
        addGLEventListener(this);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
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

    private static GLCapabilities createCapabilities() {
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        if (profile == null) {
            throw new IllegalStateException("Профиль GL2 (OpenGL 2.1) недоступен на этой системе");
        }
        GLCapabilities capabilities = new GLCapabilities(profile);
        capabilities.setDoubleBuffered(true);
        capabilities.setSampleBuffers(true);
        capabilities.setNumSamples(4);
        return capabilities;
    }

    public void renderFrame() {
        display();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glDisable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL2.GL_MULTISAMPLE);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        camera.initialize(width, height);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        int width = drawable.getSurfaceWidth();
        int height = drawable.getSurfaceHeight();
        if (width == 0 || height == 0) return;

        if (!camera.isInitialized()) {
            camera.initialize(width, height);
        }

        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glClearColor(0, 0, 0, 1);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, width, 0, height, -1, 1);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslated(camera.getOffsetX(), height - camera.getOffsetY(), 0);
        gl.glScaled(camera.getScale(), camera.getScale(), 1);
        gl.glLineWidth(1.0f);

        drawGridAndAxes(gl);
        drawWorld(gl);

        if (world.isPaused()) {
            gl.glLoadIdentity();
            drawPauseIndicator(gl, width, height);
        }
    }

    @Override
    public void paint(Graphics g) {
    }

    @Override
    public void update(Graphics g) {
    }

    private void drawGridAndAxes(GL2 gl) {
        double width = getWidth();
        double height = getHeight();
        if (width == 0 || height == 0) return;

        double scale = camera.getScale();
        double offsetX = camera.getOffsetX();
        double offsetY = camera.getOffsetY();

        double minWorldX = (0 - offsetX) / scale;
        double maxWorldX = (width - offsetX) / scale;
        double minWorldY = (offsetY - height) / scale;
        double maxWorldY = offsetY / scale;

        double spacing = UNITS_PER_METER * Math.pow(2, -Math.floor(Math.log(scale) / Math.log(2)));
        if (scale * spacing < 50) {
            spacing *= 2;
        }
        if (scale * spacing > 100) {
            spacing /= 2;
        }

        glColor(gl, new Color(40, 40, 40));
        long firstX = (long) Math.floor(minWorldX / spacing);
        long lastX = (long) Math.ceil(maxWorldX / spacing);
        for (long i = firstX; i <= lastX; i++) {
            drawLine(gl, i * spacing, minWorldY, i * spacing, maxWorldY);
        }
        long firstY = (long) Math.floor(minWorldY / spacing);
        long lastY = (long) Math.ceil(maxWorldY / spacing);
        for (long i = firstY; i <= lastY; i++) {
            drawLine(gl, minWorldX, i * spacing, maxWorldX, i * spacing);
        }

        glColor(gl, new Color(80, 80, 80));
        if (minWorldX <= 0 && 0 <= maxWorldX) {
            drawLine(gl, 0, minWorldY, 0, maxWorldY);
        }
        if (minWorldY <= 0 && 0 <= maxWorldY) {
            drawLine(gl, minWorldX, 0, maxWorldX, 0);
        }
    }

    private void drawWorld(GL2 gl) {
        for (Body body : world.getBodies()) {
            drawBody(gl, body);
        }
    }

    private void drawBody(GL2 gl, Body body) {
        Vector2D bodyPos = body.getPosition();
        double bodyX = bodyPos.x() * UNITS_PER_METER;
        double bodyY = bodyPos.y() * UNITS_PER_METER;

        if (renderAABBs) {
            AABB bodyAabb = body.getAabb();
            if (bodyAabb != null) {
                glColor(gl, new Color(220, 0, 220));
                drawAABB(gl, bodyAabb);
            }
        }

        for (Collider collider : body.getColliders()) {
            drawCollider(gl, collider, body.getAngle());
        }

        glColor(gl, Color.WHITE);
        drawFilledCircle(gl, bodyX, bodyY, 4.0 / camera.getScale(), 16);
    }

    private void drawCollider(GL2 gl, Collider collider, double bodyAngle) {
        if (renderAABBs) {
            AABB colliderAabb = collider.getAabb();
            if (colliderAabb != null) {
                glColor(gl, Color.GRAY);
                drawAABB(gl, colliderAabb);
            }
        }

        if (collider instanceof CircleCollider circle) {
            Vector2D bodyPos = circle.getOwner().getPosition();
            Vector2D globalPos = bodyPos.add(circle.getPosition().rotate(bodyAngle));
            double globalX = globalPos.x() * UNITS_PER_METER;
            double globalY = globalPos.y() * UNITS_PER_METER;
            double radius = circle.getRadius() * UNITS_PER_METER;

            glColor(gl, new Color(0, 180, 0));
            drawCircle(gl, globalX, globalY, radius);

            double totalAngle = bodyAngle + circle.getAngle();
            drawLine(gl, globalX, globalY, globalX + radius * Math.cos(totalAngle), globalY + radius * Math.sin(totalAngle));
        } else if (collider instanceof PolygonCollider polygon) {
            List<Vector2D> worldVertices = polygon.getWorldVertices();
            glColor(gl, new Color(0, 150, 255));
            gl.glBegin(GL2.GL_LINE_LOOP);
            for (Vector2D v : worldVertices) {
                gl.glVertex2d(v.x() * UNITS_PER_METER, v.y() * UNITS_PER_METER);
            }
            gl.glEnd();
        }
    }

    private void drawAABB(GL2 gl, AABB aabb) {
        double minX = aabb.min().x() * UNITS_PER_METER;
        double minY = aabb.min().y() * UNITS_PER_METER;
        double maxX = aabb.max().x() * UNITS_PER_METER;
        double maxY = aabb.max().y() * UNITS_PER_METER;
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex2d(minX, minY);
        gl.glVertex2d(maxX, minY);
        gl.glVertex2d(maxX, maxY);
        gl.glVertex2d(minX, maxY);
        gl.glEnd();
    }

    private void drawPauseIndicator(GL2 gl, int width, int height) {
        glColor(gl, new Color(0, 0, 0, 150));
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2d(0, 0);
        gl.glVertex2d(width, 0);
        gl.glVertex2d(width, height);
        gl.glVertex2d(0, height);
        gl.glEnd();

        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double barWidth = width / 40.0;
        double barHeight = height / 5.0;
        double gap = barWidth * 0.6;

        glColor(gl, Color.WHITE);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2d(centerX - gap - barWidth, centerY - barHeight / 2);
        gl.glVertex2d(centerX - gap, centerY - barHeight / 2);
        gl.glVertex2d(centerX - gap, centerY + barHeight / 2);
        gl.glVertex2d(centerX - gap - barWidth, centerY + barHeight / 2);

        gl.glVertex2d(centerX + gap, centerY - barHeight / 2);
        gl.glVertex2d(centerX + gap + barWidth, centerY - barHeight / 2);
        gl.glVertex2d(centerX + gap + barWidth, centerY + barHeight / 2);
        gl.glVertex2d(centerX + gap, centerY + barHeight / 2);
        gl.glEnd();
    }

    private void drawCircle(GL2 gl, double x, double y, double radius) {
        double screenRadius = Math.abs(radius * camera.getScale());
        int segments = (int) Math.min(256, Math.max(24, 2 * Math.PI * screenRadius / 6));
        gl.glBegin(GL2.GL_LINE_LOOP);
        for (int i = 0; i < segments; i++) {
            double a = 2 * Math.PI * i / segments;
            gl.glVertex2d(x + radius * Math.cos(a), y + radius * Math.sin(a));
        }
        gl.glEnd();
    }

    private void drawFilledCircle(GL2 gl, double x, double y, double radius, int segments) {
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex2d(x, y);
        for (int i = 0; i <= segments; i++) {
            double a = 2 * Math.PI * i / segments;
            gl.glVertex2d(x + radius * Math.cos(a), y + radius * Math.sin(a));
        }
        gl.glEnd();
    }

    private void drawLine(GL2 gl, double x1, double y1, double x2, double y2) {
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex2d(x1, y1);
        gl.glVertex2d(x2, y2);
        gl.glEnd();
    }

    private void glColor(GL2 gl, Color color) {
        gl.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
    }
}
