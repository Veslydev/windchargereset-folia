package dev.vesly.windchargeresetfolia;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class MomentumManager {
    private final Plugin plugin;
    private final ConfigManager configManager;
    private final ConcurrentHashMap<UUID, StoredMomentum> storedMomentum = new ConcurrentHashMap<>();

    MomentumManager(Plugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    void capture(Player player, Settings settings) {
        UUID playerId = player.getUniqueId();
        if (!settings.momentumCarryover()) {
            storedMomentum.remove(playerId);
            return;
        }

        Vector velocity = player.getVelocity();
        storedMomentum.put(playerId, new StoredMomentum(
                new Vector3(velocity.getX(), velocity.getY(), velocity.getZ()),
                player.getFallDistance(),
                System.currentTimeMillis() + settings.momentumTimeoutMillis()
        ));
    }

    void restoreAfterEnderPearl(Player player) {
        StoredMomentum stored = storedMomentum.remove(player.getUniqueId());
        Settings settings = configManager.settings();
        if (!settings.momentumCarryover() || stored == null || stored.expiresAtMillis() < System.currentTimeMillis()) {
            return;
        }

        player.getScheduler().run(plugin, scheduledTask -> {
            if (!player.isOnline()) {
                return;
            }

            Vector current = player.getVelocity();
            Vector3 merged = VelocityMath.mergeMomentum(
                    new Vector3(current.getX(), current.getY(), current.getZ()),
                    stored.velocity(),
                    settings.carryHorizontalMomentum(),
                    settings.carryVerticalMomentum()
            );
            player.setVelocity(new Vector(merged.x(), merged.y(), merged.z()));
            float restoredFallDistance = VelocityMath.mergeFallDistance(
                    player.getFallDistance(),
                    stored.fallDistance(),
                    settings.carryFallDistance()
            );
            if (settings.carryFallDistance()) {
                player.setFallDistance(restoredFallDistance);
            }
        }, () -> {
            // The player entity retired before its next owned tick.
        });
    }

    void remove(UUID playerId) {
        storedMomentum.remove(playerId);
    }

    void clear() {
        storedMomentum.clear();
    }

    record StoredMomentum(Vector3 velocity, float fallDistance, long expiresAtMillis) {
    }
}
