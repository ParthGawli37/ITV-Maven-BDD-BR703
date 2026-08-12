package utils;

import java.io.FileInputStream;
import java.util.Properties;

public final class ConfigReader {

    private static final Properties PROP = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            PROP.load(fis);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load config.properties", e);
        }
    }

    private ConfigReader() {
    }

    public static String get(String key) {
        String systemValue = System.getProperty(key);
        return systemValue != null ? systemValue : PROP.getProperty(key);
    }
}
