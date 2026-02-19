package top.szzz666.Dungeon.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.item.Item;
import top.szzz666.Dungeon.entity.PermanentPlayerData;

import static top.szzz666.Dungeon.DungeonMain.*;
import static top.szzz666.Dungeon.config.MyConfig.initConfig;
import static top.szzz666.Dungeon.form.MyForm.*;
import static top.szzz666.Dungeon.panel.MyChestMenu.equipmentSelect;
import static top.szzz666.Dungeon.tools.pluginUtil.setEntityLevel;
import static top.szzz666.Dungeon.tools.pluginUtil.setItemLevel;


public class MyCommand extends Command {

    public MyCommand() {
        super(ec.getString("command"), plugin.getName() + "命令");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender.isOp() || sender instanceof ConsoleCommandSender) {
            if (args.length == 1) {
                if ("reload".equals(args[0])) {
                    initConfig();
                    sender.sendMessage(plugin.getName() + "插件配置已重新加载");
                }
                if ("test".equals(args[0])) {
                    Player player = (Player) sender;
                    Item item = player.getInventory().getItemInHand();
                    int level = 1;
                    setItemLevel(item, level);
                    player.getInventory().setItemInHand(item);
                    player.sendMessage("[StarrySkyRPG] 已附魔物品");
                }
            }
            if (args.length == 3) {
                if ("setlevel".equals(args[0])){
                    Player player = plugin.getServer().getPlayer(args[1]);
                    setEntityLevel(player, Integer.parseInt(args[2]));
                    PermanentPlayerData pd = permanentPlayerDataMap.get(player);
                    pd.level = Integer.parseInt(args[2]);
                    pd.save();
                    player.sendMessage(String.format("玩家%s的等级已设置为%d", player.getName(), Integer.parseInt(args[2])));
                }
                if("setexp".equals(args[0])){
                    Player player = plugin.getServer().getPlayer(args[1]);
                    int exp = Integer.parseInt(args[2]);
                    PermanentPlayerData pd = permanentPlayerDataMap.get(player);
                    pd.exp = exp;
                    pd.save();
                    player.sendMessage(String.format("玩家%s的经验已设置为%d", player.getName(), exp));
                }
            }
        }
        if (sender instanceof Player player) {
            if (args.length == 1) {
                if ("form".equals(args[0])) {
                    mainForm(player);
                }
                if ("ed".equals(args[0])) {
                    enterDungeon(player);
                }
                if ("au".equals(args[0])) {
                    adventurerUpgrade(player);
                }
                if ("es".equals(args[0])) {
                    equipmentSelect(player);
                }
            }
        }
        return false;
    }
}
