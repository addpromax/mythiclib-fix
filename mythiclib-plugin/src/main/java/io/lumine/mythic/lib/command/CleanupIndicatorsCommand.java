package io.lumine.mythic.lib.command;

import io.lumine.mythic.lib.MythicLib;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;

public class CleanupIndicatorsCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mythiclib.admin")) {
            sender.sendMessage("§c你没有权限使用此命令！");
            return true;
        }
        
        final NamespacedKey pdcKey = new NamespacedKey(MythicLib.plugin, "hologram");
        int cleanedCount = 0;
        
        try {
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                if (world != null) {
                    for (TextDisplay td : world.getEntitiesByClass(TextDisplay.class)) {
                        try {
                            if (td != null && !td.isDead() && td.getPersistentDataContainer().has(pdcKey, PersistentDataType.BOOLEAN)) {
                                td.remove();
                                cleanedCount++;
                            }
                        } catch (Exception e) {
                            MythicLib.plugin.getLogger().warning("Error removing TextDisplay entity: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            MythicLib.plugin.getLogger().warning("Error during TextDisplay cleanup: " + e.getMessage());
            sender.sendMessage("§c清理过程中发生错误: " + e.getMessage());
            return true;
        }
        
        if (cleanedCount > 0) {
            sender.sendMessage("§a成功清理了 " + cleanedCount + " 个残留的伤害指示器实体！");
        } else {
            sender.sendMessage("§e没有发现需要清理的伤害指示器实体。");
        }
        
        return true;
    }
}
