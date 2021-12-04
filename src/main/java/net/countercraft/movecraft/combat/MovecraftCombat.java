package net.countercraft.movecraft.combat;

import net.countercraft.movecraft.combat.commands.TracerModeCommand;
import net.countercraft.movecraft.combat.commands.TracerSettingCommand;
import net.countercraft.movecraft.combat.config.ConfigUtil;
import net.countercraft.movecraft.combat.directors.AADirectorManager;
import net.countercraft.movecraft.combat.directors.CannonDirectorManager;
import net.countercraft.movecraft.combat.fireballs.FireballManager;

import net.countercraft.movecraft.combat.localisation.I18nSupport;
import net.countercraft.movecraft.combat.player.PlayerManager;
import net.countercraft.movecraft.combat.radar.RadarManager;
import net.countercraft.movecraft.combat.sign.AADirectorSign;
import net.countercraft.movecraft.combat.sign.CannonDirectorSign;
import net.countercraft.movecraft.combat.status.StatusManager;
import net.countercraft.movecraft.combat.tracking.DamageManager;
import net.countercraft.movecraft.combat.utils.LegacyUtils;
import net.countercraft.movecraft.util.Tags;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;


public final class MovecraftCombat extends JavaPlugin {
    private static MovecraftCombat instance = null;

    private AADirectorManager aaDirectors;
    private CannonDirectorManager cannonDirectors;
    private PlayerManager playerManager;

    public static synchronized MovecraftCombat getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        saveDefaultConfig();


        ConfigUtil.Debug = getConfig().getBoolean("Debug", false);

        File folder = new File(MovecraftCombat.getInstance().getDataFolder(), "userdata");
        if (!folder.exists()) {
            getLogger().info("Created userdata directory");
            folder.mkdirs();
        }

        //TODO other languages
        String[] languages = {"en", "no"};
        for (String s : languages) {
            if (!new File(getDataFolder() + "/localisation/mcclang_" + s + ".properties").exists()) {
                this.saveResource("localisation/mcclang_" + s + ".properties", false);
            }
        }
        ConfigUtil.Locale = getConfig().getString("Locale", "en");
        I18nSupport.init();

        ConfigUtil.AADirectorDistance = getConfig().getInt("AADirectorDistance", 50);
        ConfigUtil.AADirectorRange = getConfig().getInt("AADirectorRange", 120);
        ConfigUtil.EnableContactExplosives = getConfig().getBoolean("EnableContactExplosives", true);
        ConfigUtil.CannonDirectorDistance = getConfig().getInt("CannonDirectorsDistance", 100);
        ConfigUtil.CannonDirectorRange = getConfig().getInt("CannonDirectorRange", 120);
        ConfigUtil.ContactExplosivesMaxImpulseFactor = getConfig().getDouble("ContactExplosivesMaxImpulseFactor", 10.0);

        Object tool = getConfig().get("DirectorTool");
        Material directorTool = null;
        if (tool instanceof String)
        {
            directorTool = Material.getMaterial((String) tool);
        }
        if (directorTool == null)
            getLogger().log(Level.SEVERE, "Failed to load director tool " + tool.toString());
        else
            ConfigUtil.DirectorTool = directorTool;
        if (getConfig().contains("TransparentBlocks")) {
            for (Object o : getConfig().getList("TransparentBlocks")) {
                if (o instanceof String)
                    ConfigUtil.Transparent.addAll(Tags.parseMaterials((String) o));
                else
                    getLogger().log(Level.SEVERE, "Failed to load transparent " + o.toString());
            }
        }
        if(getConfig().contains("DurabilityOverride")) {
            Map<String, Object> temp = getConfig().getConfigurationSection("DurabilityOverride").getValues(false);
            ConfigUtil.DurabilityOverride = new HashMap<>();
            for (Map.Entry<String, Object> entry : temp.entrySet()) {
                EnumSet<Material> materials = Tags.parseMaterials(entry.getKey());
                for(Material m : materials)
                    ConfigUtil.DurabilityOverride.put(m, (Integer) entry.getValue());
            }
        }
        ConfigUtil.FireballLifespan = getConfig().getInt("FireballLifespan", 6);
        ConfigUtil.TracerRateTicks = getConfig().getDouble("TracerRateTicks", 5.0);
        ConfigUtil.TracerMinDistanceSqrd = getConfig().getLong("TracerMinDistance", 60);
        ConfigUtil.TracerMinDistanceSqrd *= ConfigUtil.TracerMinDistanceSqrd;
        ConfigUtil.TracerParticle = Particle.valueOf(getConfig().getString("TracerParticles", "FIREWORKS_SPARK"));
        ConfigUtil.ExplosionParticle = Particle.valueOf(getConfig().getString("ExplosionParticles", "VILLAGER_ANGRY"));


        ConfigUtil.EnableFireballTracking = getConfig().getBoolean("EnableFireballTracking", false);
        ConfigUtil.EnableTNTTracking = getConfig().getBoolean("EnableTNTTracking", true);
        ConfigUtil.EnableTorpedoTracking = getConfig().getBoolean("EnableTorpedoTracking", false);
        ConfigUtil.DamageTimeout = getConfig().getInt("DamageTimeout", 300);
        ConfigUtil.EnableCombatReleaseTracking = getConfig().getBoolean("EnableCombatReleaseTracking", false);
        ConfigUtil.EnableCombatReleaseKick = getConfig().getBoolean("EnableCombatReleaseKick", true);
        ConfigUtil.CombatReleaseBanLength = getConfig().getLong("CombatReleaseBanLength", 60);
        ConfigUtil.CombatReleaseScuttle = getConfig().getBoolean("CombatReleaseScuttle", true);
        ConfigUtil.EnableAntiRadar = getConfig().getBoolean("EnableAntiRadar", false);
        ConfigUtil.EnableFireballPenetration = getConfig().getBoolean("EnableFireballPenetration", false);
        ConfigUtil.AddFiresToHitbox = getConfig().getBoolean("AddFiresToHitbox", true);

        new LegacyUtils();

        if(LegacyUtils.getInstance().isPostTranslocation()) {
            ConfigUtil.ReImplementTNTTranslocation = getConfig().getBoolean("ReImplementTNTTranslocation", false);

            if(ConfigUtil.ReImplementTNTTranslocation) {
                getServer().getPluginManager().registerEvents(new PistonListener(), this);
            }
        }

        ConfigUtil.MovementTracers = getConfig().getBoolean("MovementTracers", false);

        getCommand("tracersetting").setExecutor(new TracerSettingCommand());
        getCommand("tracermode").setExecutor(new TracerModeCommand());

        getServer().getPluginManager().registerEvents(new CraftCollisionExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new CraftDetectListener(), this);
        getServer().getPluginManager().registerEvents(new CraftReleaseListener(), this);
        getServer().getPluginManager().registerEvents(new CraftScuttleListener(), this);
        getServer().getPluginManager().registerEvents(new CraftSinkListener(), this);
        getServer().getPluginManager().registerEvents(new DispenseListener(), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new IgniteListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMovementListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileHitListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileLaunchListener(), this);
        getServer().getPluginManager().registerEvents(new TranslateListener(), this);
        getServer().getPluginManager().registerEvents(new AADirectorSign(), this);
        getServer().getPluginManager().registerEvents(new CannonDirectorSign(), this);

        aaDirectors = new AADirectorManager();
        aaDirectors.runTaskTimer(this, 0, 1);           // Every tick
        cannonDirectors = new CannonDirectorManager();
        cannonDirectors.runTaskTimer(this, 0, 1);       // Every tick
        playerManager = new PlayerManager();

        DamageManager damageTracking = new DamageManager();
        damageTracking.runTaskTimer(this, 0, 12000);    // Every 10 minutes
        StatusManager statusTracking = new StatusManager();
        statusTracking.runTaskTimer(this, 0, 200);      // Every 10 seconds
        RadarManager radarManager = new RadarManager();
        radarManager.runTaskTimer(this, 0, 12000);      // Every 10 minutes
        FireballManager fireballManager = new FireballManager();
        fireballManager.runTaskTimer(this, 0, 20);      // Every 1 second
    }

    @Override
    public void onDisable() {
        playerManager.shutDown();
    }

    public CannonDirectorManager getCannonDirectors() {
        return cannonDirectors;
    }

    public AADirectorManager getAADirectors() {
        return aaDirectors;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
