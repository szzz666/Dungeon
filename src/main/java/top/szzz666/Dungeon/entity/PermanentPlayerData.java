package top.szzz666.Dungeon.entity;

import cn.nukkit.Player;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static top.szzz666.Dungeon.DungeonMain.*;
import static top.szzz666.Dungeon.tools.pluginUtil.getEntityLevel;

@NoArgsConstructor
@AllArgsConstructor
public class PermanentPlayerData {
    public String name;
    public int level = 1;
    public int exp = 0;
    public boolean inDungeon = false;
    public boolean autoTotem = false;
    public boolean autoSprint = false;


    public PermanentPlayerData(Player player) {
        String fileName = player.getUniqueId().toString() + ".yml";
        PermanentPlayerData data = load(fileName);
        if (data == null) {
            return;
        }
        this.name = player.getName();
        this.level = data.level;
        this.exp = data.exp;
        this.inDungeon = data.inDungeon;
        this.autoTotem = data.autoTotem;
        this.autoSprint = data.autoSprint;
    }

    public static int getExp(Player player) {
        PermanentPlayerData data = permanentPlayerDataMap.get(player);
        if (data == null) {
            return 0;
        }
        return data.exp;
    }

    public static int getExpMax(Player player) {
        return (int) (ec.getInt("玩家升级需要经验") *
                ec.getDouble("玩家升级经济倍率") * getEntityLevel(player));
    }


    public static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null; // 没找到返回 null
    }

    public static PermanentPlayerData load(String fileName) {
        try {
            String path = ConfigPath + "/data/";
            File file = new File(path + fileName);

            if (!file.exists()) {
                return null;
            }

            // Create Yaml instance with appropriate options
            DumperOptions dumperOptions = new DumperOptions();
            Representer representer = new Representer(dumperOptions);
            representer.getPropertyUtils().setSkipMissingProperties(true);
            representer.addClassTag(PermanentPlayerData.class, Tag.MAP);

            // Create LoaderOptions
            LoaderOptions loaderOptions = new LoaderOptions();
            Constructor constructor = new Constructor(PermanentPlayerData.class, loaderOptions);

            Yaml yaml = new Yaml(constructor, representer);

            try (FileReader reader = new FileReader(file)) {
                return yaml.loadAs(reader, PermanentPlayerData.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void save() {
        try {
            String path = ConfigPath + "/data/";
            Player player = getKeyByValue(permanentPlayerDataMap, this);
            if (player == null) {
                return;
            }
            String fileName = player.getUniqueId().toString() + ".yml";

            // 新版本 Representer 需要 DumperOptions 参数
            DumperOptions options = new DumperOptions();
            Representer representer = new Representer(options);
            representer.getPropertyUtils().setSkipMissingProperties(true);
            representer.addClassTag(PermanentPlayerData.class, Tag.MAP);

            Yaml yaml = new Yaml(representer);
            Files.createDirectories(Paths.get(path));
            try (FileWriter writer = new FileWriter(path + fileName)) {
                yaml.dump(this, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取等级排行榜（从高到低）
     * @param limit 返回前N名，0表示返回所有玩家
     * @return 排序后的玩家数据列表
     */
    /**
     * 获取data文件夹下所有玩家文件的等级排行榜
     * @param limit 返回前N名，0表示返回所有玩家
     * @return 排序后的玩家数据列表
     */
    public static List<PermanentPlayerData> getLevelRanking(int limit) {
        List<PermanentPlayerData> allPlayerData = new ArrayList<>();
        String path = ConfigPath + "/data/";
        File dataDir = new File(path);

        // 检查目录是否存在
        if (!dataDir.exists() || !dataDir.isDirectory()) {
            return allPlayerData;
        }

        // 获取所有.yml文件
        File[] ymlFiles = dataDir.listFiles((dir, name) -> name.endsWith(".yml"));

        if (ymlFiles == null) {
            return allPlayerData;
        }

        // 遍历所有yml文件并加载数据
        for (File file : ymlFiles) {
            try {
                PermanentPlayerData data = load(file.getName());
                if (data != null) {
                    allPlayerData.add(data);
                }
            } catch (Exception e) {
                System.err.println("加载玩家数据文件失败: " + file.getName() + ", 错误: " + e.getMessage());
            }
        }

        // 按等级从高到低排序
        allPlayerData.sort((data1, data2) -> {
            if (data2.level != data1.level) {
                return Integer.compare(data2.level, data1.level);
            } else {
                return Integer.compare(data2.exp, data1.exp);
            }
        });

        // 如果有限制，则截取前N名
        if (limit > 0 && limit < allPlayerData.size()) {
            return allPlayerData.subList(0, limit);
        }

        return allPlayerData;
    }

    /**
     * 获取等级排行榜的玩家名列表（便于显示）
     * @param limit 返回前N名
     * @return 包含排名和玩家信息的字符串列表
     */
    public static List<String> getLevelRankingAsStringList(int limit) {
        List<PermanentPlayerData> ranking = getLevelRanking(limit);
        List<String> result = new ArrayList<>();

        for (int i = 0; i < ranking.size(); i++) {
            PermanentPlayerData data = ranking.get(i);
            result.add(String.format("第%d名: %s (等级: %d, 经验: %d)",
                    i + 1, data.name, data.level, data.exp));
        }

        return result;
    }

    public static String getLevelRankingAsString(int limit) {
        StringBuilder sb = new StringBuilder();
        List<String> ranking = getLevelRankingAsStringList(limit);
        for (String s : ranking) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }
}
