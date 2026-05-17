package io.github.shogeo.phyjine.renderer;

import io.github.shogeo.phyjine.core.Body;
import io.github.shogeo.phyjine.core.utils.Vector2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

public class RenderWindow {

    private final JFrame frame;
    private final RenderPanel panel;
    private final Camera camera;

    private Point lastMousePos;

    public RenderWindow(String title, Camera camera) {
        this.camera = camera;
        this.panel = new RenderPanel(camera);

        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.add(panel);

        setupMouseListeners();
    }

    public void show() {
        frame.setVisible(true);
    }

    public void render(List<Body> bodies) {
        panel.setBodies(bodies);
        panel.repaint();
    }

    private void setupMouseListeners() {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    lastMousePos = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    lastMousePos = null;
                }
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePos != null && SwingUtilities.isLeftMouseButton(e)) {
                    int dx = e.getX() - lastMousePos.x;
                    int dy = e.getY() - lastMousePos.y;
                    camera.pan(dx, dy, panel.getWidth(), panel.getHeight());
                    lastMousePos = e.getPoint();
                }
            }
        });

        panel.addMouseWheelListener(e -> {
            Vector2D cursorWorld = camera.screenToWorld(new Point(e.getX(), e.getY()), panel.getWidth(), panel.getHeight());
            double factor = e.getWheelRotation() < 0 ? 1.1 : 0.9;
            camera.zoom(factor, cursorWorld);
        });
    }
}
