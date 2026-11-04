
package fr.dunedunes.ver;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class VerDeLaMortEgg implements CommandExecutor {

    private final NamespacedKey eggKey;

    public VerDeLaMortEgg(JavaPlugin plugin) {
        this.eggKey = new NamespacedKey(plugin, "ver_de_la_mort_egg");
    }

    public ItemStack createEgg() {
        ItemStack egg = new ItemStack(Material.SILVERFISH_SPAWN_EGG);
        ItemMeta meta = egg.getItemMeta();

        meta.setDisplayName("§6§lŒuf du Ver de la Mort");
        meta.setLore(List.of(
                "§7Invoque le terrible Ver de la Mort.",
                "§8Objet unique"
        ));

        meta.getPersistentDataContainer().set(
                eggKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        egg.setItemMeta(meta);
        return egg;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Cette commande doit être utilisée en jeu.");
            return true;
        }

        if (!player.hasPermission("dunedesert.admin")) {
            player.sendMessage("§cTu n'as pas la permission.");
            return true;
        }

        player.getInventory().addItem(createEgg());
        player.sendMessage("§aTu as reçu l'œuf du Ver de la Mort !");
        return true;
    }
}

