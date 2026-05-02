package pvpcore.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.window.Form;
import cn.nukkit.utils.TextFormat;
import pvpcore.forms.PvPCoreForms;
import pvpcore.utils.Utils;

public class PvPCoreCommand extends Command {

    public PvPCoreCommand() {
        super("pvpcore", "Displays the PvPCore Menu to the sender.", "Usage: /pvpcore", new String[]{"pvpcoremenu", "kbmenu", "pvpmenu"});
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

        Form<?> form = PvPCoreForms.getPvPCoreMenu(player);
        form.send(player);
        return true;
    }
}
