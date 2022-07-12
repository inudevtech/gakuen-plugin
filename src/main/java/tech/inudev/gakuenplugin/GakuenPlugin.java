package tech.inudev.gakuenplugin;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import lombok.Getter;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public final class GakuenPlugin extends JavaPlugin implements Listener {

    @Getter
    private static boolean photograph;
    @Getter
    private static TextChannel channel;

    @Getter
    private static GakuenPlugin instance;

    public static void setPhotograph(boolean photograph) {
        // TODO: BossBar化
        EmbedBuilder embed = new EmbedBuilder();
        if (photograph) {
            Bukkit.getOnlinePlayers().forEach(p -> {
                if (!p.hasPermission("gakuenplugin.gorakuba")) {
                    if (!p.hasPermission("gakuenplugin.approvallogin")) {
                        p.kickPlayer("只今撮影中です！しばらくしてから再接続して下さい！");
                    } else {
                        p.sendTitle("§c撮影中です！", "§cご注意を。", 0, 80, 10);
                    }
                }
            });
            embed = embed.setTitle("撮影モードが有効化されました！").setDescription("撮影中のため、建築勢はサーバーにログインできません。\nログイン可能になるまで、しばらくお待ち下さい。").setColor(Color.ORANGE);
        } else if (GakuenPlugin.photograph) {
            embed = embed.setTitle("撮影モードが無効化されました！").setDescription("撮影が終了しました。\nサーバーにログインできるようになりました。").setColor(Color.GREEN);
        }
        if (!embed.isEmpty()) {
            GakuenPlugin.photograph = photograph;
            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("photograph")).setExecutor(new GakuenCommand());
        Objects.requireNonNull(getCommand("r-reload")).setExecutor(new GakuenCommand());

        saveDefaultConfig();
        DiscordSRV.api.subscribe(this);
        instance = this;
    }

    @Subscribe
    public void discordReadyEvent(DiscordReadyEvent event) {
        channel = DiscordSRV.getPlugin().getJda().getTextChannelById(getConfig().getLong("discord.channelId"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(GakuenPlugin.getInstance(), () -> {
            try {
                URL fetchWebsite = new URL(Objects.requireNonNull(getConfig().getString("resourcepack-url")));
                File file = new File(getDataFolder().getAbsolutePath() + Paths.get(FilenameUtils.getName(fetchWebsite.getPath())));
                FileUtils.copyURLToFile(fetchWebsite, file);
                event.getPlayer().setResourcePack(Objects.requireNonNull(getConfig().getString("resourcepack-url")), DigestUtils.sha1(Files.newInputStream(file.toPath())));

                getLogger().info(String.valueOf(photograph));
                if (photograph && !event.getPlayer().hasPermission("gakuenplugin.gorakuba")) {
                    event.setJoinMessage(null);
                    event.getPlayer().sendTitle("§c撮影中です！", "§cご注意を。", 0, 80, 10);
                }
            } catch (IOException e) {
                event.getPlayer().sendMessage("リソースパックの取得に失敗しました。\n詳細はログを確認してください。");
                e.printStackTrace();
            }
        });
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

            getServer().getScheduler().runTaskLater(this,() -> {
                if (getServer().getOnlinePlayers().stream().noneMatch(player -> player.hasPermission("gakuenplugin.gorakuba"))) {
                    getServer().broadcastMessage("撮影者がいなくなったため、撮影モードを無効化しました。");
                    setPhotograph(false);
                }
            },1);
        }
    }
}
