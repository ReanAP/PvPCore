package pvpcore.player;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.connection.BedrockSession;
import cn.nukkit.network.protocol.types.PlayerInfo;
import cn.nukkit.utils.TextFormat;
import pvpcore.PvPCore;
import pvpcore.utils.PvPCKnockback;
import pvpcore.utils.Utils;

import java.util.HashMap;

public class PvPCorePlayer extends Player {

    private final HashMap<String, Object> areaInfo = new HashMap<>();

    public PvPCorePlayer(BedrockSession session, PlayerInfo info) {
        super(session, info);
    }

    public void setFirstPos() {
        this.areaInfo.put("firstPos", new Vector3(this.getX(), this.getY(), this.getZ()));
        this.sendMessage(Utils.getPrefix() + TextFormat.GREEN + " Successfully set the first position of the PvPArea.");
    }

    public void setSecondPos() {
        this.areaInfo.put("secondPos", new Vector3(this.getX(), this.getY(), this.getZ()));
        this.sendMessage(Utils.getPrefix() + TextFormat.GREEN + " Successfully set the second position of the PvPArea.");
    }

    public HashMap<String, Object> getAreaInfo() {
        return this.areaInfo;
    }

    public void createArea(String name) {
        if (PvPCore.getAreaHandler().createArea(this.areaInfo, name, this)) {
            this.sendMessage(Utils.getPrefix() + TextFormat.GREEN + " Successfully created a new PvPArea.");
            this.areaInfo.clear();
        }
    }

    @Override
    public void knockBack(Entity attacker, double damage, double x, double z, double base) {
        double horizontalKnockback = base;
        double verticalKnockback = base;

        if (attacker instanceof Player attackingPlayer) {
            PvPCKnockback knockback = Utils.getKnockbackFor(this, attackingPlayer);
            if (knockback != null) {
                horizontalKnockback = knockback.getHorizontalKB();
                verticalKnockback = knockback.getVerticalKB();
            }
        }

        double factor = Math.sqrt(x * x + z * z);
        if (factor <= 1.0e-6) {
            return;
        }

        factor = 1.0D / factor;
        Vector3 motion = new Vector3(this.motionX, this.motionY, this.motionZ);
        motion.x /= 2.0D;
        motion.y /= 2.0D;
        motion.z /= 2.0D;
        motion.x += x * factor * horizontalKnockback;
        motion.y += verticalKnockback;
        motion.z += z * factor * horizontalKnockback;

        if (motion.y > verticalKnockback) {
            motion.y = verticalKnockback;
        }

        this.setMotion(motion);
    }

    @Override
    public boolean attack(EntityDamageEvent event) {
        boolean attacked = super.attack(event);
        if (attacked
                && !event.isCancelled()
                && event instanceof EntityDamageByEntityEvent entityDamageByEntityEvent
                && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && entityDamageByEntityEvent.getDamager() instanceof Player attacker) {
            PvPCKnockback knockback = Utils.getKnockbackFor(this, attacker);
            if (knockback != null) {
                this.attackTime = knockback.getAttackDelay();
            }
        }
        return attacked;
    }
}
