package net.daboross.bukkitdev.redstoneclockdetector.commands;

import java.util.List;
import java.util.Map.Entry;
import net.daboross.bukkitdev.redstoneclockdetector.RCDPlugin;
import net.daboross.bukkitdev.redstoneclockdetector.utils.AbstractCommand;
import net.daboross.bukkitdev.redstoneclockdetector.utils.UsageException;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class ListCommand extends AbstractCommand {

    protected RCDPlugin plugin;
    protected final int pageSize = 10;

    public ListCommand(AbstractCommand[] children, RCDPlugin plugin) {
        super("list [page]  List locations of redstone activities.", "redstoneclockdetector.list", children);
        this.plugin = plugin;
    }

    @Override
    protected boolean execute(CommandSender sender, MatchResult[] data) throws UsageException {
        int pageNum = 1;
        if (data.length > 0) {
            Integer pageData = data[0].getInteger();
            if (pageData == null || pageData <= 0) {
                throw new UsageException(this.coloredUsage, "Page number should be a positive integer.");
            }
            pageNum = pageData;
        }
        int startIndex = (pageNum - 1) * this.pageSize;
        List<Entry<Location, Integer>> actList = this.plugin.getRedstoneActivityList();
        int totalPage = actList.isEmpty() ? 0 : (actList.size() - 1) / this.pageSize + 1;
        sender.sendMessage("Page: " + ChatColor.YELLOW + pageNum + ChatColor.WHITE + "/" + ChatColor.GOLD + totalPage);
        if (startIndex >= actList.size()) {
            sender.sendMessage(ChatColor.GRAY + "No data.");
        } else {
            for (int i = startIndex, e = Math.min(startIndex + this.pageSize, actList.size()); i < e; ++i) {
                Entry<Location, Integer> entry = actList.get(i);
                Location l = entry.getKey();
                BaseComponent[] components = TextComponent.fromLegacyText(String.format(
                        ChatColor.YELLOW.toString() + "%d" + ChatColor.WHITE + ". " + ChatColor.GREEN + "(%d, %d, %d) %s " + ChatColor.DARK_GREEN + "%d",
                        i + 1, l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld().getName(), entry.getValue()));
                ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rcd tp " + (i + 1));
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.BLUE + "Click to teleport to " + (i + 1)));
                for (BaseComponent component : components) {
                    component.setClickEvent(clickEvent);
                    component.setHoverEvent(hoverEvent);
                }
                sender.spigot().sendMessage(components);
            }
        }
        return true;
    }
}
