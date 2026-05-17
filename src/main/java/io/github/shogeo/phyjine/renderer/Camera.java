package io.github.shogeo.phyjine.renderer;

import io.github.shogeo.phyjine.utils.Vector2D;

import java.awt.*;

public class Camera {

    public static final double UNITS_PER_METER = 100.0;

    private Vector2D position;
    private double zoom;

    public Camera() {
        this.position = new Vector2D(0, 0);
        this.zoom = 0.5;
    }

    public Vector2D getPosition() {
        return position;
    }

    public double getZoom() {
        return zoom;
    }

    public Point worldToScreen(Vector2D world, int screenW, int screenH) {
        double scale = UNITS_PER_METER * zoom;
        int sx = (int) Math.round((world.x() - position.x()) * scale + screenW / 2.0);
        int sy = (int) Math.round(-(world.y() - position.y()) * scale + screenH / 2.0);
        return new Point(sx, sy);
    }

    public Vector2D screenToWorld(Point screen, int screenW, int screenH) {
        double scale = UNITS_PER_METER * zoom;
        double wx = (screen.x - screenW / 2.0) / scale + position.x();
        double wy = -(screen.y - screenH / 2.0) / scale + position.y();
        return new Vector2D(wx, wy);
    }

    public void pan(int screenDeltaX, int screenDeltaY, int screenW, int screenH) {
        double scale = UNITS_PER_METER * zoom;
        double dx = screenDeltaX / scale;
        double dy = -screenDeltaY / scale;
        position = new Vector2D(position.x() - dx, position.y() - dy);
    }

    public void zoom(double factor, Vector2D cursorWorld) {
        double oldZoom = zoom;
        zoom = Math.clamp(zoom * factor, 0.01, 100.0);
        double ratio = oldZoom / zoom;
        double newX = cursorWorld.x() - (cursorWorld.x() - position.x()) * ratio;
        double newY = cursorWorld.y() - (cursorWorld.y() - position.y()) * ratio;
        position = new Vector2D(newX, newY);
    }
}
