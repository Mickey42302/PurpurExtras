package org.purpurmc.purpurextras.modules;

import io.papermc.paper.event.player.PlayerServerFullCheckEvent;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.purpurmc.purpurextras.PurpurExtras;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class JoinFullServerModule implements PurpurExtrasModule, Listener {

    private YamlConfiguration uuidConfig;
    private File file;

    @Override
    public void enable() {
        PurpurExtras plugin = PurpurExtras.getInstance();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadUUIDFile(plugin);
    }

    @Override
    public boolean shouldEnable() {
        return PurpurExtras.getPurpurConfig().getBoolean("settings.join-full-server.enabled", false);
    }

    private void loadUUIDFile(PurpurExtras plugin) {
        this.file = new File(plugin.getDataFolder(), "jfs_uuids.yml");
        boolean isNewFile = false;

        try {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                Files.createDirectories(file.getParentFile().toPath());
            }
            if (!file.exists()) {
                Files.createFile(file.toPath());
                isNewFile = true;
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create the JFS UUIDs file.");
        }

        this.uuidConfig = YamlConfiguration.loadConfiguration(file);

        if (isNewFile) {
            this.uuidConfig.set("uuids", Collections.emptyList());
            saveConfig();
        }
    }

    public synchronized boolean addUUID(UUID uuid) {
        if (uuid == null) return false;
        List<String> uuids = getUUIDList();
        String uuidStr = uuid.toString();

        if (uuids.contains(uuidStr)) {
            return false;
        }

        uuids.add(uuidStr);
        this.uuidConfig.set("uuids", uuids);
        return saveConfig();
    }

    public synchronized boolean removeUUID(UUID uuid) {
        if (uuid == null) return false;
        List<String> uuids = getUUIDList();
        String uuidStr = uuid.toString();

        if (!uuids.contains(uuidStr)) {
            return false;
        }

        uuids.remove(uuidStr);
        this.uuidConfig.set("uuids", uuids);
        return saveConfig();
    }

    public boolean isAllowed(UUID uuid) {
        if (uuid == null) return false;
        return getUUIDList().contains(uuid.toString());
    }

    private List<String> getUUIDList() {
        if (this.uuidConfig == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(this.uuidConfig.getStringList("uuids"));
    }

    private boolean saveConfig() {
        if (this.uuidConfig == null || this.file == null) return false;
        try {
            this.uuidConfig.save(this.file);
            return true;
        } catch (IOException e) {
            PurpurExtras.getInstance().getLogger().severe("Could not save the JFS UUIDs file.");
            return false;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onServerFullCheck(PlayerServerFullCheckEvent event) {
        UUID uuid = event.getPlayerProfile().getId();
        if (isAllowed(uuid)) {
            event.allow(true);
        }
    }
}
