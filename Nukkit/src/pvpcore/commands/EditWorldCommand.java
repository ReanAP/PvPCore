package pvpcore.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.window.Form;
import cn.nukkit.utils.TextFormat;
import pvpcore.PvPCore;
import pvpcore.forms.PvPCoreForms;
import pvpcore.utils.Utils;
import pvpcore.worlds.PvPCWorld;

public class EditWorldCommand extends Command {

    public EditWorldCommand() {
        super("editworld", "Sends the edit world knockback form to the player.", "Usage: /editworld", new String[]{"editworldkb"});
        this.setPermission("pvpcore.permission.edit");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Utils.getPrefix() + TextFormat.RED + " Console can't use this command.");
            return true;
        }
        if (!this.testPermission(sender)) {
            return true;
        }

        PvPCWorld world = PvPCore.getWorldHandler().getWorld(player.getLevel());
        if (world == null) {
            sender.sendMessage(Utils.getPrefix() + TextFormat.RED + " Failed to load this world's PvPCore settings.");
            return true;
        }

        Form<?> form = PvPCoreForms.getWorldMenu(player, world, false);
        form.send(player);
        return true;
    }
}
