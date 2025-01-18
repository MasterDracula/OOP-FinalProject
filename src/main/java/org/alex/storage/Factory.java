package org.alex.storage;

import org.alex.model.Category;
import org.alex.model.Storage;
import org.alex.model.User;

public interface Factory {
    Storage<String, User> getUserStorage();

    Storage<String, Category> getCategoryStorage(User user);

    Storage<Long, Category.Value> getCategoryValueStorage(User user, Category category);

    final class FileFactory implements Factory {
        @Override
        public Storage<String, User> getUserStorage() {
            return new FileUserStorage();
        }

        @Override
        public Storage<String, Category> getCategoryStorage(User user) {
            return new FileCategoryStorage(user);
        }

        @Override
        public Storage<Long, Category.Value> getCategoryValueStorage(User user, Category category) {
            return new FileCategoryValueStorage(user, category);
        }
    }
}
