package org.alex.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Storage<K, V> {
    void reload();

    void flush();

    void add(V value, boolean persist);

    default void add(V value) {
        add(value, false);
    }

    default void delete(K key) {
        delete(key, false);
    }

    List<V> values();

    void delete(K key, boolean persist);

    Optional<V> get(K key);

    abstract class AbstractStorage<K, V extends Id<K>> implements Storage<K, V> {
        private final Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .enableComplexMapKeySerialization()
                .create();
        private final Map<K, V> entities = new HashMap<>();
        private final Path path;

        protected AbstractStorage(String fileName) {
            this.path = Paths.get(fileName);
            reload();
        }

        @Override
        public void reload() {
            try {
                if (!Files.exists(path)) {
                    return;
                }
                Map<K, V> loaded = gson.fromJson(Files.readString(path, StandardCharsets.UTF_8), getType());
                if (entities.equals(loaded)) {
                    return;
                }
                if (entities.isEmpty()) {
                    entities.putAll(loaded);
                } else {
                    System.err.println("Сущности не загружены");
                    entities.clear();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void flush() {
            String text = gson.toJson(entities, getType());
            try {
                Files.writeString(this.path, text, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Optional<V> get(K key) {
            return Optional.ofNullable(entities.get(key));
        }

        @Override
        public void add(V value, boolean persist) {
            K key = value.getKey();
            if (entities.containsKey(key)) {
                System.err.println("Key " + key + " already exists");
            }
            entities.put(key, value);
            if (persist) {
                flush();
            }
        }

        @Override
        public List<V> values() {
            return entities.values().stream().toList();
        }

        @Override
        public void delete(K key, boolean persist) {
            entities.remove(key);
            if (persist) {
                flush();
            }
        }

        protected abstract Type getType();
    }
}
