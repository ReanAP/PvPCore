package pvpcore.worlds.areas;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import pvpcore.PvPCore;
import pvpcore.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AreaHandler {

    private final PvPCore core;
    private final File areaFile;
    private final ConcurrentHashMap<String, PvPCArea> areas = new ConcurrentHashMap<>();

    public AreaHandler(PvPCore core) {
        this.core = core;
        this.areaFile = new File(core.getDataFolder(), "areas.json");
        this.init();
    }

    private void init() {
        try {
            if (!this.areaFile.exists()) {
                this.loadLegacyConfig();
                this.save();
                return;
            }

            Config config = new Config(this.areaFile, Config.JSON);
            for (Map.Entry<String, Object> entry : config.getAll().entrySet()) {
                PvPCArea area = PvPCArea.decode(entry.getKey(), entry.getValue());
                if (area != null) {
                    this.areas.put(area.getName(), area);
                }
            }
        } catch (Exception exception) {
            this.core.getLogger().warning("Failed to load areas.json: " + exception.getMessage());
        }
    }

    private void loadLegacyConfig() {
        File configFile = new File(this.core.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return;
        }

        Config legacyConfig = new Config(configFile, Config.YAML);
        Object areas = legacyConfig.get("areas");
        if (!(areas instanceof Map<?, ?> areasMap)) {
            return;
        }

        for (Map.Entry<?, ?> entry : areasMap.entrySet()) {
            if (entry.getKey() instanceof String areaName) {
                PvPCArea area = PvPCArea.decodeLegacy(areaName, entry.getValue());
                if (area != null) {
                    this.areas.put(area.getName(), area);
                }
            }
        }
    }

    public PvPCArea getArea(String name) {
        return this.areas.get(name);
    }

    public PvPCArea getAreaKnockback(Player player1, Player player2) {
        for (PvPCArea area : this.areas.values()) {
            if (area.canUseKnockback(player1, player2)) {
                return area;
            }
        }
        return null;
    }

    public boolean createArea(Map<?, ?> map, String name, Player player) {
        if (this.areas.containsKey(name)) {
            player.sendMessage(Utils.getPrefix() + TextFormat.RED + " The area already exists!");
            return false;
        }

        if (map != null && map.get("firstPos") instanceof Vector3 firstPos && map.get("secondPos") instanceof Vector3 secondPos) {
            PvPCArea area = new PvPCArea(name, player.getLevel(), firstPos, secondPos);
            this.areas.put(area.getName(), area);
            this.save();
            return true;
        }

        player.sendMessage(Utils.getPrefix() + TextFormat.RED + " Failed to create the area.");
        return false;
    }

    public boolean deleteArea(PvPCArea area) {
        if (this.areas.remove(area.getName()) != null) {
            this.save();
            return true;
        }
        return false;
    }

    public ArrayList<PvPCArea> getAreas() {
        return new ArrayList<>(this.areas.values());
    }

    public void save() {
        try {
            LinkedHashMap<String, Object> output = new LinkedHashMap<>();
            for (PvPCArea area : this.areas.values()) {
                output.put(area.getName(), area.export());
            }

            Config config = new Config(this.areaFile, Config.JSON);
            config.setAll(output);
            config.save();
        } catch (Exception exception) {
            this.core.getLogger().error("Failed to save areas.json", exception);
        }
    }
}
