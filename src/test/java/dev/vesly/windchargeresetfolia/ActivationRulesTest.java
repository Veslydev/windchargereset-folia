package dev.vesly.windchargeresetfolia;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivationRulesTest {
    @Test
    void minimumFallVelocityIncludesExactBoundary() {
        assertTrue(ActivationRules.meetsMinimumFallVelocity(-0.25D, -0.25D));
        assertTrue(ActivationRules.meetsMinimumFallVelocity(-0.251D, -0.25D));
        assertFalse(ActivationRules.meetsMinimumFallVelocity(-0.249D, -0.25D));
    }

    @Test
    void fallingRequirementRejectsZeroAndUpwardMotion() {
        assertTrue(ActivationRules.isFalling(-0.001D));
        assertFalse(ActivationRules.isFalling(0.0D));
        assertFalse(ActivationRules.isFalling(0.001D));
    }

    @Test
    void groundDistanceIncludesExactBoundary() {
        assertTrue(ActivationRules.isAtOrBelowMinimumGroundDistance(20.0D, 10.0D, 10.0D));
        assertFalse(ActivationRules.isAtOrBelowMinimumGroundDistance(21.0D, 10.0D, 10.0D));
    }
}
