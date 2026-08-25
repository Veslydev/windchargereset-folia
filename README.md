# WindChargeReset Folia

Native Folia 1.21.11 implementation of the Wind Charge momentum reset mechanic. A player who is falling fast enough, looking down, and sufficiently clear of solid ground can launch a Wind Charge to immediately brake downward momentum.

This project targets Java 21 and Folia 1.21.11. It is an independent, clean-room behavioral reimplementation of the analyzed WindChargeReset 2.0 plugin; it has no affiliation with the original author, Minemen, or the original project. Do not install this JAR alongside the original WindChargeReset JAR. Both plugins listen to the same launch event and would fight over the same projectile.

## Installation

1. Run Folia `1.21.11` with Java `21`.
2. Put `windchargereset-folia-2.0.0-folia.1.jar` in the server `plugins/` directory.
3. Start the server once to create `plugins/WindChargeReset/config.yml`.
4. Use either reload command after changing the configuration.

## Commands

| Command | Alias | Purpose |
| --- | --- | --- |
| `/windchargestop reload` | `/wcs` | Reload the config. |
| `/windchargereset reload` | `/wcr` | Legacy-compatible reload command. |

Tab completion only offers `reload` to senders with a reload permission.

## Permissions

| Permission | Default | Purpose |
| --- | --- | --- |
| `windchargereset.use` | `true` | Allows the reset mechanic. |
| `windchargestop.use` | `true` | Compatible legacy use permission. |
| `windchargereset.reload` | `op` | Allows config reload. |
| `windchargestop.reload` | `op` | Compatible legacy reload permission. |

Either use permission enables the mechanic. Either reload permission enables both commands.

## Configuration

The generated configuration preserves the original 2.0 keys and defaults.

| Key | Default | Description |
| --- | --- | --- |
| `enabled` | `true` | Enables the mechanic globally. |
| `activation.require_airborne` | `true` | Rejects activation when a collision box supports the player. |
| `activation.require_falling` | `true` | Requires negative vertical velocity. |
| `activation.min_fall_velocity` | `-0.25` | Player Y velocity must be less than or equal to this value. |
| `activation.min_pitch_down` | `55.0` | Minimum downward-looking pitch. Clamped to `-90..90`. |
| `activation.min_ground_distance` | `10.0` | Player must be more than this distance above solid ground. Exactly `10.0` is blocked. Negative values become `0`. |
| `activation.cooldown_ms` | `400` | Per-player wall-clock cooldown. Values at or below zero disable it. |
| `effect.consume_wind_charge` | `true` | Consumes one charge when the reset cancels the projectile, except in Creative. When the projectile is retained, controls vanilla consumption. |
| `effect.cancel_projectile` | `true` | Cancels and removes the Wind Charge after a successful reset, preventing later vanilla knockback. |
| `effect.vertical_brake_multiplier` | `0.25` | Multiplies current Y velocity before clamping to the upward range. Clamped to `0..1`. |
| `effect.min_upward_velocity` | `0.12` | Lower bound for reset Y velocity. Clamped to `-0.2..1`. |
| `effect.max_upward_velocity` | `0.45` | Upper bound for reset Y velocity. Clamped to `min_upward_velocity..1`. |
| `effect.horizontal_multiplier` | `0.85` | Multiplies X and Z velocity. Clamped to `0..1`. |
| `sound.enabled` | `true` | Plays the reset sound in the player world. |
| `sound.type` | `ENTITY_WIND_CHARGE_WIND_BURST` | Bukkit `Sound` name. Blank or invalid values use the default and invalid values log a warning. |
| `sound.volume` | `1.0` | Sound volume, with a minimum of zero. |
| `sound.pitch` | `0.67` | Sound pitch, with a minimum of zero. |
| `momentum_carryover.enabled` | `false` | Captures pre-reset velocity and fall distance for an Ender Pearl restore. |
| `momentum_carryover.timeout_ms` | `10000` | Maximum age of captured momentum. Negative values become zero. |
| `momentum_carryover.horizontal` | `true` | Restores captured X and Z after the next Ender Pearl teleport. |
| `momentum_carryover.vertical` | `true` | Restores captured Y after the next Ender Pearl teleport. |
| `momentum_carryover.fall_distance` | `true` | Restores captured fall distance after the next Ender Pearl teleport. |
| `messages.reload` | `&aWindCharge Reset config reloaded.` | Reload success message. Legacy `&` color codes work. |
| `messages.no_permission` | `&cYou do not have permission.` | Reload permission failure message. Legacy `&` color codes work. |

When `cancel_projectile` is `false`, the plugin leaves the projectile alive and only sets vanilla item consumption according to `consume_wind_charge`. The immediate custom reset still runs, and vanilla Wind Charge behavior may run later.

## Momentum Carryover

When enabled, a successful reset stores velocity and fall distance from immediately before the reset. The next `ENDER_PEARL` teleport consumes that stored entry and restores selected components on the player's next entity-owned tick. This supports movement combinations that need pre-reset momentum or fall distance, including mace interactions.

## Folia Behavior

All player mutation runs in player-owned contexts. Post-Ender-Pearl restoration uses the player's entity scheduler, and shared cooldown/momentum state is concurrent. Runtime config is published as an immutable atomic snapshot.

Ground scans are synchronous because the launch event must decide whether to cancel the projectile immediately. Before a scan, the plugin verifies that every chunk covered by the player's horizontal footprint is owned by the current Folia region. At the rare boundary where the footprint crosses into a non-owned region, the activation fails closed for that throw rather than performing an illegal cross-region block read.

## Build

```bash
./gradlew clean test build
```

The plugin JAR is written to `build/libs/windchargereset-folia-2.0.0-folia.1.jar`.
