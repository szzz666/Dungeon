package top.szzz666.Dungeon.config;


import java.util.ArrayList;
import java.util.HashMap;

import static top.szzz666.Dungeon.DungeonMain.ConfigPath;
import static top.szzz666.Dungeon.DungeonMain.ec;

public class MyConfig {
    public static void initConfig() {
        HashMap<String, Object> monster = new HashMap<>();
        monster.put("名称", "僵尸");
        monster.put("类型", "Zombie");
        monster.put("权重", 5);
        ArrayList<HashMap<String, Object>> monsterBrushingType = new ArrayList<>();
        monsterBrushingType.add(monster);

        HashMap<String, Double> item = new HashMap<>();
        item.put("s_rpg:magic_stone_fragment", 1.0);

        HashMap<String, Integer> item1 = new HashMap<>();
        item1.put("s_rpg:magic_stone", 10);

        ec = new EasyConfig(ConfigPath + "/config.yml");
        ec.add("command", "rpg");
        ec.add("地下城世界", "dungeon");

        ec.add("掉落材料", item);
        ec.add("等级原掉落材料倍率", 1.5);
        ec.add("等级击杀经验倍率", 10.0);

        ec.add("玩家升级经验倍率", 2.0);
        ec.add("玩家升级经济倍率", 5.0);
        ec.add("玩家升级需要经验", 1000);
        ec.add("玩家升级需要经济", 10000.0);


        ec.add("装备升级材料", item1);
        ec.add("装备升级材料倍率", 10.0);
        ec.add("装备升级经济倍率", 5.0);
        ec.add("装备升级需要经济", 1000.0);

        ec.add("剑每级提升攻击", 2.0);
        ec.add("盔甲每级提升血量", 2.0);

        ec.add("玩家刷新最大范围", 500);

        ec.add("等级压制倍率", 1.5);
        ec.add("怪物最低等级", 1);
        ec.add("怪物最高等级", 50);
        ec.add("怪物等级浮动", 5);

        ec.add("刷怪检测范围", 3);
        ec.add("刷怪检测数量", 15);
        ec.add("刷怪检测间隔", 20);
        ec.add("刷怪清理范围", 64);
        ec.add("刷怪清理最大范围", 128);
        ec.add("刷怪范围", 2);
        ec.add("安全区域半径", 24);
        ec.add("刷怪类型", monsterBrushingType);

        ec.add("物品刷新概率", 0.05);
        ec.add("容器物品", new ArrayList<HashMap<String, Object>>());
        ec.load();

    }

}
