package dev.vesly.windchargeresetfolia;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class CooldownManager {
    private final ConcurrentHashMap<UUID, Long> expirations = new ConcurrentHashMap<>();

    boolean isOnCooldown(UUID playerId, long nowMillis) {
        Long expiresAt = expirations.get(playerId);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt <= nowMillis) {
            expirations.remove(playerId, expiresAt);
            return false;
        }
        return true;
    }

    void apply(UUID playerId, long nowMillis, long cooldownMillis) {
        if (cooldownMillis <= 0L) {
            expirations.remove(playerId);
            return;
        }
        expirations.put(playerId, nowMillis + cooldownMillis);
    }

    void remove(UUID playerId) {
        expirations.remove(playerId);
    }

    void clear() {
        expirations.clear();
    }

    int entryCount() {
        return expirations.size();
    }
}
