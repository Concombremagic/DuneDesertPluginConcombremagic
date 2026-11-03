package fr.dunedunes;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public final class DuneDesertPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("DuneDesert activé. Commande : /dunes create <nom>");
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new DuneGenerator();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("dunes")) return false;

        if (!sender.hasPermission("dunedesert.admin")) {
            sender.sendMessage("§cTu n'as pas la permission.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("§e/dunes create <nom> §7- crée un nouveau monde de dunes");
            sender.sendMessage("§e/dunes tp <nom> §7- téléporte vers ce monde");
            return true;
        }

        String action = args[0].toLowerCase();
        String worldName = args[1];

        if (!worldName.matches("[a-zA-Z0-9_-]{1,32}")) {
            sender.sendMessage("§cNom invalide. Utilise 1 à 32 lettres, chiffres, _ ou -.");
            return true;
        }

        if (action.equals("create")) {
            if (Bukkit.getWorld(worldName) != null) {
                sender.sendMessage("§cCe monde est déjà chargé. Utilise /dunes tp " + worldName);
                return true;
            }

            sender.sendMessage("§eCréation du monde " + worldName + "...");
            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(World.Environment.NORMAL);
            creator.generator(new DuneGenerator());

            World world = Bukkit.createWorld(creator);
            if (world == null) {
                sender.sendMessage("§cLa création a échoué. Vérifie la console du serveur.");
                return true;
            }

            sender.sendMessage("§aMonde créé ! Il utilise le générateur DuneDesert.");
            if (sender instanceof Player player) {
                player.teleport(world.getSpawnLocation());
            }
            return true;
        }

        if (action.equals("tp")) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                sender.sendMessage("§cMonde introuvable ou non chargé. Crée-le avec /dunes create " + worldName);
                return true;
            }
            if (sender instanceof Player player) {
                player.teleport(world.getSpawnLocation());
                sender.sendMessage("§aTéléportation vers " + worldName + ".");
            } else {
                sender.sendMessage("§cCette commande doit être exécutée par un joueur pour la téléportation.");
            }
            return true;
        }

        sender.sendMessage("§cAction inconnue. Utilise create ou tp.");
        return true;
    }
}
