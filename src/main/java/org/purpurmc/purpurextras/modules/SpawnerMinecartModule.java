package org.purpurmc.purpurextras.modules;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.purpurmc.purpurextras.PurpurExtras;

public class SpawnerMinecartModule implements PurpurExtrasModule, Listener {

    @Override
    public void enable() {
        PurpurExtras plugin = PurpurExtras.getInstance();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean shouldEnable() {
        return PurpurExtras.getPurpurConfig().getBoolean("settings.spawner-minecart.enabled", false);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (!(event.getRightClicked() instanceof SpawnerMinecart spawnerMinecart)) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            return;
        }

        EntityType spawnType = getEntityTypeFromEgg(item);

        if (spawnType == null || !spawnType.isSpawnable()) {
            return;
        }

        if (spawnerMinecart.getSpawnedType() == spawnType) {
            return;
        }

        spawnerMinecart.setSpawnedType(spawnType);

        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            item.subtract(1);
        }

        event.setCancelled(true);
    }

    private EntityType getEntityTypeFromEgg(ItemStack item) {
        String materialName = item.getType().name();
        if (materialName.endsWith("_SPAWN_EGG")) {
            String entityName = materialName.substring(0, materialName.length() - 10);
            try {
                return EntityType.valueOf(entityName);
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (item.getItemMeta() instanceof SpawnEggMeta eggMeta) {
            if (eggMeta.getCustomSpawnedType() != null) {
                return eggMeta.getCustomSpawnedType();
            }
            if (eggMeta.getSpawnedEntity() != null) {
                return eggMeta.getSpawnedEntity().getEntityType();
            }
        }

        return null;
    }
}
