package dev.vesly.windchargeresetfolia;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MomentumMathTest {
    private static final Vector3 CURRENT = new Vector3(1.0D, 2.0D, 3.0D);
    private static final Vector3 STORED = new Vector3(-1.0D, -2.0D, -3.0D);

    @Test
    void mergesEveryCarryoverToggleCombination() {
        for (boolean horizontal : new boolean[]{false, true}) {
            for (boolean vertical : new boolean[]{false, true}) {
                for (boolean fallDistance : new boolean[]{false, true}) {
                    Vector3 merged = VelocityMath.mergeMomentum(CURRENT, STORED, horizontal, vertical);

                    assertVector(
                            merged,
                            horizontal ? -1.0D : 1.0D,
                            vertical ? -2.0D : 2.0D,
                            horizontal ? -3.0D : 3.0D
                    );
                    assertEquals(fallDistance ? 12.5F : 4.0F,
                            VelocityMath.mergeFallDistance(4.0F, 12.5F, fallDistance));
                }
            }
        }
    }

    private static void assertVector(Vector3 vector, double x, double y, double z) {
        assertEquals(x, vector.x());
        assertEquals(y, vector.y());
        assertEquals(z, vector.z());
    }
}
