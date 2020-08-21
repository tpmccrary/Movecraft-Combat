package net.countercraft.movecraft.combat.movecraftcombat.listener;

import net.countercraft.movecraft.combat.movecraftcombat.radar.RadarManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.combat.movecraftcombat.tracking.DamageManager;
import net.countercraft.movecraft.combat.movecraftcombat.status.StatusManager;


public class CraftSinkListener implements Listener {
    @EventHandler
    public void sinkListener(CraftSinkEvent e) {
        DamageManager.getInstance().craftSunk(e.getCraft());
        StatusManager.getInstance().craftSunk(e.getCraft());

        Player p = e.getCraft().getNotificationPlayer();
        if(p == null)
            return;
        if(e.getCraft().getType().getCruiseOnPilot())
            return;

        RadarManager.getInstance().endPilot(p);
        RadarManager.getInstance().endInvisible(p);
    }
}
