package dev.vesly.windchargeresetfolia;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class WindChargeResetPlugin extends JavaPlugin {
    private CooldownManager cooldownManager;
    private MomentumManager momentumManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        ConfigManager configManager = new ConfigManager(this);
        configManager.loadInitial();
        cooldownManager = new CooldownManager();
        momentumManager = new MomentumManager(this, configManager);

        AirStopService airStopService = new AirStopService(configManager, cooldownManager, momentumManager);
        getServer().getPluginManager().registerEvents(new WindChargeListener(airStopService), this);
        getServer().getPluginManager().registerEvents(new PlayerLifecycleListener(cooldownManager, momentumManager), this);

        ReloadCommand reloadCommand = new ReloadCommand(this, configManager);
        registerCommand("windchargestop", reloadCommand);
        registerCommand("windchargereset", reloadCommand);
    }

    @Override
    public void onDisable() {
        if (cooldownManager != null) {
            cooldownManager.clear();
        }
        if (momentumManager != null) {
            momentumManager.clear();
        }
    }

    private void registerCommand(String name, ReloadCommand command) {
        PluginCommand pluginCommand = getCommand(name);
        if (pluginCommand == null) {
            return;
        }
        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);
    }
}
