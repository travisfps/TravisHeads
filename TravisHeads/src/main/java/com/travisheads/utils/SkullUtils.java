package com.travisheads.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.UUID;

public class SkullUtils {

    public static void applySkullTexture(SkullMeta skullMeta, String url) {
        if (skullMeta == null || url == null || url.isEmpty()) {
            return;
        }

        if (!url.startsWith("http")) {
            url = "https://textures.minecraft.net/texture/" + url;
        }

        try {
            Class<?> profileClass = Class.forName("com.mojang.authlib.GameProfile");
            Object profile = profileClass.getConstructor(UUID.class, String.class)
                    .newInstance(UUID.randomUUID(), null);

            String base64 = Base64.getEncoder().encodeToString(
                    String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes()
            );

            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            Object property = propertyClass.getConstructor(String.class, String.class)
                    .newInstance("textures", base64);

            Method getPropertiesMethod = profileClass.getMethod("getProperties");
            Object properties = getPropertiesMethod.invoke(profile);
            Method putMethod = properties.getClass().getMethod("put", Object.class, Object.class);
            putMethod.invoke(properties, "textures", property);

            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);

        } catch (Exception e) {
            Bukkit.getLogger().warning("Erro ao aplicar textura de skull: " + e.getMessage());
        }
    }
}