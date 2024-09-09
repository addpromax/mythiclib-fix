package io.lumine.mythic.lib.util.configobject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class JsonWrapper implements ConfigObject {
    protected final String key;
    protected final JsonObject object;

    public JsonWrapper(String key, JsonObject object) {
        this.key = key;
        this.object = object;
    }

    protected JsonWrapper(String value) {

        /*
         * If there is no config, no need to parse the Json object.
         * Split, define key and find arguments
         */
        if (!value.contains("{") || !value.contains("}")) {
            object = new JsonObject();
            key = null;
            return;
        }

        // Load Json object
        final int begin = value.indexOf("{"), end = value.lastIndexOf("}") + 1;
        object = JsonParser.parseString(value.substring(begin, end)).getAsJsonObject();
        key = nullify(value.substring(0, begin));
    }

    @Nullable
    private String nullify(@Nullable String str) {
        return str == null || str.isEmpty() ? null : str;
    }

    @Override
    public String getString(String key) {
        return object.get(key).getAsString();
    }

    @Override
    public String getString(String key, String defaultValue) {
        return object.has(key) ? getString(key) : defaultValue;
    }

    @Override
    public double getDouble(String key) {
        return object.get(key).getAsDouble();
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return object.has(key) ? getDouble(key) : defaultValue;
    }

    @Override
    public int getInt(String key) {
        return object.get(key).getAsInt();
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return object.has(key) ? getInt(key) : defaultValue;
    }

    @NotNull
    @Override
    public ConfigObject adaptObject(String key) {
        final JsonElement found = object.get(key);

        final JsonObject loadFrom;
        if (found instanceof JsonObject) loadFrom = found.getAsJsonObject();
        else if (found instanceof JsonPrimitive) {
            loadFrom = new JsonObject();
            loadFrom.addProperty("type", found.getAsString());
        } else throw new IllegalArgumentException("Expecting either a string or object");

        return new JsonWrapper(key, loadFrom);
    }

    @Override
    public boolean getBoolean(String key) {
        return object.get(key).getAsBoolean();
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return object.has(key) ? getBoolean(key) : defaultValue;
    }

    @NotNull
    @Override
    public ConfigObject getObject(String key) {
        return new JsonWrapper(key, object.getAsJsonObject(key));
    }

    @Override
    public boolean contains(String key) {
        return object.has(key);
    }

    @NotNull
    @Override
    public Set<String> getKeys() {
        return object.keySet();
    }

    @Override
    public String getKey() {
        return key;
    }
}
