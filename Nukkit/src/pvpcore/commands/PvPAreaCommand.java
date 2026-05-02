package pvpcore.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import pvpcore.player.PvPCorePlayer;
import pvpcore.utils.Utils;

public class PvPAreaCommand extends Command {

    public PvPAreaCommand() {
        super("pvparea", "Sets the pvparea information in the player", "Usage: /pvparea <pos1:pos2>", new String[]{"pvpareapos"});
        this.setPermission("pvpcore.permission.edit");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }
        if (!(sender instanceof PvPCorePlayer player)) {
            String message = sender instanceof Player
                    ? "Internal plugin error. Please rejoin before using this command."
                    : "Console can't use this command.";
            sender.sendMessage(Utils.getPrefix() + " " + TextFormat.RED + message);
            return true;
        }
        if (args.length == 0 || (!args[0].equalsIgnoreCase("pos1") && !args[0].equalsIgnoreCase("pos2"))) {
            sender.sendMessage(Utils.getPrefix() + " " + TextFormat.RED + this.getUsage());
            return true;
        }

        if (args[0].equalsIgnoreCase("pos1")) {
            player.setFirstPos();
        } else {
            player.setSecondPos();
        }
        return true;
    }
}
