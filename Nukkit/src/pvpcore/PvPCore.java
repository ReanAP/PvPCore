package pvpcore;

import cn.nukkit.event.Listener;
import cn.nukkit.plugin.PluginBase;
import pvpcore.commands.CreateAreaCommand;
import pvpcore.commands.EditWorldCommand;
import pvpcore.commands.PvPAreaCommand;
import pvpcore.commands.PvPCoreCommand;
import pvpcore.utils.Utils;
import pvpcore.worlds.WorldHandler;
import pvpcore.worlds.areas.AreaHandler;

public class PvPCore extends PluginBase implements Listener {

    private static WorldHandler worldHandler;
    private static AreaHandler areaHandler;

    @Override
    public void onEnable() {
        this.getDataFolder().mkdirs();

        worldHandler = new WorldHandler(this);
        areaHandler = new AreaHandler(this);

        this.initCommands();
        this.getServer().getPluginManager().registerEvents(new PvPCListener(), this);
    }

    @Override
    public void onDisable() {
        if (worldHandler != null) {
            worldHandler.save();
        }
        if (areaHandler != null) {
            areaHandler.save();
        }
    }

    public static WorldHandler getWorldHandler() {
        return worldHandler;
    }

    public static AreaHandler getAreaHandler() {
        return areaHandler;
    }

    private void initCommands() {
        Utils.registerCommand(new PvPCoreCommand());
        Utils.registerCommand(new PvPAreaCommand());
        Utils.registerCommand(new CreateAreaCommand());
        Utils.registerCommand(new EditWorldCommand());
    }
}
