package org.alex.storage;

import com.google.gson.reflect.TypeToken;
import org.alex.model.Storage;
import org.alex.model.User;

import java.lang.reflect.Type;
import java.util.Map;

public final class FileUserStorage extends Storage.AbstractStorage<String, User> {
    private static final Type TYPE = new TypeToken<Map<String, User>>() {
    }.getType();

    public FileUserStorage() {
        super("users.json");
    }

    @Override
    protected Type getType() {
        return TYPE;
    }
}
