package net.countercraft.movecraft.combat.listener;

import net.countercraft.movecraft.combat.config.ConfigUtil;
import net.countercraft.movecraft.combat.tracking.FireballTracking;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.util.MathUtils;


public class ProjectileHitListener implements Listener {
    @EventHandler
    public void projectileHitEvent(ProjectileHitEvent e) {
        if(!ConfigUtil.EnableFireballTracking)
            return;
        if(e.getEntity() == null)
            return;
        if(!(e.getEntity() instanceof Fireball))
            return;
        Fireball fireball = (Fireball) e.getEntity();

        Craft craft = CraftManager.getInstance().fastNearestCraftToLoc(fireball.getLocation());
        if(craft == null || !(craft instanceof PlayerCraft))
            return;
        if(!MathUtils.locIsNearCraftFast(craft, MathUtils.bukkit2MovecraftLoc(fireball.getLocation())))
            return;

        FireballTracking.getInstance().damagedCraft((PlayerCraft) craft, fireball);
    }
}
