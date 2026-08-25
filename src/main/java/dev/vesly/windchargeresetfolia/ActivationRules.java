package dev.vesly.windchargeresetfolia;

final class ActivationRules {
    private ActivationRules() {
    }

    static boolean isFalling(double verticalVelocity) {
        return verticalVelocity < 0.0D;
    }

    static boolean meetsMinimumFallVelocity(double verticalVelocity, double minimumFallVelocity) {
        return verticalVelocity <= minimumFallVelocity;
    }

    static boolean isAtOrBelowMinimumGroundDistance(double playerMinY, double blockMaxY, double minimumGroundDistance) {
        return playerMinY - blockMaxY <= minimumGroundDistance;
    }
}
