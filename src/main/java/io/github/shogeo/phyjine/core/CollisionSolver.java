package io.github.shogeo.phyjine.core;

import io.github.shogeo.phyjine.core.utils.CollisionManifold;
import io.github.shogeo.phyjine.core.utils.Vector2D;

import java.util.List;

public class CollisionSolver {
    public void solve(List<CollisionManifold> collisions) {
        for (CollisionManifold m : collisions) {
            resolvePositions(m);
        }
        for (int i = 0; i < 1; i++) {
            for (CollisionManifold m : collisions) {
                resolveVelocities(m);
            }
        }
    }

    private void resolvePositions(CollisionManifold c) {
        Body body1 = c.a().getOwner();
        Body body2 = c.b().getOwner();

        double totalInvMass = body1.getInvMass() + body2.getInvMass();
        if (totalInvMass <= 0.0) return;

        double magnitude = Math.max(c.penetration(), 0.0) / totalInvMass;
        Vector2D separationVector = c.normal().multiply(magnitude);

        body1.setPosition(body1.getPosition().subtract(separationVector.multiply(body1.getInvMass())));
        body2.setPosition(body2.getPosition().add(separationVector.multiply(body2.getInvMass())));
    }

    private void resolveVelocities(CollisionManifold c) {
        Body body1 = c.a().getOwner();
        Body body2 = c.b().getOwner();

        double totalInvMass = body1.getInvMass() + body2.getInvMass();

        double combinedStaticFriction = Math.sqrt(c.a().getMaterial().staticFriction() * c.b().getMaterial().staticFriction());
        double combinedKineticFriction = Math.sqrt(c.a().getMaterial().kineticFriction() * c.b().getMaterial().kineticFriction());
        double combinedRestitution = Math.sqrt(c.a().getMaterial().restitution() * c.b().getMaterial().restitution());

        Vector2D leverArm1 = c.contactPoint().subtract(body1.getPosition());
        Vector2D leverArm2 = c.contactPoint().subtract(body2.getPosition());

        Vector2D contactVelocity1 = body1.getVelocity().add(new Vector2D(-body1.getAngularVelocity() * leverArm1.y(), body1.getAngularVelocity() * leverArm1.x()));
        Vector2D contactVelocity2 = body2.getVelocity().add(new Vector2D(-body2.getAngularVelocity() * leverArm2.y(), body2.getAngularVelocity() * leverArm2.x()));

        Vector2D relVelocity = contactVelocity2.subtract(contactVelocity1);

        double normalVelocity = relVelocity.dot(c.normal());

        if (normalVelocity > 0.0) return;

        double normalTorqueArm1 = leverArm1.cross(c.normal());
        double normalTorqueArm2 = leverArm2.cross(c.normal());

        double normalEffectiveMass = totalInvMass + (normalTorqueArm1 * normalTorqueArm1) * body1.getInverseMomentOfInertia() + (normalTorqueArm2 * normalTorqueArm2) * body2.getInverseMomentOfInertia();

        double impulse = -(1.0 + combinedRestitution) * normalVelocity / normalEffectiveMass;

        Vector2D normalImpulse = c.normal().multiply(impulse);

        body1.setVelocity(body1.getVelocity().subtract(normalImpulse.multiply(body1.getInvMass())));
        body1.setAngularVelocity(body1.getAngularVelocity() - normalTorqueArm1 * impulse * body1.getInverseMomentOfInertia());
        body2.setVelocity(body2.getVelocity().add(normalImpulse.multiply(body2.getInvMass())));
        body2.setAngularVelocity(body2.getAngularVelocity() + normalTorqueArm2 * impulse * body2.getInverseMomentOfInertia());

        contactVelocity1 = body1.getVelocity().add(new Vector2D(-body1.getAngularVelocity() * leverArm1.y(), body1.getAngularVelocity() * leverArm1.x()));
        contactVelocity2 = body2.getVelocity().add(new Vector2D(-body2.getAngularVelocity() * leverArm2.y(), body2.getAngularVelocity() * leverArm2.x()));
        relVelocity = contactVelocity2.subtract(contactVelocity1);

        Vector2D tangent = new Vector2D(-c.normal().y(), c.normal().x());

        double tangentVelocity = relVelocity.dot(tangent);

        double tangentTorqueArm1 = leverArm1.cross(tangent);
        double tangentTorqueArm2 = leverArm2.cross(tangent);

        double tangentEffectiveMass = totalInvMass + (tangentTorqueArm1 * tangentTorqueArm1) * body1.getInverseMomentOfInertia() + (tangentTorqueArm2 * tangentTorqueArm2) * body2.getInverseMomentOfInertia();
        if (tangentEffectiveMass <= 0.0) return;

        double stoppingImpulse = -tangentVelocity / tangentEffectiveMass;

        double maxFrictionImpulse = impulse * combinedStaticFriction;
        if (Math.abs(stoppingImpulse) > maxFrictionImpulse) {
            stoppingImpulse = Math.signum(stoppingImpulse) * impulse * combinedKineticFriction;
        }

        Vector2D frictionImpulse = tangent.multiply(stoppingImpulse);

        body1.setVelocity(body1.getVelocity().subtract(frictionImpulse.multiply(body1.getInvMass())));
        body1.setAngularVelocity(body1.getAngularVelocity() - tangentTorqueArm1 * stoppingImpulse * body1.getInverseMomentOfInertia());

        body2.setVelocity(body2.getVelocity().add(frictionImpulse.multiply(body2.getInvMass())));
        body2.setAngularVelocity(body2.getAngularVelocity() + tangentTorqueArm2 * stoppingImpulse * body2.getInverseMomentOfInertia());
    }
}