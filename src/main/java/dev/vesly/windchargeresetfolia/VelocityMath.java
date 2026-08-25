package dev.vesly.windchargeresetfolia;

final class VelocityMath {
    private VelocityMath() {
    }

    static Vector3 brake(
            Vector3 velocity,
            double verticalBrakeMultiplier,
            double minimumUpwardVelocity,
            double maximumUpwardVelocity,
            double horizontalMultiplier
    ) {
        double vertical = velocity.y() * verticalBrakeMultiplier;
        vertical = Math.max(vertical, minimumUpwardVelocity);
        vertical = Math.min(vertical, maximumUpwardVelocity);
        return new Vector3(velocity.x() * horizontalMultiplier, vertical, velocity.z() * horizontalMultiplier);
    }

    static Vector3 mergeMomentum(Vector3 current, Vector3 stored, boolean horizontal, boolean vertical) {
        return new Vector3(
                horizontal ? stored.x() : current.x(),
                vertical ? stored.y() : current.y(),
                horizontal ? stored.z() : current.z()
        );
    }

    static float mergeFallDistance(float current, float stored, boolean carryFallDistance) {
        return carryFallDistance ? stored : current;
    }
}
