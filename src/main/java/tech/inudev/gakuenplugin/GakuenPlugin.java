package tech.inudev.gakuenplugin;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.util.Objects;

public final class GakuenPlugin extends JavaPlugin implements Listener {

    @Getter
    private static boolean photograph;
    @Getter
    private static TextChannel channel;

    public static void setPhotograph(boolean photograph) {
        GakuenPlugin.photograph = photograph;
        // TODO: BossBar化
        EmbedBuilder embed = new EmbedBuilder();
        if (photograph) {
            Bukkit.getOnlinePlayers().forEach(p -> {
                if (!p.hasPermission("gakuenplugin.gorakuba")){
                    if (!p.hasPermission("gakuenplugin.approvallogin")){
                        p.kickPlayer("只今撮影中です！しばらくしてから再接続して下さい！");
                    } else {
                        p.sendTitle("§c撮影中です！", "§cご注意を。", 0, 80, 10);
                    }
                }
            });
            embed = embed.setTitle("撮影モードが有効化されました！").setDescription("撮影中のため、建築勢はサーバーにログインできません。\nログイン可能になるまで、しばらくお待ち下さい。").setColor(Color.ORANGE);
        } else {
            embed = embed.setTitle("撮影モードが無効化されました！").setDescription("撮影が終了しました。\nサーバーにログインできるようになりました。").setColor(Color.GREEN);
        }
        channel.sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("photograph")).setExecutor(new GakuenCommand());

        saveDefaultConfig();
        channel = DiscordSRV.getPlugin().getJda().getTextChannelById(getConfig().getLong("discord.channelId"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        getLogger().info(String.valueOf(photograph));
        if (photograph && !event.getPlayer().hasPermission("gakuenplugin.gorakuba")) {
            event.setJoinMessage(null);
            event.getPlayer().sendTitle("§c撮影中です！", "§cご注意を。", 0, 80, 10);
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (photograph && !event.getPlayer().hasPermission("gakuenplugin.approvallogin") && !event.getPlayer().hasPermission("gakuenplugin.gorakuba")) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "只今撮影中です！しばらくしてから再接続して下さい！");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (photograph) {
            event.setQuitMessage(null);
        }
        if (getServer().getOnlinePlayers().stream().noneMatch(player -> player.hasPermission("gakuenplugin.gorakuba"))) {
            getServer().broadcastMessage("撮影者がいなくなったため、撮影モードを無効化しました。");
            setPhotograph(false);
        }
    }
}
