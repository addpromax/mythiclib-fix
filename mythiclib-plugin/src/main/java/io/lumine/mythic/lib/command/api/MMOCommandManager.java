package io.lumine.mythic.lib.command.api;

import io.lumine.mythic.lib.manager.MMOManager;
import io.lumine.mythic.lib.util.ConfigFile;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

public abstract class MMOCommandManager implements MMOManager {
    public abstract JavaPlugin getPlugin();

    public abstract List<ToggleableCommand> getAll();

    @Override
    public void initialize(boolean clearBefore) {
        Validate.isTrue(!clearBefore, "Reloading this manager requires a reload");
        final List<ToggleableCommand> commands = getAll();

        // Load default config file
        if (!new File(getPlugin().getDataFolder(), "commands.yml").exists()) {
            final ConfigFile config = new ConfigFile(getPlugin(), "", "commands");

            for (ToggleableCommand cmd : commands) {
                final String path = cmd.getConfigPath();
                config.getConfig().set(path + ".main", cmd.getName());
                config.getConfig().set(path + ".permission", cmd.getPermission());
                config.getConfig().set(path + ".aliases", cmd.getAliases());
                config.getConfig().set(path + ".description", cmd.getDescription());
            }

            config.save();
        }

        try {

            // Find command map
            final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            final CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            // Enable commands individually
            final FileConfiguration config = new ConfigFile(getPlugin(), "", "commands").getConfig();
            for (ToggleableCommand toggleable : commands)
                if (toggleable.isEnabled() && (toggleable.isForced() || config.contains(toggleable.getConfigPath())))
                    commandMap.register(getPlugin().getName(), toggleable.toBukkit(config.getConfigurationSection(toggleable.getConfigPath())));

        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException exception) {
            getPlugin().getLogger().log(Level.WARNING, "Unable to register custom commands:");
            exception.printStackTrace();
        }
    }
}
