package net.jcom.minecraft.paperdjbattle.config;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@SuppressWarnings({"unchecked"})
public class DefaultsManager {
    private static Plugin myPlugin = null;

    public static void init(Plugin plugin, Class<?> defaultClass) {
        myPlugin = plugin;

        var fields = findPairFields(defaultClass);
        for (var field : fields) {
            field.setAccessible(true);
            try {
                var pair = (Pair<String, Object>) field.get(defaultClass);
                plugin.getConfig().addDefault(pair.getLeft(), pair.getRight());
            } catch (IllegalAccessException e) {
                plugin.getLogger().log(Level.WARNING, "Skipping " + field.getName());
            }
        }

        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    public static List<Field> findPairFields(Class<?> clazz) {
        List<Field> pairFields = new ArrayList<>();

        // Get all declared fields of the class, including inherited fields
        Field[] fields = clazz.getDeclaredFields();

        // Iterate over the fields
        for (Field field : fields) {
            // Check if the field type is Pair<String, Object>
            if (field.getType() == Pair.class) {
                // Get the type arguments of the field's generic type
                Class<?> leftType = String.class;
                Class<?> rightType = Object.class;

                if (field.getGenericType() instanceof ParameterizedType parameterizedType) {

                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    if (typeArguments.length == 2) {
                        if (typeArguments[0] instanceof Class<?> && typeArguments[1] instanceof Class<?>) {
                            leftType = (Class<?>) typeArguments[0];
                            rightType = (Class<?>) typeArguments[1];
                        }
                    }
                }

                // Add the field to the list if it matches the desired Pair<String, Object> type
                if (leftType == String.class && rightType == Object.class) {
                    pairFields.add(field);
                }
            }
        }

        return pairFields;
    }


    private static String getKey(Pair<String, Object> varName) {
        return varName.getLeft();
    }

    public static <T> T getValue(Pair<String, Object> varName) {
        var key = getKey(varName);
        return (T) myPlugin.getConfig().get(key);
    }
}