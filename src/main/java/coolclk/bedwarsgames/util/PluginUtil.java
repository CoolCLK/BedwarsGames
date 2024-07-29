package coolclk.bedwarsgames.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;

public class PluginUtil {
    public static <T extends JavaPlugin> boolean isPluginEnabled(final Class<T> clazz) {
        return isPluginEnabled(getPluginInstance(clazz));
    }

    public static <T extends PluginBase> boolean isPluginEnabled(final T instance) {
        return isPluginEnabled(getPluginName(instance));
    }

    public static boolean isPluginEnabled(final String name) {
        return Bukkit.getPluginManager().isPluginEnabled(name);
    }

    public static <T extends JavaPlugin> String getPluginName(final Class<T> clazz) {
        return getPluginName(getPluginInstance(clazz));
    }

    public static <T extends PluginBase> String getPluginName(final T instance) {
        return instance.getName();
    }

    @SuppressWarnings("unchecked")
    public static <T extends JavaPlugin> T getPluginInstance(final Class<T> clazz) {
        return (T) JavaPlugin.getProvidingPlugin(clazz);
    }

    public static <T extends JavaPlugin> void saveResource(final Class<T> clazz, final String resourcePath, final String savePath) {
        saveResource(clazz, resourcePath, savePath, false);
    }

    public static <T extends JavaPlugin> void saveResource(final Class<T> clazz, final String resourcePath, final String savePath, final boolean replace) {
        File save = new File(getPluginInstance(clazz).getDataFolder(), savePath);
        if (!replace && save.exists() && save.isFile()) {
            return;
        }
        try (InputStream resourceStream = getPluginInstance(clazz).getResource(resourcePath)) {
            try (OutputStream saveStream = Files.newOutputStream(save.toPath())) {
                int resourceByte;
                while ((resourceByte = resourceStream.read()) != -1) {
                    saveStream.write(resourceByte);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends JavaPlugin> void saveResources(final Class<T> clazz, String resourcesPath, final String savePath) {
        saveResources(clazz, resourcesPath, savePath, false);
    }

    public static <T extends JavaPlugin> void saveResources(final Class<T> clazz, String resourcesPath, String savePath, final boolean replace) {
        if (!resourcesPath.endsWith("/")) resourcesPath += "/";
        if (!savePath.endsWith("/")) savePath += "/";
        File save = new File(getPluginInstance(clazz).getDataFolder(), savePath);
        if ((replace || !save.exists() || !save.isDirectory()) && save.mkdir()) {
            for (File f : Objects.requireNonNull(new File(Objects.requireNonNull(clazz.getResource(resourcesPath)).getFile()).listFiles())) {
                if (f.isFile()) {
                    saveResource(clazz, resourcesPath + f.getName(), savePath + f.getName());
                } else if (f.isDirectory()) {
                    saveResources(clazz, resourcesPath + f.getName(), savePath + f.getName());
                }
            }
        }
    }
}
