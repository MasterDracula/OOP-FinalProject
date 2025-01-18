package org.alex.storage;

import com.google.gson.reflect.TypeToken;
import org.alex.model.Category;
import org.alex.model.Storage;
import org.alex.model.User;

import java.lang.reflect.Type;
import java.util.Map;

public class FileCategoryStorage extends Storage.AbstractStorage<String, Category> {
    private static final Type TYPE = new TypeToken<Map<String, Category>>() {
    }.getType();

    public FileCategoryStorage(User user) {
        super(String.format("%s-categories.json", user.login()));
    }

    @Override
    protected Type getType() {
        return TYPE;
    }
}
