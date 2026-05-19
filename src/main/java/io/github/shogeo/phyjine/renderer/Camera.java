package io.github.shogeo.phyjine.renderer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class Camera {

    private double offsetX = 0;
    private double offsetY = 0;
    private double scale = 1.0;
    private boolean initialized = false;
    private int screenWidth;
    private int screenHeight;

    public void initialize(int screenWidth, int screenHeight) {
        if (!initialized) {
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.offsetX = screenWidth / 2.0;
            this.offsetY = screenHeight / 2.0;
            this.initialized = true;
        }
    }

    public void pan(double dx, double dy) {
        this.offsetX += dx;
        this.offsetY += dy;
    }

    public void zoom(double zoomFactor, Point mousePoint) {
        // Translate the mouse point to world coordinates before zoom
        double oldScale = this.scale;
        double worldX = (mousePoint.x - this.offsetX) / oldScale;
        double worldY = (mousePoint.y - this.offsetY) / -oldScale; // Y is inverted

        this.scale *= zoomFactor;

        // After zoom, the world coordinates under the mouse should be the same.
        // newOffsetX = mousePoint.x - worldX * newScale
        // newOffsetY = mousePoint.y - worldY * -newScale
        this.offsetX = mousePoint.x - worldX * this.scale;
        this.offsetY = mousePoint.y - worldY * -this.scale;
    }

    public AffineTransform getTransform() {
        AffineTransform tx = new AffineTransform();
        tx.translate(offsetX, offsetY);
        tx.scale(scale, -scale); // Invert Y for physics coordinates
        return tx;
    }

    public double getScale() {
        return scale;
    }

    public boolean isInitialized() {
        return initialized;
    }
}