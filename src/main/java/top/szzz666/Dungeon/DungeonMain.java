package top.szzz666.Dungeon;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.item.Item;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import me.onebone.economyapi.EconomyAPI;
import top.szzz666.Dungeon.apis.TipVariable;
import top.szzz666.Dungeon.command.MyCommand;
import top.szzz666.Dungeon.config.EasyConfig;
import top.szzz666.Dungeon.entity.PermanentPlayerData;
import top.szzz666.Dungeon.entity.Team;
import top.szzz666.Dungeon.event.Listeners;
import top.szzz666.Dungeon.item.ItemMagicStone;
import top.szzz666.Dungeon.item.ItemMagicStoneFragment;
import top.szzz666.Dungeon.item.ItemTpMagicScroll;
import top.szzz666.Dungeon.panel.esay_chest_menu.CMListener;
import top.szzz666.Dungeon.world.DungeonGenerator;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static top.szzz666.Dungeon.biology.Refresh.StartRefresh;
import static top.szzz666.Dungeon.config.MyConfig.initConfig;
import static top.szzz666.Dungeon.recipe.Recipes.MagicStone;
import static top.szzz666.Dungeon.tools.pluginUtil.*;


public class DungeonMain extends PluginBase {
    public static Plugin plugin;
    public static Server nkServer;
    public static CommandSender consoleObjects;
    public static String ConfigPath;
    public static EconomyAPI economyAPI;
    public static EasyConfig ec;
    public static boolean NukkitType;
    public static ConcurrentHashMap<Player, PermanentPlayerData> permanentPlayerDataMap = new ConcurrentHashMap<>();
    public static HashMap<String, Team> teams = new HashMap<>();

    //插件读取
    @Override
    public void onLoad() {
        nkServer = getServer();
        plugin = this;
        consoleObjects = getServer().getConsoleSender();
        ConfigPath = getDataFolder().getPath();
        NukkitType = checkServer();
        initConfig();
        if (isDependencyLoaded("EconomyAPI")) {
            economyAPI = EconomyAPI.getInstance();
        }else {
            nkConsole("&cEconomyAPI未加载", 2);
            nkServer.shutdown();
        }
        Generator.addGenerator(DungeonGenerator.class, "dungeon", Generator.TYPE_INFINITE);
        Item.registerCustomItem(ItemMagicStone.class);
        Item.registerCustomItem(ItemMagicStoneFragment.class);
        Item.registerCustomItem(ItemTpMagicScroll.class);
        MagicStone();
        nkConsole("&b" + plugin.getName() + "插件读取...");
    }

    //插件开启
    @Override
    public void onEnable() {
        checkServer();
        //注册监听器
        nkServer.getPluginManager().registerEvents(new Listeners(), this);
        nkServer.getPluginManager().registerEvents(new CMListener(), this);
        //注册命令
        nkServer.getCommandMap().register(this.getName(), new MyCommand());
//        Enchantment.register(new LevelUpEnchantment(), false);
        if (isDependencyLoaded("Tips")) {
            try {
                Class.forName("tip.utils.variables.BaseVariable");
                TipVariable.init();
            } catch (Exception ignored) {
            }
        }
        if (getServer().getLevelByName(ec.getString("地下城世界")) == null) {
            Server.getInstance().generateLevel(ec.getString("地下城世界"), new Random().nextInt(), Generator.getGenerator("dungeon"));
        }
        StartRefresh();
        pluginNameLineConsole();
        nkConsole("&b" + plugin.getName() + "插件开启");
        nkConsole("&c" + plugin.getName() + "如果遇到任何bug，请加入Q群进行反馈：894279534", 1);

    }

    //插件关闭
    @Override
    public void onDisable() {
        nkConsole("&b" + plugin.getName() + "插件关闭");
    }

}
