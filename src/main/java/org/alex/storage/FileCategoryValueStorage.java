package org.alex.storage;

import com.google.gson.reflect.TypeToken;
import org.alex.model.Category;
import org.alex.model.Storage;
import org.alex.model.User;

import java.lang.reflect.Type;
import java.util.Map;

public final class FileCategoryValueStorage extends Storage.AbstractStorage<Long, Category.Value> {
    private static final Type TYPE = new TypeToken<Map<Long, Category.Value>>() {
    }.getType();

    public FileCategoryValueStorage(User user, Category category) {
        super(String.format("%s-%s-categories-values.json", user.login(), category.name()));
    }

    @Override
    protected Type getType() {
        return TYPE;
    }
}
