package me.VAL.grapplingHook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class GrapplingHook extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Plugin enabled!");
        Bukkit.getPluginManager().registerEvents(new Hooks(), this);
        getCommand("grapplinghook").setExecutor(new Hooks());
    }
}
