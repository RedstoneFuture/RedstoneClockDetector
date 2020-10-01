package net.daboross.bukkitdev.redstoneclockdetector.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import net.daboross.bukkitdev.redstoneclockdetector.RCDPlugin;
import net.daboross.bukkitdev.redstoneclockdetector.utils.AbstractCommand;
import net.daboross.bukkitdev.redstoneclockdetector.utils.PermissionsException;
import net.daboross.bukkitdev.redstoneclockdetector.utils.UsageException;

public class StartCommand extends AbstractCommand {

    public StartCommand(AbstractCommand[] children, RCDPlugin plugin, AbstractCommand listCommand) {
        super("<sec>  Start scan for <sec> seconds.", "redstoneclockdetector.start", children);
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
        CommandSender user = this.plugin.getUser();
        if (user != null) {
            sender.sendMessage(ChatColor.GREEN.toString() + user.getName() + ChatColor.WHITE + " has already started a scan.");
            return true;
        }
        this.plugin.start(sender, seconds, new ProgressReporter(sender, new FinishCallback(this.listCommand, sender)));
        sender.sendMessage("Starting scan of " + seconds + " seconds.");
        return true;
    }

    protected class ProgressReporter implements RCDPlugin.IProgressReporter {

        protected CommandSender sender;
        protected FinishCallback finishCallback;

        public ProgressReporter(CommandSender sender, FinishCallback finishCallback) {
            this.sender = sender;
            this.finishCallback = finishCallback;
        }

        @Override
        public void onProgress(int secondsRemaining) {
            String header = ChatColor.WHITE + "[" + ChatColor.YELLOW + "RCD" + ChatColor.WHITE + "] ";
            if (secondsRemaining <= 0) {
                finishCallback.onFinish();
            } else if (secondsRemaining <= 5) {
                sender.sendMessage(header + secondsRemaining + " seconds remaining.");
            } else if (secondsRemaining <= 60 && secondsRemaining % 10 == 0) {
                sender.sendMessage(header + secondsRemaining + " seconds remaining.");
            } else if (secondsRemaining % 60 == 0) {
                sender.sendMessage(header + (secondsRemaining / 60) + " minutes remaining.");
            }
        }
    }

    protected class FinishCallback {

        public FinishCallback(AbstractCommand listCommand, CommandSender sender) {
            this.listCommand = listCommand;
            this.sender = sender;
        }

        public void onFinish() {
            try {
                this.listCommand.execute(sender, new String[]{"list"});
            } catch (PermissionsException unused) {
            } catch (UsageException unused) {
            }
        }
        protected AbstractCommand listCommand;
        protected CommandSender sender;
    }
}
