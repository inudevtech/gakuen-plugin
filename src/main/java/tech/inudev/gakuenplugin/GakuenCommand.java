package tech.inudev.gakuenplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class GakuenCommand implements CommandExecutor {
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
        }
        return false;
    }
}
