package io.github.shogeo.phyjine.renderer;

import io.github.shogeo.phyjine.core.Transform;
import io.github.shogeo.phyjine.core.Vector2;

public class Camera {
    public final double width;
    public final double height;
    Transform transform = new Transform(new Vector2(0, 0), new Transform.Rotation());

    public Camera(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public Vector2 globalPosToWindowPos(Transform globalPos) {
        double halfWidth = width / 2;
        double halfHeight = height / 2;

        Vector2 relativePos = this.transform.position.relative(globalPos.position);

        double cameraAngle = this.transform.rotation.angle;
        double rotatedX = relativePos.x * Math.cos(-cameraAngle) - relativePos.y * Math.sin(-cameraAngle);
        double rotatedY = relativePos.x * Math.sin(-cameraAngle) + relativePos.y * Math.cos(-cameraAngle);

        double windowX = rotatedX + halfWidth;
        double windowY = halfHeight - rotatedY;

        return new Vector2(windowX, windowY);
    }
}
