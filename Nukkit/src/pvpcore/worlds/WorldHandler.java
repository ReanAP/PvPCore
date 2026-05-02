package pvpcore.worlds;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;
import pvpcore.PvPCore;
import pvpcore.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldHandler {

    private final PvPCore core;
    private final Server server;
    private final File worldsFile;
    private final ConcurrentHashMap<String, PvPCWorld> worlds = new ConcurrentHashMap<>();

    public WorldHandler(PvPCore core) {
        this.core = core;
        this.server = core.getServer();
        this.worldsFile = new File(core.getDataFolder(), "worlds.json");
        this.init();
    }

    private void init() {
        try {
            if (!this.worldsFile.exists()) {
                this.loadLegacyConfig();
                this.save();
                return;
            }

            Config config = new Config(this.worldsFile, Config.JSON);
            for (Map.Entry<String, Object> entry : config.getAll().entrySet()) {
                PvPCWorld world = PvPCWorld.decode(entry.getKey(), entry.getValue());
                if (world != null) {
                    this.worlds.put(world.getLevelName(), world);
                }
            }
        } catch (Exception exception) {
            this.core.getLogger().warning("Failed to load worlds.json: " + exception.getMessage());
        }
    }

    private void loadLegacyConfig() {
        File configFile = new File(this.core.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return;
        }

        Config legacyConfig = new Config(configFile, Config.YAML);
        Object levels = legacyConfig.get("levels");
        if (!(levels instanceof Map<?, ?> levelsMap)) {
            return;
        }

        for (Map.Entry<?, ?> entry : levelsMap.entrySet()) {
            if (entry.getKey() instanceof String levelName) {
                PvPCWorld world = PvPCWorld.decodeLegacy(levelName, entry.getValue());
                if (world != null) {
                    this.worlds.put(world.getLevelName(), world);
                }
            }
        }
    }

    public PvPCWorld getWorld(Object level) {
        if (level instanceof Level nukkitLevel) {
            PvPCWorld world = this.worlds.computeIfAbsent(nukkitLevel.getName(), ignored -> new PvPCWorld(nukkitLevel));
            if (!Utils.levelsEqual(world.getLevel(), nukkitLevel)) {
                world.setLevel(nukkitLevel);
            }
            return world;
        }

        if (level instanceof String levelName) {
            boolean loaded = this.server.isLevelLoaded(levelName) || this.server.loadLevel(levelName);
            return loaded ? this.getWorld(this.server.getLevelByName(levelName)) : null;
        }

        return null;
    }

    public PvPCWorld getWorldKnockback(Player player1, Player player2) {
        for (PvPCWorld world : this.worlds.values()) {
            if (world.canUseKnockback(player1, player2)) {
                return world;
            }
        }

        if (Utils.levelsEqual(player1.getLevel(), player2.getLevel())) {
            return this.getWorld(player1.getLevel());
        }

        return null;
    }

    public ArrayList<PvPCWorld> getWorlds() {
        ArrayList<PvPCWorld> output = new ArrayList<>();
        Collection<Level> levels = this.server.getLevels().values();
        for (Level level : levels) {
            PvPCWorld world = this.getWorld(level);
            if (world != null) {
                output.add(world);
            }
        }
        return output;
    }

    public void save() {
        try {
            LinkedHashMap<String, Object> output = new LinkedHashMap<>();
            for (PvPCWorld world : this.worlds.values()) {
                output.put(world.getLevelName(), world.export());
            }

            Config config = new Config(this.worldsFile, Config.JSON);
            config.setAll(output);
            config.save();
        } catch (Exception exception) {
            this.core.getLogger().error("Failed to save worlds.json", exception);
        }
    }
}
