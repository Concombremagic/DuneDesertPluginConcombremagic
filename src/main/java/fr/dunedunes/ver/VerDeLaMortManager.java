
package fr.dunedunes.ver;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Silverfish;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class VerDeLaMortManager {

    public static final double MAX_HEALTH = 350.0;

    private final JavaPlugin plugin;
    private final NamespacedKey bossKey;

    public VerDeLaMortManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.bossKey = new NamespacedKey(plugin, "ver_de_la_mort");
    }

    public Mob spawn(Location location) {
        Silverfish worm = (Silverfish) location.getWorld()
                .spawnEntity(location, EntityType.SILVERFISH);

        worm.setCustomName("§6§lVer de la Mort");
        worm.setCustomNameVisible(true);
        worm.setPersistent(true);
        worm.setRemoveWhenFarAway(false);

        var health = worm.getAttribute(Attribute.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(MAX_HEALTH);
            worm.setHealth(MAX_HEALTH);
        }

        worm.getPersistentDataContainer().set(
                bossKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        return worm;
    }
}

