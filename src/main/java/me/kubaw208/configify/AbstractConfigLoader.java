package me.kubaw208.configify;

import de.exlll.configlib.*;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import me.kubaw208.configify.annotations.AutoRegisterConfig;
import me.kubaw208.configify.annotations.CheckClass;
import me.kubaw208.configify.enums.SearchType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for managing configurations in a Bukkit/Spigot plugin.
 * <p>
 * Main features:
 * <ul>
 *     <li>Discovery of configurations marked with the {@link AutoRegisterConfig} annotation</li>
 *     <li>Management of multiple configuration instances across different paths</li>
 *     <li>Built-in methods for loading, saving, and updating configurations</li>
 * </ul>
 * <p>
 * To use this class, you should:
 * <ol>
 *     <li>Extend it in your own loader class</li>
 *     <li>Implement the {@link #registerSerializers()} method to register custom serializers</li>
 *     <li>Create configuration classes extending {@link AbstractConfig} or {@link SingletonConfig}</li>
 *     <li>Optionally use the {@link AutoRegisterConfig} annotation and call {@link #loadAutoRegisteredConfigs()} to load them</li>
 * </ol>
 *
 * @see AbstractConfig
 * @see SingletonConfig
 * @see AutoRegisterConfig
 */
public abstract class AbstractConfigLoader {

    protected YamlConfigurationProperties properties;

    protected final JavaPlugin plugin;

    private final Map<Class<?>, Serializer> customSerializers = new HashMap<>();
    private final HashMap<Path, HashMap<Class<? extends AbstractConfig>, YamlConfigurationStore<? extends AbstractConfig>>> stores = new HashMap<>();
    private final HashMap<Path, HashMap<Class<? extends AbstractConfig>, AbstractConfig>> configs = new HashMap<>();
    private final HashMap<Class<? extends AbstractConfig>, Path> defaultPaths = new HashMap<>();

    /**
     * Initializes the configuration loader, builds properties, and automatically loads marked classes.
     *
     * @param plugin The JavaPlugin instance.
     */
    public AbstractConfigLoader(JavaPlugin plugin) {
        this.plugin = plugin;

        buildProperties();
    }

    /**
     * Registers custom serializers for data types not supported by default by ConfigLib.
     */
    protected abstract void registerSerializers();

    /**
     * Builds configuration properties, including registering custom serializers.
     */
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

    /**
     * Adds a custom serializer to the list of serializers to be registered.
     * Should be called inside {@link #registerSerializers()}.
     *
     * @param type       The data type class.
     * @param serializer The serializer instance.
     * @param <T>        The data type.
     */
    protected final <T> void addSerializer(Class<T> type, Serializer<T, ?> serializer) {
        customSerializers.put(type, serializer);
    }

    /**
     * Loads multiple configurations from a specified folder.
     *
     * @param startingFolder The starting folder.
     * @param searchType     The search type (files or folders).
     * @param filesToFind    A map of file names and their corresponding configuration classes.
     */
    protected void loadConfigs(@NotNull File startingFolder, @NotNull SearchType searchType, @NotNull Map<String, Class<?>> filesToFind) {
        if(!startingFolder.exists() || !startingFolder.isDirectory()) {
            plugin.getLogger().warning("Invalid starting folder: " + startingFolder);
            return;
        }
        if(filesToFind.isEmpty()) return;

        if(searchType == SearchType.FILES) {
            loadFilesFromFolder(startingFolder, filesToFind);
        } else if(searchType == SearchType.RECURSIVE) {
            loadFilesInSubFolders(startingFolder, filesToFind);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFilesFromFolder(File folder, Map<String, Class<?>> filesToFind) {
        File[] files = folder.listFiles();

        if(files == null) return;

        for(File file : files) {
            if(!file.isFile()) continue;

            String fileName = file.getName();
            Class<?> clazz = filesToFind.get(fileName);

            if(clazz == null) continue;
            if(!AbstractConfig.class.isAssignableFrom(clazz)) continue;

            Class<? extends AbstractConfig> configClass = (Class<? extends AbstractConfig>) clazz;

            loadConfig(configClass, fileName, folder);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFilesInSubFolders(File startingFolder, Map<String, Class<?>> filesToFind) {
        File[] subdirs = startingFolder.listFiles(File::isDirectory);

        if(subdirs == null) return;

        for(File subdir : subdirs) {
            for(Map.Entry<String, Class<?>> entry : filesToFind.entrySet()) {
                String fileName = entry.getKey();
                Class<?> clazz = entry.getValue();
                File targetFile = new File(subdir, fileName);

                if(!targetFile.exists() || !targetFile.isFile()) continue;
                if(!AbstractConfig.class.isAssignableFrom(clazz)) continue;

                Class<? extends AbstractConfig> configClass = (Class<? extends AbstractConfig>) clazz;

                loadConfig(configClass, fileName, subdir);
            }
        }
    }

    /**
     * Core method for loading a configuration from the plugin's main folder.
     *
     * @param clazz    The configuration class.
     * @param fileName The file name.
     * @param <T>      The configuration type.
     * @return The loaded configuration instance.
     */
    protected <T extends AbstractConfig> T loadConfig(Class<T> clazz, String fileName) {
        return loadConfig(clazz, fileName, plugin.getDataFolder());
    }

    /**
     * Loads a configuration from a specified path.
     *
     * @param clazz    The configuration class.
     * @param fileName The file name.
     * @param path     The path to the folder containing the file.
     * @param <T>      The configuration type.
     * @return The loaded configuration instance.
     */
    protected <T extends AbstractConfig> T loadConfig(Class<T> clazz, String fileName, File path) {
        var configurationStore = new YamlConfigurationStore<>(clazz, properties);
        var config = configurationStore.update(new File(path, fileName).toPath());

        configs.putIfAbsent(path.toPath(), new HashMap<>());
        stores.putIfAbsent(path.toPath(), new HashMap<>());

        var configs = this.configs.get(path.toPath());
        var stores = this.stores.get(path.toPath());

        configs.put(clazz, config);
        stores.put(clazz, configurationStore);

        return config;
    }

    /**
     * Retrieves a Singleton-type configuration instance from the default path.
     *
     * @param clazz The configuration class.
     * @param <T>   The configuration type.
     * @return The configuration instance or {@code null}.
     */
    @CheckClass(AutoRegisterConfig.class)
    public <T extends SingletonConfig> T getConfig(Class<T> clazz) {
        var defaultPath = defaultPaths.getOrDefault(clazz, plugin.getDataFolder().toPath());
        return getConfig(clazz, defaultPath);
    }

    /**
     * Retrieves a configuration instance from a specified path.
     *
     * @param clazz The configuration class.
     * @param path  The path to the folder.
     * @param <T>   The configuration type.
     * @return The configuration instance or {@code null}.
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractConfig> T getConfig(Class<T> clazz, Path path) {
        var configsData = configs.get(path);

        if(configsData == null) return null;

        return (T) configsData.get(clazz);
    }

    /**
     * Retrieves the configuration store for a Singleton-type configuration from the default path.
     *
     * @param clazz The configuration class.
     * @param <T>   The configuration type.
     * @return The {@link YamlConfigurationStore} object or {@code null}.
     */
    @CheckClass(AutoRegisterConfig.class)
    public <T extends SingletonConfig> YamlConfigurationStore<T> getStore(Class<T> clazz) {
        var defaultPath = defaultPaths.getOrDefault(clazz, plugin.getDataFolder().toPath());
        return getStore(clazz, defaultPath);
    }

    /**
     * Retrieves the configuration store from a specified path.
     *
     * @param clazz The configuration class.
     * @param path  The path to the folder.
     * @param <T>   The configuration type.
     * @return The {@link YamlConfigurationStore} object or {@code null} if no store was found for this path and class.
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractConfig> YamlConfigurationStore<T> getStore(Class<T> clazz, Path path) {
        var storesData = stores.get(path);

        if(storesData == null) return null;

        return (YamlConfigurationStore<T>) storesData.get(clazz);
    }

    /**
     * Saves a Singleton-type configuration in the default path.
     *
     * @param clazz The configuration class.
     * @param <T>   The configuration type.
     */
    @CheckClass(AutoRegisterConfig.class)
    public <T extends SingletonConfig> void saveConfig(Class<T> clazz) {
        var defaultPath = defaultPaths.getOrDefault(clazz, plugin.getDataFolder().toPath());
        saveConfig(clazz, defaultPath);
    }

    /**
     * Shorthand method for saving a configuration in a specified path.
     *
     * @param clazz The configuration class.
     * @param path  The path to the folder.
     * @param <T>   The configuration type.
     */
    public <T extends AbstractConfig> void saveConfig(Class<T> clazz, Path path) {
        YamlConfigurations.save(path, clazz, YamlConfigurations.load(path, clazz));
    }

    /**
     * Saves all loaded configurations.
     */
    public void saveAllConfigs() {
        for(var path : configs.keySet()) {
            for(var clazz : configs.get(path).keySet()) {
                saveConfig(clazz, path);
            }
        }
    }

    /**
     * Reloads a Singleton-type configuration in the default path.
     *
     * @param clazz The configuration class.
     * @param <T>   The configuration type.
     * @return {@code true} if the reload was successful, {@code false} otherwise.
     */
    @CheckClass(AutoRegisterConfig.class)
    public <T extends SingletonConfig> boolean reloadConfig(Class<T> clazz) {
        var defaultPath = defaultPaths.getOrDefault(clazz, plugin.getDataFolder().toPath());
        return reloadConfig(clazz, defaultPath);
    }

    /**
     * Reloads a configuration file.
     *
     * @param clazz The configuration class.
     * @param path  The path to the folder.
     * @param <T>   The configuration type.
     * @return {@code true} if the reload was successful, {@code false} otherwise.
     */
    public <T extends AbstractConfig> boolean reloadConfig(Class<T> clazz, Path path) {
        var configsData = configs.get(path);

        if(configsData == null) return false;

        configsData.put(clazz, YamlConfigurations.load(path, clazz));
        return true;
    }

    /**
     * Reloads all loaded configurations.
     */
    public void reloadAllConfigs() {
        for(var path : configs.keySet()) {
            for(var clazz : configs.get(path).keySet()) {
                reloadConfig(clazz, path);
            }
        }
    }

    /**
     * Scans for and loads all classes marked with the {@link AutoRegisterConfig} annotation
     * within the plugin's package.
     * <p>
     * <b>Important:</b> Only classes extending {@link SingletonConfig} will be processed.
     * This method must be called manually (usually in your loader's constructor or onEnable)
     * to initialize these configurations.
     */
    @SuppressWarnings("unchecked")
    public void loadAutoRegisteredConfigs() {
        try(ScanResult scanResult = new ClassGraph()
                .enableAnnotationInfo()
                .acceptPackages(plugin.getClass().getPackageName())
                .scan()) {

            ClassInfoList classesWithAnnotation = scanResult.getClassesWithAnnotation(AutoRegisterConfig.class.getName());

            for(ClassInfo classInfo : classesWithAnnotation) {
                try {
                    Class<?> clazz = classInfo.loadClass();

                    if(!SingletonConfig.class.isAssignableFrom(clazz)) {
                        plugin.getLogger().warning("Class " + clazz.getName() + " has @AutoRegisterConfig but does not extend SingletonConfig - skipping");
                        continue;
                    }

                    AutoRegisterConfig annotation = clazz.getAnnotation(AutoRegisterConfig.class);
                    String fileName = annotation.fileName();
                    String path = annotation.path();
                    Class<? extends SingletonConfig> configClass = (Class<? extends SingletonConfig>) clazz;

                    if(path == null || path.trim().isEmpty()) {
                        loadConfig(configClass, fileName);
                    } else {
                        File customPath = new File(plugin.getDataFolder(), path);

                        if(!customPath.exists())
                            customPath.mkdirs();

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