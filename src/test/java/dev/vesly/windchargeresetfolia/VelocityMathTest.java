package dev.vesly.windchargeresetfolia;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VelocityMathTest {
    @Test
    void appliesDefaultWindChargeBrake() {
        Vector3 result = VelocityMath.brake(new Vector3(1.0D, -0.4D, -0.5D), 0.25D, 0.12D, 0.45D, 0.85D);

        assertEquals(0.85D, result.x());
        assertEquals(0.12D, result.y());
        assertEquals(-0.425D, result.z());
    }

    @Test
    void clampsVerticalVelocityAtBothBounds() {
        Vector3 lower = VelocityMath.brake(new Vector3(0.0D, -4.0D, 0.0D), 0.25D, 0.12D, 0.45D, 0.85D);
        Vector3 upper = VelocityMath.brake(new Vector3(0.0D, 4.0D, 0.0D), 0.25D, 0.12D, 0.45D, 0.85D);

        assertEquals(0.12D, lower.y());
        assertEquals(0.45D, upper.y());
    }
}
