# Configify
[![Version](https://jitpack.io/v/KubawGaming/Configify.svg)](https://jitpack.io/#KubawGaming/Configify)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

Minimalist Spigot/Paper configuration library powered by ConfigLib.

## Installation

### Gradle (Groovy)
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.KubawGaming:Configify:{VERSION}'
    annotationProcessor 'com.github.KubawGaming:Configify:{VERSION}'
}
```

### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.KubawGaming</groupId>
        <artifactId>Configify</artifactId>
        <version>{VERSION}</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>com.github.KubawGaming</groupId>
                        <artifactId>Configify</artifactId>
                        <version>{VERSION}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Usage

### 1. Define a Config
- **SingletonConfig**: Use for unique, global files. Registered via `@AutoRegisterConfig` annotation and loaded using `loadAutoRegisteredConfigs()`.
- **AbstractConfig**: Use for multiple instances. These must be loaded manually using `loadConfig(...)` methods in your loader.

```java
// Singleton example
@AutoRegisterConfig(fileName = "config.yml")
public class MyConfig extends SingletonConfig {
    
    public String message = "Hello World!";
    
}

// Multi-instance example
public class RoundConfig extends AbstractConfig {
    
    public String roundName = "Default";
    public int duration = 300;
    
}
```

### 2. Create a Loader
Extend `AbstractConfigLoader` to register custom serializers and load specific files.
Note: You must implement `configureProperties`.

```java
public class MyConfigLoader extends AbstractConfigLoader {
    
    public MyConfigLoader(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void configureProperties(ConfigurationProperties.Builder<?> builder) {
        // Add custom serializers
        builder.addSerializer(MyType.class, new MySerializer());
    }

    public void loadConfigs() {
        // 1. Load auto-registered singletons. If files are not present, they will be created.
        loadAutoRegisteredConfigs();

        // 2. Manually load specific files. If not present, it will be created.
        loadConfig(RoundConfig.class, "round1.yml", new File(plugin.getDataFolder(), "rounds"));
        loadConfig(RoundConfig.class, "round2.yml", new File(plugin.getDataFolder(), "rounds"));

        // 3. Dynamic loading (Search for existing files)
        // This method scans the folder and loads only existing files matching the map.
        // It does NOT create new files if they don't exist.

        // SearchType.RECURSIVE: Enters each subdirectory of the starting folder
        // and looks for the files there (it does not go deeper into nested subfolders).
        /*
        Visual structure for RECURSIVE search:
        /rounds
           /round1
              config.yml  <-- LOADED
              /nested
                 config.yml  <-- IGNORED
           /round2
              config.yml  <-- LOADED
        */
        loadConfigs(
                new File(plugin.getDataFolder(), "rounds"), 
                SearchType.RECURSIVE, 
                new HashMap<>() {{
                    put("config.yml", RoundConfig.class);
                }}
        );

        // SearchType.FILES: Scans the starting folder directly for the files.
        // It does NOT enter any subdirectories.
        /*
        Visual structure for FILES search:
        /arenas
           arena1.yml  <-- LOADED
           arena2.yml  <-- LOADED
           /subdir
              arena3.yml  <-- IGNORED
        */
        loadConfigs(
                new File(plugin.getDataFolder(), "arenas"),
                SearchType.FILES,
                new HashMap<>() {{
                    put("arena1.yml", RoundConfig.class);
                    put("arena2.yml", RoundConfig.class);
                    put("arena3.yml", RoundConfig.class);
                }}
        );
    }

    // You can create utility methods to simplify access to specific AbstractConfig instances.
    // This is useful if you don't want make getter every time you create new file for your round folder.
    public <T extends AbstractConfig> T getRoundConfig(Class<T> clazz, String roundId) {
        return getConfig(clazz, new File(plugin.getDataFolder(), "rounds/" + roundId).toPath());
    }

    // Or you can make getters specific to file names (depends on your needs).
    // But in this case you need to create a new method every time you add a new file.
    public <T extends AbstractConfig> T getRoundConfig(String roundId) {
        return getConfig(RoundConfig.class, new File(plugin.getDataFolder(), "rounds/" + roundId).toPath());
    }

    public <T extends AbstractConfig> T getRoundMessages(String roundId) {
        return getConfig(RoundMessages.class, new File(plugin.getDataFolder(), "rounds/" + roundId).toPath());
    }
    
}
```

### 3. Initialize Loader
Create your loader in the plugin's `onEnable` and trigger your loading method.

```java
public class MyPlugin extends JavaPlugin {
    
    private MyConfigLoader configLoader;

    @Override
    public void onEnable() {
        configLoader = new MyConfigLoader(this);
        configLoader.loadConfigs();
    }
    
}
```

### 4. Access & Save
```java
// Access singleton
MyConfig config = configLoader.getConfig(MyConfig.class);

// Access specific instance of AbstractConfig by using our utility method in configLoader
RoundConfig round1Config = configLoader.getRoundConfig(RoundConfig.class, "round1");
RoundMessages round1Messages = configLoader.getRoundConfig(RoundMessages.class, "round1");

// Save changes
configLoader.saveConfig(MyConfig.class); // Singleton
```
