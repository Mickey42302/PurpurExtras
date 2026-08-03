package org.purpurmc.purpurextras.modules;

import io.papermc.paper.event.player.PlayerServerFullCheckEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.purpurmc.purpurextras.PurpurExtras;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class JoinFullServerModule implements PurpurExtrasModule, Listener {

    @Override
    public void enable() {
        PurpurExtras plugin = PurpurExtras.getInstance();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean shouldEnable() {
        boolean enabled = PurpurExtras.getPurpurConfig().getBoolean("settings.join-full-server.enabled", false);
        PurpurExtras.getPurpurConfig().getList("settings.join-full-server.uuids", Collections.emptyList());
        return enabled;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onServerFullCheck(PlayerServerFullCheckEvent event) {
        UUID uuid = event.getPlayerProfile().getId();
        if (uuid == null) {
            return;
        }

        List<String> allowed = PurpurExtras.getPurpurConfig().getList(
                "settings.join-full-server.uuids",
                Collections.emptyList()
        );

        if (allowed.contains(uuid.toString())) {
            event.allow(true);
        }
    }
}
