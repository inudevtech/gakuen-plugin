package tech.inudev.gakuenplugin;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class GakuenCommand implements CommandExecutor {
    static public void setResourcePack(String url, CommandSender sender){
        try {
            URL fetchWebsite = new URL(url);
            File file = new File(GakuenPlugin.getInstance().getDataFolder().getAbsolutePath()+Paths.get(FilenameUtils.getName(fetchWebsite.getPath())));

            FileUtils.copyURLToFile(fetchWebsite, file);
            Bukkit.getOnlinePlayers().forEach((p) -> {
                try {
                    p.setResourcePack(url,DigestUtils.sha1(new FileInputStream(file)));
                } catch (IOException e) {
                    sender.sendMessage("リソースパックの取得に失敗しました。\n詳細はログを確認してください。");
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            sender.sendMessage("リソースパックの取得に失敗しました。\n詳細はログを確認してください。");
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("photograph")) { //親コマンドの判定
            if (args.length == 0) { //子コマンドがない場合
                sender.sendMessage("/photograph [on|off]");
            } else if (args.length == 1) { //子コマンドがある場合
                if (args[0].equalsIgnoreCase("on")) { //子コマンドがonの場合
                    GakuenPlugin.setPhotograph(true);
                    // TODO: コマンドの部分をクリックしたら実行できるように。
                    sender.sendMessage("撮影モードが有効になりました。\n全員が退出するか、§9§n/photograph off§rを実行すると撮影モードが無効になります。\nこのメッセージはF3+Dを押すことで消すことができます。");
                    return true;
                } else if (args[0].equalsIgnoreCase("off")) { //子コマンドがoffの場合
                    GakuenPlugin.setPhotograph(false);
                    sender.sendMessage("撮影モードが無効になりました。");
                    return true;
                }
            }
        } else if(command.getName().equalsIgnoreCase("r-reload")) {
            if(args.length == 0){
                sender.sendMessage("リソースパックを更新中...");
                setResourcePack(GakuenPlugin.getInstance().getConfig().getString("resourcepack-url"), sender);
                sender.sendMessage("リソースパックを更新しました。");
                return true;
            } else if(args.length == 1){
                sender.sendMessage("リソースパックを更新中...");
                setResourcePack(args[0], sender);
                sender.sendMessage("リソースパックを更新しました。");
                return true;
            }
        }
        return false;
    }
}
