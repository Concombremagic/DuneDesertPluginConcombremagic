
package fr.dunedunes.ver;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class VerDeLaMortListener implements Listener {

    private final NamespacedKey bossKey;

    public VerDeLaMortListener(JavaPlugin plugin) {
        this.bossKey = new NamespacedKey(plugin, "ver_de_la_mort");
    }

    @EventHandler
    public void onBossBite(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();

        if (!(attacker instanceof Mob mob)) return;

        if (!mob.getPersistentDataContainer().has(
                bossKey, PersistentDataType.BYTE)) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) return;

        // Une seule attaque : la morsure.
        event.setDamage(8.0);
    }
}

