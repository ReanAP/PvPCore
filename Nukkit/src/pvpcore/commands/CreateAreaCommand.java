package pvpcore.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.window.Form;
import cn.nukkit.utils.TextFormat;
import pvpcore.forms.PvPCoreForms;
import pvpcore.utils.Utils;

public class CreateAreaCommand extends Command {

    public CreateAreaCommand() {
        super("createarea", "Command used to create the PvPArea.", "Usage: /createarea", new String[]{"areacreate", "createpvparea"});
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

        Form<?> form = PvPCoreForms.getCreateAreaForm(player);
        form.send(player);
        return true;
    }
}
