package dev.vesly.windchargeresetfolia;

import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;

record Settings(
        boolean enabled,
        boolean requireAirborne,
        boolean requireFalling,
        double minFallVelocity,
        double minPitchDown,
        double minGroundDistance,
        long cooldownMillis,
        boolean consumeWindCharge,
        boolean cancelProjectile,
        double verticalBrakeMultiplier,
        double minUpwardVelocity,
        double maxUpwardVelocity,
        double horizontalMultiplier,
        boolean soundEnabled,
        String soundTypeName,
        float soundVolume,
        float soundPitch,
        boolean momentumCarryover,
        long momentumTimeoutMillis,
        boolean carryHorizontalMomentum,
        boolean carryVerticalMomentum,
        boolean carryFallDistance,
        String reloadMessage,
        String noPermissionMessage
) {
    static final String DEFAULT_SOUND = "ENTITY_WIND_CHARGE_WIND_BURST";

    static Settings read(FileConfiguration configuration, Consumer<String> warningLogger, Predicate<String> knownSound) {
        double minUpwardVelocity = clamp(configuration.getDouble("effect.min_upward_velocity", 0.12D), -0.2D, 1.0D);
        double maxUpwardVelocity = clamp(configuration.getDouble("effect.max_upward_velocity", 0.45D), minUpwardVelocity, 1.0D);

        return new Settings(
                configuration.getBoolean("enabled", true),
                configuration.getBoolean("activation.require_airborne", true),
                configuration.getBoolean("activation.require_falling", true),
                configuration.getDouble("activation.min_fall_velocity", -0.25D),
                clamp(configuration.getDouble("activation.min_pitch_down", 55.0D), -90.0D, 90.0D),
                Math.max(0.0D, configuration.getDouble("activation.min_ground_distance", 10.0D)),
                Math.max(0L, configuration.getLong("activation.cooldown_ms", 400L)),
                configuration.getBoolean("effect.consume_wind_charge", true),
                configuration.getBoolean("effect.cancel_projectile", true),
                clamp(configuration.getDouble("effect.vertical_brake_multiplier", 0.25D), 0.0D, 1.0D),
                minUpwardVelocity,
                maxUpwardVelocity,
                clamp(configuration.getDouble("effect.horizontal_multiplier", 0.85D), 0.0D, 1.0D),
                configuration.getBoolean("sound.enabled", true),
                parseSoundName(configuration.getString("sound.type"), warningLogger, knownSound),
                Math.max(0.0F, (float) configuration.getDouble("sound.volume", 1.0D)),
                Math.max(0.0F, (float) configuration.getDouble("sound.pitch", 0.67D)),
                configuration.getBoolean("momentum_carryover.enabled", false),
                Math.max(0L, configuration.getLong("momentum_carryover.timeout_ms", 10_000L)),
                configuration.getBoolean("momentum_carryover.horizontal", true),
                configuration.getBoolean("momentum_carryover.vertical", true),
                configuration.getBoolean("momentum_carryover.fall_distance", true),
                color(configuration.getString("messages.reload", "&aWindCharge Reset config reloaded.")),
                color(configuration.getString("messages.no_permission", "&cYou do not have permission."))
        );
    }

    static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    static boolean isBukkitSound(String soundName) {
        try {
            Sound.valueOf(soundName);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static String parseSoundName(String configuredName, Consumer<String> warningLogger, Predicate<String> knownSound) {
        if (configuredName == null || configuredName.isBlank()) {
            return DEFAULT_SOUND;
        }

        String normalized = configuredName.toUpperCase(Locale.ROOT);
        if (!knownSound.test(normalized)) {
            warningLogger.accept("Unknown sound '" + configuredName + "'; using " + DEFAULT_SOUND + " instead.");
            return DEFAULT_SOUND;
        }
        return normalized;
    }

    private static String color(String message) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message == null ? "" : message);
    }
}
