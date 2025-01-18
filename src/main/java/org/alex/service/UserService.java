package org.alex.service;

import org.alex.model.Storage;
import org.alex.model.User;

import java.util.Optional;

public class UserService {
    private final Storage<String, User> userStorage;

    public UserService(Storage<String, User> userStorage) {
        this.userStorage = userStorage;
    }
    public  void register(String login, String password){
        if(userStorage.get(login).isPresent()){
            System.err.println("This user already exist");
            return;
        }
        userStorage.add(new User(login, password), true); // как бы говорим, что бы он данные пользователя сразу сохранил в файл
        System.out.println("User added");
    }
    public Optional<User> login(String login, String password){
        Optional<User> user = userStorage.get(login);
        if (user.isPresent()&&user.get().password().equals(password)){
            System.out.println("Welcome");
            return user;
        }else {
            System.err.println("Wrong password or login");
            return Optional.empty();
        }
    }
}
