package net.daboross.bukkitdev.redstoneclockdetector.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import net.daboross.bukkitdev.redstoneclockdetector.RCDPlugin;
import net.daboross.bukkitdev.redstoneclockdetector.utils.AbstractCommand;
import net.daboross.bukkitdev.redstoneclockdetector.utils.PermissionsException;
import net.daboross.bukkitdev.redstoneclockdetector.utils.UsageException;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class StartCommand extends AbstractCommand {

    public StartCommand(AbstractCommand[] children, RCDPlugin plugin, AbstractCommand listCommand) {
        super("<sec> [-bc]  Start scan for <sec> seconds.", "redstoneclockdetector.start", children);
        this.plugin = plugin;
        this.listCommand = listCommand;
    }
    protected RCDPlugin plugin;
    protected AbstractCommand listCommand;

    @Override
    protected boolean execute(CommandSender sender, MatchResult[] data) throws UsageException {
        Integer seconds = data[0].getInteger();
        if (seconds == null) {
            return false;
        }
        if (seconds <= 0) {
            throw new UsageException(this.coloredUsage, "Seconds number should be a positive integer.");
        }
        boolean broadcast = data.length > 1 && sender.hasPermission("redclockdetector.broadcast.send")
                && ("-bc".equalsIgnoreCase(data[1].getString()) || "--broadcast".equalsIgnoreCase(data[1].getString()));
        CommandSender user = this.plugin.getUser();
        if (user != null) {
            sender.sendMessage(ChatColor.GREEN.toString() + user.getName() + ChatColor.WHITE + " has already started a scan.");
            this.plugin.getWorker().getReporter().addSender(sender);
            if (broadcast) {
                addBroadcastReceivers();
            }
            return true;
        }
        this.plugin.start(sender, seconds, new ProgressReporter(sender, new FinishCallback(this.listCommand, sender)));
        if (broadcast) {
            addBroadcastReceivers();
        }
        sender.sendMessage("Starting scan of " + seconds + " seconds.");
        return true;
    }

    private void addBroadcastReceivers() {
        this.plugin.getWorker().getReporter().addSender(this.plugin.getServer().getConsoleSender());
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission("redclockdetector.broadcast.receive")) {
                this.plugin.getWorker().getReporter().addSender(player);
            }
        }
    }

    protected class ProgressReporter implements RCDPlugin.IProgressReporter {

        protected Set<CommandSender> senders = new LinkedHashSet<>();
        protected FinishCallback finishCallback;

        public ProgressReporter(CommandSender sender, FinishCallback finishCallback) {
            this.senders.add(sender);
            this.finishCallback = finishCallback;
        }

        @Override
        public void onProgress(int secondsRemaining) {
            String header = ChatColor.WHITE + "[" + ChatColor.YELLOW + "RCD" + ChatColor.WHITE + "] ";
            if (secondsRemaining <= 0) {
                finishCallback.onFinish();
            } else {
                for (CommandSender sender : senders) {
                    if (secondsRemaining <= 5) {
                        sender.sendMessage(header + secondsRemaining + " seconds remaining.");
                    } else if (secondsRemaining <= 60 && secondsRemaining % 10 == 0) {
                        sender.sendMessage(header + secondsRemaining + " seconds remaining.");
                    } else if (secondsRemaining % 60 == 0) {
                        sender.sendMessage(header + (secondsRemaining / 60) + " minutes remaining.");
                    }
                }
            }
        }

        @Override
        public void addSender(CommandSender sender) {
            senders.add(sender);
            finishCallback.receivers.add(sender);
        }
    }

    protected class FinishCallback {

        public FinishCallback(AbstractCommand listCommand, CommandSender... receivers) {
            this.listCommand = listCommand;
            Collections.addAll(this.receivers, receivers);
        }

        public void onFinish() {
            for (CommandSender receiver : receivers) {
                try {
                    this.listCommand.execute(receiver, new String[]{"list"});
                } catch (PermissionsException unused) {
                } catch (UsageException unused) {
                }
            }
        }
        protected AbstractCommand listCommand;
        protected Set<CommandSender> receivers = new LinkedHashSet<>();
    }
}
