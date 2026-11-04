
package fr.dunedunes.ver;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class VerDeLaMortListener implements Listener {

    private final NamespacedKey bossKey;
    private final NamespacedKey eggKey;
    private final VerDeLaMortManager manager;

    public VerDeLaMortListener(JavaPlugin plugin) {
        this.bossKey = new NamespacedKey(plugin, "ver_de_la_mort");
        this.eggKey = new NamespacedKey(plugin, "ver_de_la_mort_egg");
        this.manager = ((fr.dunedunes.DuneDesertPlugin) plugin)
                .getVerDeLaMortManager();
    }

    @EventHandler
    public void onEggUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR
                && action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        if (!item.getItemMeta().getPersistentDataContainer()
                .has(eggKey, PersistentDataType.BYTE)) return;

        event.setCancelled(true);

        Player player = event.getPlayer();

        manager.spawn(player.getLocation());

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        player.sendMessage("§6Le Ver de la Mort a été invoqué !");
    }

    @EventHandler
    public void onBossBite(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();

        if (!(attacker instanceof Mob mob)) return;

        if (!mob.getPersistentDataContainer()
                .has(bossKey, PersistentDataType.BYTE)) return;

        if (!(event.getEntity() instanceof Player)) return;

        event.setDamage(8.0);
    }
}

