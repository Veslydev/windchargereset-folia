package dev.vesly.windchargeresetfolia;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import org.bukkit.Material;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

final class WindChargeListener implements Listener {
    private final AirStopService airStopService;

    WindChargeListener(AirStopService airStopService) {
        this.airStopService = airStopService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerLaunchProjectile(PlayerLaunchProjectileEvent event) {
        if (!(event.getProjectile() instanceof WindCharge windCharge)
                || event.getItemStack().getType() != Material.WIND_CHARGE) {
            return;
        }

        var player = event.getPlayer();
        Settings settings = airStopService.settings();
        if (!airStopService.canActivate(player, settings)) {
            return;
        }
        airStopService.activate(player, settings);
        if (settings.cancelProjectile()) {
            event.setShouldConsume(false);
            event.setCancelled(true);
            windCharge.remove();
            if (settings.consumeWindCharge()) {
                airStopService.consumeOne(player, event.getItemStack());
            }
        } else {
            event.setShouldConsume(settings.consumeWindCharge());
        }
    }
}
