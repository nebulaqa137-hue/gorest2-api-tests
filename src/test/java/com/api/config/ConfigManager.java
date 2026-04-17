package com.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigManager — carga config.properties una sola vez (Singleton).
 * Acceso global sin repetir FileReader en cada test.
 */
public class ConfigManager {

    private static final String CONFIG_FILE = "config.properties";
    private static ConfigManager instance;
    private final Properties properties;

    private ConfigManager() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("No se encontró " + CONFIG_FILE + " en resources");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error cargando configuración: " + e.getMessage(), e);
        }
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public String get(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Propiedad no encontrada o vacía: " + key);
        }
        return value.trim();
    }

    public String getBaseUrl()       { return get("base.url"); }
    public String getValidToken()    { return get("valid.token"); }
    public String getInvalidToken()  { return get("invalid.token"); }
}
