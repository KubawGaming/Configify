package me.kubaw208.configify;

import de.exlll.configlib.*;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import me.kubaw208.configify.annotations.AutoRegisterConfig;
import me.kubaw208.configify.annotations.CheckClass;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractConfigLoader {

    protected YamlConfigurationProperties properties;

    protected final JavaPlugin plugin;

    private final Map<Class<?>, Serializer> customSerializers = new HashMap<>();
    private final HashMap<Path, HashMap<Class<? extends AbstractConfig>, YamlConfigurationStore<? extends AbstractConfig>>> stores = new HashMap<>();
    private final HashMap<Path, HashMap<Class<? extends AbstractConfig>, AbstractConfig>> configs = new HashMap<>();
    private final HashMap<Class<? extends AbstractConfig>, Path> defaultPaths = new HashMap<>();

    private Path pathToCheckInFolders;
    private HashMap<String, Class<? extends AbstractConfig>> checkInFolders;

    public AbstractConfigLoader(JavaPlugin plugin) {
        this.plugin = plugin;

        buildProperties();
        loadAutoRegisteredConfigs();
        loadConfigs();
    }

    public AbstractConfigLoader(JavaPlugin plugin, Path path, HashMap<String, Class<? extends AbstractConfig>> configs) {
        this(plugin);
        this.pathToCheckInFolders =  path;
        this.checkInFolders = configs;
    }

    protected abstract void registerSerializers();

    public AbstractConfigLoader loadConfigs() { return this; }

    private void buildProperties() {
        var properties = ConfigLib.BUKKIT_DEFAULT_PROPERTIES
                .toBuilder();

        registerSerializers();

        for(var clazz : customSerializers.keySet()) {
            var serializer =  customSerializers.get(clazz);

            properties.addSerializer(clazz, serializer);
        }

        this.properties = properties.build();
    }

    protected final <T> void addSerializer(Class<T> type, Serializer<T, ?> serializer) {
        customSerializers.put(type, serializer);
    }

    protected <T extends AbstractConfig> T loadConfig(Class<T> clazz, String fileName) {
        return loadConfig(clazz, fileName, plugin.getDataFolder());
    }

    protected <T extends AbstractConfig> T loadConfig(Class<T> clazz, String fileName, File path) {
        return loadConfig(clazz, fileName, path, true);
    }

    protected <T extends AbstractConfig> T loadConfig(Class<T> clazz, String fileName, File path, boolean defaultPath) {
        var configurationStore = new YamlConfigurationStore<>(clazz, properties);
        var config = configurationStore.update(new File(path, fileName).toPath());

        configs.putIfAbsent(path.toPath(), new HashMap<>());
        stores.putIfAbsent(path.toPath(), new HashMap<>());

        var configs = this.configs.get(path.toPath());
        var stores = this.stores.get(path.toPath());

        configs.put(clazz, config);
        stores.put(clazz, configurationStore);

        if(defaultPath)
            defaultPaths.put(clazz, path.toPath());

        return config;
    }

    @CheckClass(AutoRegisterConfig.class)
    public <T extends SingletonConfig> T getConfig(Class<T> clazz) {
        var defaultPath = defaultPaths.getOrDefault(clazz, plugin.getDataFolder().toPath());
        return getConfig(clazz, defaultPath);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractConfig> T getConfig(Class<T> clazz, Path path) {
        var configsData = configs.get(path);

        if(configsData == null) return null;

        return (T) configsData.get(clazz);
    }

    public <T extends SingletonConfig> YamlConfigurationStore<T> getStore(Class<T> clazz) {
        var defaultPath = defaultPaths.getOrDefault(clazz, plugin.getDataFolder().toPath());
        return getStore(clazz, defaultPath);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractConfig> YamlConfigurationStore<T> getStore(Class<T> clazz, Path path) {
        var storesData = stores.get(path);

        if(storesData == null) return null;

        return (YamlConfigurationStore<T>) storesData.get(clazz);
    }

    public <T extends SingletonConfig> void saveConfig(Class<T> clazz) {
        var defaultPath = defaultPaths.getOrDefault(clazz, plugin.getDataFolder().toPath());
        saveConfig(clazz, defaultPath);
    }

    public <T extends AbstractConfig> void saveConfig(Class<T> clazz, Path path) {
        YamlConfigurations.save(path, clazz, YamlConfigurations.load(path, clazz));
    }

    public void saveAllConfigs() {
        for(var path : configs.keySet()) {
            for(var clazz : configs.get(path).keySet()) {
                saveConfig(clazz, path);
            }
        }
    }

    public <T extends SingletonConfig> boolean updateConfig(Class<T> clazz) {
        var defaultPath = defaultPaths.getOrDefault(clazz, plugin.getDataFolder().toPath());
        return updateConfig(clazz, defaultPath);
    }

    public <T extends AbstractConfig> boolean updateConfig(Class<T> clazz, Path path) {
        var configsData = configs.get(path);

        if(configsData == null) return false;

        configsData.put(clazz, YamlConfigurations.load(path, clazz));
        return true;
    }

    public void updateAllConfigs() {
        for(var path : configs.keySet()) {
            for(var clazz : configs.get(path).keySet()) {
                updateConfig(clazz, path);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadAutoRegisteredConfigs() {
        try(ScanResult scanResult = new ClassGraph()
                .enableAnnotationInfo()
                .acceptPackages(plugin.getClass().getPackageName())
                .scan()) {

            ClassInfoList classesWithAnnotation = scanResult.getClassesWithAnnotation(AutoRegisterConfig.class.getName());

            for(ClassInfo classInfo : classesWithAnnotation) {
                try {
                    Class<?> clazz = classInfo.loadClass();

                    if(!AbstractConfig.class.isAssignableFrom(clazz)) {
                        plugin.getLogger().warning("Class " + clazz.getName() + " has @AutoRegisterConfig but does not extend BaseConfig – skipping");
                        continue;
                    }

                    AutoRegisterConfig annotation = clazz.getAnnotation(AutoRegisterConfig.class);
                    String fileName = annotation.fileName();
                    String path = annotation.path();
                    boolean defaultPath = annotation.defaultPath();
                    Class<? extends AbstractConfig> configClass = (Class<? extends AbstractConfig>) clazz;

                    if(path == null || path.trim().isEmpty()) {
                        loadConfig(configClass, fileName);
                    } else {
                        File customPath = new File(plugin.getDataFolder(), path);

                        if(!customPath.exists())
                            customPath.mkdirs();

                        if(defaultPath)
                            defaultPaths.put((Class<? extends AbstractConfig>) clazz, customPath.toPath());

                        loadConfig(configClass, fileName, customPath);
                    }
                } catch(Exception e) {
                    plugin.getLogger().severe("Failed to load auto-registered config for class: " + classInfo.getName() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

}