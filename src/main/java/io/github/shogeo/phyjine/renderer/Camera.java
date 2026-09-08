package io.github.shogeo.phyjine.renderer;

import java.awt.Point;

public class Camera {

    private double offsetX = 0;
    private double offsetY = 0;
    private double scale = 1.0;
    private boolean initialized = false;

    public synchronized void initialize(int screenWidth, int screenHeight) {
        if (!initialized) {
            this.offsetX = screenWidth / 2.0;
            this.offsetY = screenHeight / 2.0;
            this.initialized = true;
        }
    }

    public synchronized void pan(double dx, double dy) {
        this.offsetX += dx;
        this.offsetY += dy;
    }

    public synchronized void zoom(double zoomFactor, Point mousePoint) {
        double oldScale = this.scale;
        double worldX = (mousePoint.x - this.offsetX) / oldScale;
        double worldY = (mousePoint.y - this.offsetY) / -oldScale;

        this.scale *= zoomFactor;

        this.offsetX = mousePoint.x - worldX * this.scale;
        this.offsetY = mousePoint.y - worldY * -this.scale;
    }

    public synchronized double getOffsetX() {
        return offsetX;
    }

    public synchronized double getOffsetY() {
        return offsetY;
    }

    public synchronized double getScale() {
        return scale;
    }

    public synchronized boolean isInitialized() {
        return initialized;
    }
}
