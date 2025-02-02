package net.countercraft.movecraft.combat.fireballs;

import net.countercraft.movecraft.combat.MovecraftCombat;
import net.countercraft.movecraft.combat.config.ConfigUtil;
import org.bukkit.entity.SmallFireball;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;

public class FireballManager extends BukkitRunnable {
    private static FireballManager instance;
    private final LinkedList<SmallFireball> q;

    public static FireballManager getInstance() {
        return instance;
    }

    public FireballManager() {
        instance = this;
        q = new LinkedList<>();
    }

    @Override
    public void run() {
        final int timeLimit = 20 * ConfigUtil.FireballLifespan * 50;

        while(q.size() > 0 && System.currentTimeMillis() - q.peek().getMetadata("MCC-Expiry").get(0).asLong() > timeLimit) {
            SmallFireball f = q.pop();
            f.remove();
        }
    }

    public void addFireball(SmallFireball f) {
        f.setMetadata("MCC-Expiry", new FixedMetadataValue(MovecraftCombat.getInstance(), System.currentTimeMillis()));
        q.add(f);
    }
}