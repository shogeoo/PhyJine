package io.github.shogeo.phyjine.core;

import io.github.shogeo.phyjine.core.utils.CollisionManifold;
import io.github.shogeo.phyjine.core.utils.Vector2D;

import java.util.List;

public class CollisionSolver {
    public void solve(List<CollisionManifold> collisions) {
        for (CollisionManifold m : collisions) {
            resolvePositions(m);
        }
        for (CollisionManifold m : collisions) {
            resolveVelocities(m);
        }
    }

    private void resolvePositions(CollisionManifold c) {
        Body bodyA = c.a().getOwner();
        Body bodyB = c.b().getOwner();

        double invMassA = bodyA.getInvMass();
        double invMassB = bodyB.getInvMass();

        double totalInvMass = invMassA + invMassB;

        if (totalInvMass <= 0.0) {
            return;
        }

        if (c.penetration() == 0.0) {
            return;
        }

        double magnitude = c.penetration() / totalInvMass;

        Vector2D separationVector = c.normal().multiply(magnitude);

        if (invMassA > 0.0) {
            Vector2D newPosA = bodyA.getPosition().subtract(separationVector.multiply(invMassA));
            bodyA.setPosition(newPosA);
        }

        if (invMassB > 0.0) {
            Vector2D newPosB = bodyB.getPosition().add(separationVector.multiply(invMassB));
            bodyB.setPosition(newPosB);
        }
    }

    private void resolveVelocities(CollisionManifold c) {
    }
}
