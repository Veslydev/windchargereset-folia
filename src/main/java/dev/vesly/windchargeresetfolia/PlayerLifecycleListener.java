package dev.vesly.windchargeresetfolia;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

final class PlayerLifecycleListener implements Listener {
    private final CooldownManager cooldownManager;
    private final MomentumManager momentumManager;

    PlayerLifecycleListener(CooldownManager cooldownManager, MomentumManager momentumManager) {
        this.cooldownManager = cooldownManager;
        this.momentumManager = momentumManager;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            momentumManager.restoreAfterEnderPearl(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cooldownManager.remove(event.getPlayer().getUniqueId());
        momentumManager.remove(event.getPlayer().getUniqueId());
    }
}
