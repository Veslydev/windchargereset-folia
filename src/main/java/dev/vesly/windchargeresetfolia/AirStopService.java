package dev.vesly.windchargeresetfolia;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

final class AirStopService {
    private static final double FOOTPRINT_INSET = 0.02D;
    private static final double SUPPORT_SCAN_BELOW = 0.08D;
    private static final double SUPPORT_SCAN_ABOVE = 0.02D;

    private final ConfigManager configManager;
    private final CooldownManager cooldownManager;
    private final MomentumManager momentumManager;

    AirStopService(ConfigManager configManager, CooldownManager cooldownManager, MomentumManager momentumManager) {
        this.configManager = configManager;
        this.cooldownManager = cooldownManager;
        this.momentumManager = momentumManager;
    }

    Settings settings() {
        return configManager.settings();
    }

    boolean canActivate(Player player, Settings settings) {
        if (!settings.enabled() || !hasUsePermission(player)) {
            return false;
        }

        BoundingBox playerBox = player.getBoundingBox();
        if ((settings.requireAirborne() || settings.minGroundDistance() > 0.0D)
                && !ownsFootprint(player.getWorld(), playerBox)) {
            return false;
        }
        if (settings.requireAirborne() && hasSupportingBlock(player.getWorld(), playerBox)) {
            return false;
        }
        if (isAtOrBelowMinimumGroundDistance(player.getWorld(), playerBox, settings.minGroundDistance())) {
            return false;
        }

        Vector velocity = player.getVelocity();
        if (settings.requireFalling() && !ActivationRules.isFalling(velocity.getY())) {
            return false;
        }
        if (!ActivationRules.meetsMinimumFallVelocity(velocity.getY(), settings.minFallVelocity())) {
            return false;
        }
        if (player.getLocation().getPitch() < settings.minPitchDown()) {
            return false;
        }

        return !cooldownManager.isOnCooldown(player.getUniqueId(), System.currentTimeMillis());
    }

    void activate(Player player, Settings settings) {
        momentumManager.capture(player, settings);

        Vector velocity = player.getVelocity();
        Vector3 adjusted = VelocityMath.brake(
                new Vector3(velocity.getX(), velocity.getY(), velocity.getZ()),
                settings.verticalBrakeMultiplier(),
                settings.minUpwardVelocity(),
                settings.maxUpwardVelocity(),
                settings.horizontalMultiplier()
        );
        player.setVelocity(new Vector(adjusted.x(), adjusted.y(), adjusted.z()));
        player.setFallDistance(0.0F);

        if (settings.soundEnabled()) {
            player.getWorld().playSound(
                    player.getLocation(),
                    Sound.valueOf(settings.soundTypeName()),
                    settings.soundVolume(),
                    settings.soundPitch()
            );
        }
        cooldownManager.apply(player.getUniqueId(), System.currentTimeMillis(), settings.cooldownMillis());
    }

    void consumeOne(Player player, ItemStack launchedStack) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (matchesLaunchedStack(mainHand, launchedStack)) {
            consumeOne(mainHand);
        } else if (matchesLaunchedStack(offHand, launchedStack)) {
            consumeOne(offHand);
        }
    }

    private boolean hasUsePermission(Player player) {
        return player.hasPermission("windchargereset.use") || player.hasPermission("windchargestop.use");
    }

    private boolean ownsFootprint(World world, BoundingBox playerBox) {
        BoundingBox footprint = horizontalFootprint(playerBox);
        int minChunkX = Math.floorDiv(floor(footprint.getMinX()), 16);
        int maxChunkX = Math.floorDiv(floor(footprint.getMaxX()), 16);
        int minChunkZ = Math.floorDiv(floor(footprint.getMinZ()), 16);
        int maxChunkZ = Math.floorDiv(floor(footprint.getMaxZ()), 16);

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!Bukkit.isOwnedByCurrentRegion(world, chunkX, chunkZ)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasSupportingBlock(World world, BoundingBox playerBox) {
        BoundingBox supportBox = new BoundingBox(
                playerBox.getMinX() + FOOTPRINT_INSET,
                playerBox.getMinY() - SUPPORT_SCAN_BELOW,
                playerBox.getMinZ() + FOOTPRINT_INSET,
                playerBox.getMaxX() - FOOTPRINT_INSET,
                playerBox.getMinY() + SUPPORT_SCAN_ABOVE,
                playerBox.getMaxZ() - FOOTPRINT_INSET
        );
        for (int x = floor(supportBox.getMinX()); x <= floor(supportBox.getMaxX()); x++) {
            for (int y = floor(supportBox.getMinY()); y <= floor(supportBox.getMaxY()); y++) {
                for (int z = floor(supportBox.getMinZ()); z <= floor(supportBox.getMaxZ()); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (!block.isPassable() && block.getBoundingBox().overlaps(supportBox)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isAtOrBelowMinimumGroundDistance(World world, BoundingBox playerBox, double minimumGroundDistance) {
        if (minimumGroundDistance <= 0.0D) {
            return false;
        }

        BoundingBox footprint = horizontalFootprint(playerBox);
        int lowestY = Math.max(world.getMinHeight(), floor(playerBox.getMinY() - minimumGroundDistance - 1.0D));
        for (int y = floor(playerBox.getMinY()); y >= lowestY; y--) {
            for (int x = floor(footprint.getMinX()); x <= floor(footprint.getMaxX()); x++) {
                for (int z = floor(footprint.getMinZ()); z <= floor(footprint.getMaxZ()); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (!block.isPassable()
                            && overlapsHorizontally(block.getBoundingBox(), footprint)
                            && ActivationRules.isAtOrBelowMinimumGroundDistance(
                            playerBox.getMinY(), block.getBoundingBox().getMaxY(), minimumGroundDistance)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static BoundingBox horizontalFootprint(BoundingBox playerBox) {
        return new BoundingBox(
                playerBox.getMinX() + FOOTPRINT_INSET,
                playerBox.getMinY(),
                playerBox.getMinZ() + FOOTPRINT_INSET,
                playerBox.getMaxX() - FOOTPRINT_INSET,
                playerBox.getMaxY(),
                playerBox.getMaxZ() - FOOTPRINT_INSET
        );
    }

    private static boolean overlapsHorizontally(BoundingBox first, BoundingBox second) {
        return first.getMaxX() > second.getMinX()
                && first.getMinX() < second.getMaxX()
                && first.getMaxZ() > second.getMinZ()
                && first.getMinZ() < second.getMaxZ();
    }

    private static boolean matchesLaunchedStack(ItemStack handStack, ItemStack launchedStack) {
        return handStack == launchedStack || (handStack != null && launchedStack != null && handStack.isSimilar(launchedStack));
    }

    private static void consumeOne(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return;
        }
        stack.setAmount(Math.max(0, stack.getAmount() - 1));
    }

    private static int floor(double value) {
        return (int) Math.floor(value);
    }
}
