package dev.zafrias.reports.commands;

import dev.zafrias.reports.Tradit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.bukkit.exception.SenderNotPlayerException;

import java.util.logging.Level;

public final class ReportCommand {

    private final FileConfiguration config;

    public ReportCommand(FileConfiguration config) {
        this.config = config;
    }

    @DefaultFor({"report"})
    public void defaultExec(BukkitCommandActor actor) {
        actor.reply(Component.text("/report <player> <reason...>", NamedTextColor.RED));
    }

    @Command({"report", "rep"})
    @CommandPermission("command.tradit.report")
    @Description("Main command for reporting players")
    public void reportPlayer(BukkitCommandActor actor, Player player, String reason) {
        // /report <player> <reason>
        try {
            actor.requirePlayer();
        } catch (SenderNotPlayerException ex) {
            actor.reply("Only a player can do this !");
            return;
        }

        String bypassPerm = config.getString("permissions.cannot-be-reported");
        if (player.hasPermission(bypassPerm)) {
            Tradit.sendConfigMessage(player, "report-fail");
            return;
        }

        Tradit.getInstance().getReportManager()
                .addReport(actor.getUniqueId(), player.getUniqueId(), reason)
                .exceptionally((ex) -> {
                    Tradit.getInstance().getLogger().log(Level.SEVERE, ex, () -> "Failed to store the report !");
                    return null;
                })
                .whenComplete((v, ex) -> {
                    if (ex != null) {
                        Tradit.sendComponent(actor.requirePlayer(), "ERROR: Something happened while processing your report !", NamedTextColor.RED);
                        return;
                    }
                    Tradit.sendConfigMessage(actor.requirePlayer(), "report-success",
                            Placeholder.parsed("target", player.getName()),
                            Placeholder.parsed("reason", reason));

                    final String broadcastPermission = config.getString("permissions.report-receive");
                    TagResolver[] placeholders = {
                            Placeholder.parsed("reporter", actor.getName()),
                            Placeholder.parsed("target", player.getName()),
                            Placeholder.parsed("reason", reason)
                    };

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.hasPermission(broadcastPermission)) {
                            Tradit.sendConfigMessage(onlinePlayer, "report-broadcast", placeholders);
                        }
                    }
                });


    }

}
