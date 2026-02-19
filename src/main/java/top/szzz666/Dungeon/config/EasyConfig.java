package top.szzz666.Dungeon.config;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EasyConfig {
    private final String configFilePath;
    private final LinkedHashMap<String, Object> defaults = new LinkedHashMap<>();
    private LinkedHashMap<String, Object> config = new LinkedHashMap<>();

    public EasyConfig(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    /**
     * 添加配置项及其默认值
     *
     * @param key          配置项名称
     * @param defaultValue 默认值
     */
    public void add(String key, Object defaultValue) {
        defaults.put(key, defaultValue);
    }

    /**
     * 获取配置项的值，自动推断类型
     *
     * @param key 配置项名称
     * @param <T> 返回值类型
     * @return 配置项的值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (!config.containsKey(key)) {
            return (T) defaults.get(key);
        }
        return (T) config.get(key);
    }

    public String getString(String key) {
        Object value = get(key);
        return value != null ? value.toString() : null;
    }

    public int getInt(String key) {
        Object value = get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        return ((Number) value).intValue();
    }

    public boolean getBoolean(String key) {
        Object value = get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }

    public double getDouble(String key) {
        Object value = get(key);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof String) {
            return Double.parseDouble((String) value);
        }
        return ((Number) value).doubleValue();
    }

    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> getMap(String key) {
        return (Map<K, V>) get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key) {
        return (List<T>) get(key);
    }

    public void set(String key, Object value) {
        config.put(key, value);
    }

    public void load() {
        Yaml yaml = new Yaml();
        File configFile = new File(configFilePath);

        try {
            if (!configFile.exists()) {
                // 如果文件不存在，创建并写入默认值
                config.putAll(defaults);
                save();
                return;
            }

            // 加载现有配置
            try (InputStream inputStream = Files.newInputStream(Paths.get(configFilePath))) {
                LinkedHashMap<String, Object> loadedConfig = yaml.load(inputStream);
                if (loadedConfig != null) {
                    config = loadedConfig;
                }
            }

            // 检查是否有新增的默认值
            boolean modified = false;
            for (Map.Entry<String, Object> entry : defaults.entrySet()) {
                if (!config.containsKey(entry.getKey())) {
                    config.put(entry.getKey(), entry.getValue());
                    modified = true;
                }
            }

            if (modified) {
                save();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存配置文件
     */
    public void save() {
        Yaml yaml = new Yaml();
        try {
            // 确保父目录存在
            File configFile = new File(configFilePath);
            File parentDir = configFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // 如果文件不存在则创建
            if (!configFile.exists()) {
                configFile.createNewFile();
            }

            // 写入文件（使用UTF-8编码）
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
                yaml.dump(config, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}