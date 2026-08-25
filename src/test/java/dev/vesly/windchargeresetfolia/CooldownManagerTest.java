package dev.vesly.windchargeresetfolia;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CooldownManagerTest {
    @Test
    void tracksActiveAndExpiredCooldownsAtTheExactBoundary() {
        CooldownManager cooldowns = new CooldownManager();
        UUID playerId = UUID.randomUUID();
        cooldowns.apply(playerId, 1_000L, 400L);

        assertTrue(cooldowns.isOnCooldown(playerId, 1_399L));
        assertFalse(cooldowns.isOnCooldown(playerId, 1_400L));
        assertEquals(0, cooldowns.entryCount());
    }

    @Test
    void zeroCooldownLeavesNoEntry() {
        CooldownManager cooldowns = new CooldownManager();
        UUID playerId = UUID.randomUUID();
        cooldowns.apply(playerId, 1_000L, 0L);

        assertFalse(cooldowns.isOnCooldown(playerId, 1_000L));
        assertEquals(0, cooldowns.entryCount());
    }
}
