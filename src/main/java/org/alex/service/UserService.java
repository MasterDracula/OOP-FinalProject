package org.alex.service;

import org.alex.model.Storage;
import org.alex.model.User;

import java.util.Optional;

public class UserService {
    private final Storage<String, User> userStorage;

    public UserService(Storage<String, User> userStorage) {
        this.userStorage = userStorage;
    }

    public void register(String login, String password) {
        if (userStorage.get(login).isPresent()) {
            System.err.println("Этот пользователь уже существует.");
            return;
        }
        userStorage.add(new User(login, password), true);
        System.out.println("Пользователь добавлен");
    }

    public Optional<User> login(String login, String password) {
        Optional<User> user = userStorage.get(login);
        if (user.isPresent() && user.get().password().equals(password)) {
            System.out.println("Добро пожаловать");
            return user;
        } else {
            System.err.println("Неверный логин или пароль");
            return Optional.empty();
        }
    }

    public Optional<User> getUserByLogin(String login) {
        return userStorage.get(login);
    }
}
