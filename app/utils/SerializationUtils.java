package utils;

import com.google.common.collect.Maps;
import com.google.gson.*;
import exceptions.SystemException;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.libs.Codec;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

public class SerializationUtils {
    private static final Gson SERIALIZER;

    static {
        SERIALIZER = new Gson();
    }

    private SerializationUtils() {
    }

    public static  <T> T deserialize(final String json, Type type) {
        return SERIALIZER.fromJson(json, type);
    }

    public static String serialize(final Object obj) {
        return SERIALIZER.toJson(obj);
    }

    public static void checkRequired(final Object obj) {
        for (Class<?> c = obj.getClass(); c != null; c = c.getSuperclass()) {
            for (java.lang.reflect.Field field : c.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(Required.class) && field.get(obj) == null) {
                        throw new IllegalStateException(field.getName() + " is required");
                    }
                } catch (IllegalAccessException e) {
                    throw new SystemException(e);
                }
            }
        }
    }

    /**
     * Gets the md5 hash of string value
     *
     * @param value
     * @return the md5 hash of the value
     */
    public static String md5(String value) {
        return StringUtils.isBlank(value) ? null : Codec.hexMD5(value.trim().toLowerCase());
    }

    /**
     * Gets the Adler32 value for a set of bytes
     *
     * @param bytes input bytes
     * @return Adler32 value
     */
    public static long getAdler32Checksum(byte[] bytes) {
        Checksum adler32 = new Adler32();
        adler32.update(bytes, 0, bytes.length);
        return adler32.getValue();
    }

    public static boolean isJSONValid(final String json) {
        try {
            new JsonParser().parse(json);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }

    public static JsonObject toJSON(String json) {
        return new Gson().fromJson(json, JsonElement.class).getAsJsonObject();
    }

    public static String toJSON(Map<String, List<String>> map) {
        Map<String, String> newMap = Maps.newHashMap();
        map.forEach((k, v) -> newMap.put(k, v.get(0)));
        return new Gson().toJson(newMap);
    }

    public static JsonArray toJSONArray(String json) {
        return new Gson().fromJson(json, JsonArray.class).getAsJsonArray();
    }
}
