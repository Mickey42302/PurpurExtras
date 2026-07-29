package org.purpurmc.purpurextras.modules;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.purpurmc.purpurextras.PurpurExtras;

public class JoinFullServerModule implements PurpurExtrasModule, Listener {

    private final Permission joinFullServerPermission = new Permission(
            "purpurextras.joinfullserver",
            "Allows a player to bypass the player limit",
            PermissionDefault.OP
    );

    private final Permission legacyJoinFullServerPermission = new Permission(
            "purpur.joinfullserver",
            "Allows a player to bypass the player limit (legacy alias)",
            PermissionDefault.OP
    );

    @Override
    public void enable() {
        PurpurExtras plugin = PurpurExtras.getInstance();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        if (legacyPermissionEnabled()) {
            plugin.getLogger().warning("The \"legacy-permission\" setting is enabled.");
            plugin.getLogger().warning("The \"purpur.joinfullserver\" permission node is deprecated and will be removed in a future release.");
            plugin.getLogger().warning("Please migrate to the \"purpurextras.joinfullserver\" permission node.");
        }
    }

    @Override
    public boolean shouldEnable() {
        if (legacyPermissionEnabled()) {
            registerPermissions(joinFullServerPermission, legacyJoinFullServerPermission);
        } else {
            registerPermissions(joinFullServerPermission);
        }
        return PurpurExtras.getPurpurConfig().getBoolean("settings.join-full-server.enabled", false);
    }

    private boolean legacyPermissionEnabled() {
        return PurpurExtras.getPurpurConfig().getBoolean("settings.join-full-server.legacy-permission", false);
    }

    private boolean hasRequiredPermission(Player player) {
        if (legacyPermissionEnabled()) {
            return player.hasPermission(joinFullServerPermission) || player.hasPermission(legacyJoinFullServerPermission);
        }
        return player.hasPermission(joinFullServerPermission);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            if (hasRequiredPermission(event.getPlayer())) {
                event.allow();
            }
        }
    }
}