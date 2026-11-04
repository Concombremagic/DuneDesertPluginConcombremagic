
package fr.dunedunes.ver;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Silverfish;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class VerDeLaMort {

    public static final double MAX_HEALTH = 350.0;

    private final JavaPlugin plugin;

    public VerDeLaMort(JavaPlugin plugin) {
        this.plugin = plugin;
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
                new org.bukkit.NamespacedKey(plugin, "ver_de_la_mort"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        return worm;
    }
}

