package pvpcore;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerCreationEvent;
import pvpcore.player.PvPCorePlayer;

public class PvPCListener implements Listener {

    @EventHandler
    public void onPlayerCreation(PlayerCreationEvent event) {
        event.setPlayerClass(PvPCorePlayer.class);
    }
}
