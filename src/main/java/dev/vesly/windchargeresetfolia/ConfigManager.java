package dev.vesly.windchargeresetfolia;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

final class ConfigManager {
    private final JavaPlugin plugin;
    private final AtomicReference<Settings> settings = new AtomicReference<>();

    ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    void loadInitial() {
        settings.set(readSettings());
    }

    Settings settings() {
        return settings.get();
    }

    Settings reload() {
        Settings loaded = readSettings();
        settings.set(loaded);
        return loaded;
    }

    private Settings readSettings() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);
        Logger logger = plugin.getLogger();
        return Settings.read(configuration, logger::warning, Settings::isBukkitSound);
    }
}
