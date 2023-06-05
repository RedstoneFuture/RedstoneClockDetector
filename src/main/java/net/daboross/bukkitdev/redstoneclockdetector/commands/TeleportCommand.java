package net.daboross.bukkitdev.redstoneclockdetector.commands;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.daboross.bukkitdev.redstoneclockdetector.RCDPlugin;
import net.daboross.bukkitdev.redstoneclockdetector.utils.AbstractCommand;
import net.daboross.bukkitdev.redstoneclockdetector.utils.UsageException;

public class TeleportCommand extends AbstractCommand {

    public TeleportCommand(AbstractCommand[] children, RCDPlugin plugin) {
        super("tp [player] [num]  Teleport player [player] to place of number [num] in list.",
                "redstoneclockdetector.tp", children);
        this.plugin = plugin;
    }
    protected RCDPlugin plugin;

    @Override
    protected boolean execute(CommandSender sender, MatchResult[] data)
            throws UsageException {
        Player player = sender instanceof Player ? (Player) sender : null;
        int tpNum = 0;
        if (data.length == 0) {
            if (player == null) {
                throw new UsageException(this.coloredUsage, "Must specify which player to teleport.");
            }
        } else if (data.length == 1) {
            if (player == null) {
                String playerName = data[0].getString();
                player = this.plugin.getServer().getPlayer(playerName);
                if (player == null) {
                    sender.sendMessage("Couldn't find player " + ChatColor.GREEN.toString() + playerName + ChatColor.WHITE + ".");
                    return true;
                }
            } else {
                Integer numData = data[0].getInteger();
                if (numData == null || numData <= 0) {
                    throw new UsageException(this.coloredUsage, "Location num must be a positive integer.");
                }
                tpNum = numData - 1;
            }
        } else if (data.length == 2) {
            String playerName = data[0].getString();
            player = this.plugin.getServer().getPlayer(playerName);
            if (player == null) {
                sender.sendMessage("Couldn't find player " + ChatColor.GREEN + playerName + ChatColor.WHITE + ".");
                return true;
            }
            Integer numData = data[1].getInteger();
            if (numData == null || numData <= 0) {
                throw new UsageException(this.coloredUsage, "Location num must be a positive integer.");
            }
            tpNum = numData - 1;
        }
        List<Entry<Location, Integer>> actList = this.plugin.getRedstoneActivityList();
        if (tpNum >= actList.size()) {
            sender.sendMessage("Location num " + ChatColor.YELLOW + (tpNum + 1) + " " + ChatColor.WHITE + "does not exist.");
        } else {
            player.teleport(actList.get(tpNum).getKey());
            if (player == sender) {
                sender.sendMessage("Teleporting...");
            } else {
                sender.sendMessage(ChatColor.GREEN.toString() + sender.getName() + ChatColor.WHITE + "is teleporting you...");
            }
        }
        return true;
    }

}
