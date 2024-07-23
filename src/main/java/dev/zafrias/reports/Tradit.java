package dev.zafrias.reports;

import dev.zafrias.reports.base.ReportManager;
import dev.zafrias.reports.commands.ReportCommand;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitCommandHandler;

@Getter
public final class Tradit extends JavaPlugin {


    @Getter
    private static Tradit instance;

    private ReportManager reportManager;

    private static BukkitAudiences audiences;
    private final static MiniMessage MINI_MESSAGE = MiniMessage.builder()
            .tags(TagResolver.builder()
                    .resolver(TagResolver.standard()).build())
            .build();


    @Override
    public void onEnable() {
        instance = this;
        audiences = BukkitAudiences.create(this);

        getLogger().info("Attempting to load config.yml");
        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();
        getLogger().info("Loaded config.yml successfully !");

        this.reportManager = new ReportManager(this, this.getConfig());

        getLogger().info("Loading commands !");
        //TODO load commands here
        BukkitCommandHandler handler = BukkitCommandHandler.create(this);
        handler.enableAdventure(audiences);
        if(handler.isBrigadierSupported()) {
            handler.registerBrigadier();
        }

        handler.register(new ReportCommand(this.getConfig()));

        getLogger().info("Tradit v" + this.getDescription().getVersion() + " has enabled successfully !");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    public static void sendComponent(Player player, String msg, TextColor color) {
        audiences.player(player).sendMessage(Component.text(msg, color));
    }

    public static void sendConfigMessage(CommandSender sender, String configPath, TagResolver... resolvers) {
        String msg = instance.getConfig().getString("messages." + configPath);
        Component comp = MINI_MESSAGE.deserialize(msg, resolvers);
        audiences.sender(sender).sendMessage(comp);
    }

}
