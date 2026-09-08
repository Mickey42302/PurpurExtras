package org.purpurmc.purpurextras;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.purpurmc.purpurextras.modules.JoinFullServerModule;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class JFSListCommand {

    private static final String PERMISSION = "purpurextras.jfslist";

    public static LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("jfslist")
                .requires(css -> css.getSender().hasPermission(PERMISSION))
                .executes(JFSListCommand::executeList)
                .then(Commands.literal("add")
                        .then(Commands.argument("targets", ArgumentTypes.playerProfiles())
                                .executes(JFSListCommand::executeAdd)))
                .then(Commands.literal("remove")
                        .then(Commands.argument("targets", ArgumentTypes.playerProfiles())
                                .executes(JFSListCommand::executeRemove)))
                .then(Commands.literal("list")
                        .executes(JFSListCommand::executeList))
                .then(Commands.literal("reload")
                        .executes(JFSListCommand::executeReload))
                .build();
    }

    private static JoinFullServerModule getModule(CommandSender sender) {
        JoinFullServerModule module = JoinFullServerModule.getInstance();
        if (module == null) {
            sender.sendRichMessage("<red>The Join Full Server module is disabled.");
        }
        return module;
    }

    private static int executeAdd(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        JoinFullServerModule module = getModule(sender);
        if (module == null) return 0;

        try {
            PlayerProfileListResolver profileResolver = ctx.getArgument("targets", PlayerProfileListResolver.class);
            Collection<PlayerProfile> profiles = profileResolver.resolve(ctx.getSource());

            if (profiles.isEmpty()) {
                sender.sendRichMessage("<red>Player not found.");
                return 0;
            }

            for (PlayerProfile profile : profiles) {
                UUID uuid = profile.getId();
                if (uuid == null) continue;

                String name = profile.getName() != null ? profile.getName() : uuid.toString();

                if (module.addUUID(uuid)) {
                    sender.sendRichMessage("<white>Added " + name + " to the Join Full Server list.");
                } else {
                    sender.sendRichMessage("<white>" + name + " is already on the Join Full Server list.");
                }
            }
        } catch (Exception e) {
            sender.sendRichMessage("<red>Player not found.");
            return 0;
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int executeRemove(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        JoinFullServerModule module = getModule(sender);
        if (module == null) return 0;

        try {
            PlayerProfileListResolver profileResolver = ctx.getArgument("targets", PlayerProfileListResolver.class);
            Collection<PlayerProfile> profiles = profileResolver.resolve(ctx.getSource());

            if (profiles.isEmpty()) {
                sender.sendRichMessage("<red>Player not found.");
                return 0;
            }

            for (PlayerProfile profile : profiles) {
                UUID uuid = profile.getId();
                if (uuid == null) continue;

                String name = profile.getName() != null ? profile.getName() : uuid.toString();

                if (module.removeUUID(uuid)) {
                    sender.sendRichMessage("<white>Removed " + name + " from the Join Full Server list.");
                } else {
                    sender.sendRichMessage("<white" + name + " is not on the Join Full Server list.");
                }
            }
        } catch (Exception e) {
            sender.sendRichMessage("<red>Player not found.");
            return 0;
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int executeList(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        JoinFullServerModule module = getModule(sender);
        if (module == null) return 0;

        List<String> uuids = module.getUUIDList();
        if (uuids.isEmpty()) {
            sender.sendRichMessage("<white>The Join Full Server list is empty.");
            return Command.SINGLE_SUCCESS;
        }

        sender.sendRichMessage("<purple>Join Full Server List (" + uuids.size() + "):</purple>");
        for (String uuidString : uuids) {
            UUID uuid = UUID.fromString(uuidString);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : uuidString;
            sender.sendRichMessage("<white>- " + name + " (" + uuidString + ")");
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int executeReload(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        JoinFullServerModule module = getModule(sender);
        if (module == null) return 0;

        module.reload();
        sender.sendRichMessage("<white>The Join Full Server list has been reloaded.");
        return Command.SINGLE_SUCCESS;
    }
}
