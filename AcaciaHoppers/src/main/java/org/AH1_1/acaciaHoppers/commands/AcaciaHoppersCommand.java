package org.AH1_1.acaciaHoppers.commands;

import org.AH1_1.acaciaHoppers.AcaciaHoppers;
import org.AH1_1.acaciaHoppers.hoppers.HopperType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AcaciaHoppersCommand implements CommandExecutor {

    private final AcaciaHoppers plugin;

    public AcaciaHoppersCommand(AcaciaHoppers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!sender.hasPermission("acaciahoppers.admin")) {
            sender.sendMessage(plugin.getMessage("commands.acaciahoppers.permission-message"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getMessage("commands.acaciahoppers.usage"));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload" -> {
                plugin.saveConfig();
                plugin.reloadConfig();
                plugin.getHopperManager().saveAll();
                plugin.getSuperChestManager().saveAll();
                sender.sendMessage("§aAcaciaHoppers plugin reloaded successfully!");
            }

            case "chest" -> giveSuperChest(sender, args);
            case "mob" -> giveHopper(sender, args, HopperType.MOB);
            case "crop" -> giveHopper(sender, args, HopperType.CROP);
            case "ore" -> giveHopper(sender, args, HopperType.ORE);
            case "tree" -> giveHopper(sender, args, HopperType.TREE);
            case "fish" -> giveHopper(sender, args, HopperType.FISH);

            default -> sender.sendMessage("§cUnknown subcommand: " + args[0]);
        }

        return true;
    }

    private void giveSuperChest(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /acaciahoppers chest <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found: " + args[1]);
            return;
        }

        plugin.getSuperChestManager().giveSuperChestItem(target);
        sender.sendMessage("§aGave 1 Super Chest to " + target.getName());
    }

    private void giveHopper(CommandSender sender, String[] args, HopperType type) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /acaciahoppers " + type.name().toLowerCase() + " <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found: " + args[1]);
            return;
        }

        plugin.getHopperManager().giveHopperItem(target, type, 1);
        sender.sendMessage("§aGave 1 " + type.name() + " hopper to " + target.getName());
    }
}
