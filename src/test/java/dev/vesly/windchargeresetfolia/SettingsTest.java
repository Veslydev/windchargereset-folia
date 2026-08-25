package dev.vesly.windchargeresetfolia;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SettingsTest {
    @Test
    void clampsConfiguredBounds() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("activation.min_pitch_down", 120.0D);
        config.set("activation.min_ground_distance", -5.0D);
        config.set("activation.cooldown_ms", -1L);
        config.set("effect.vertical_brake_multiplier", 2.0D);
        config.set("effect.min_upward_velocity", -5.0D);
        config.set("effect.max_upward_velocity", -1.0D);
        config.set("effect.horizontal_multiplier", -1.0D);
        config.set("momentum_carryover.timeout_ms", -1L);

        Settings settings = Settings.read(config, ignored -> {
        }, ignored -> true);

        assertEquals(90.0D, settings.minPitchDown());
        assertEquals(0.0D, settings.minGroundDistance());
        assertEquals(0L, settings.cooldownMillis());
        assertEquals(1.0D, settings.verticalBrakeMultiplier());
        assertEquals(-0.2D, settings.minUpwardVelocity());
        assertEquals(-0.2D, settings.maxUpwardVelocity());
        assertEquals(0.0D, settings.horizontalMultiplier());
        assertEquals(0L, settings.momentumTimeoutMillis());
    }

    @Test
    void clampsPitchAtLowerBoundAndMaximumUpwardAtOne() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("activation.min_pitch_down", -120.0D);
        config.set("effect.min_upward_velocity", 2.0D);
        config.set("effect.max_upward_velocity", 4.0D);
        config.set("effect.horizontal_multiplier", 2.0D);

        Settings settings = Settings.read(config, ignored -> {
        }, ignored -> true);

        assertEquals(-90.0D, settings.minPitchDown());
        assertEquals(1.0D, settings.minUpwardVelocity());
        assertEquals(1.0D, settings.maxUpwardVelocity());
        assertEquals(1.0D, settings.horizontalMultiplier());
    }

    @Test
    void fallsBackToTheDefaultSoundWhenConfiguredSoundIsInvalid() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("sound.type", "not_a_sound");
        AtomicReference<String> warning = new AtomicReference<>();

        Settings settings = Settings.read(config, warning::set, ignored -> false);

        assertEquals(Settings.DEFAULT_SOUND, settings.soundTypeName());
        assertEquals("Unknown sound 'not_a_sound'; using ENTITY_WIND_CHARGE_WIND_BURST instead.", warning.get());
    }
}
