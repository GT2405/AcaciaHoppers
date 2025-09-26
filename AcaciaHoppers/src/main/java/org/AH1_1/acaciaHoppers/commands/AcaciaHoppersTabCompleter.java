package org.AH1_1.acaciaHoppers.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AcaciaHoppersTabCompleter implements TabCompleter {

    public AcaciaHoppersTabCompleter() {
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("acaciahoppers.admin")) {
                completions.add("reload");
                completions.add("chest");
                completions.add("mob");
                completions.add("crop");
                completions.add("ore");
                completions.add("tree");
                completions.add("fish");
            }
        }

        return completions;
    }
}
