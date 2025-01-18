package org.alex;

import org.alex.model.*;
import org.alex.storage.Factory;
import org.alex.storage.FileUserStorage;
import org.alex.service.UserService;
import java.util.Optional;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Storage<String, User> userStorage = new FileUserStorage();
        UserService userService = new UserService(userStorage);
        userStorage.add(new User("Vasia", "123"));
        userStorage.add(new User("Petia", "123"));
        userStorage.add(new User("Kilia", "123"));
        userStorage.flush();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Do you want register (1) or log in (2)?");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                System.out.println("Login:");
                String login = scanner.nextLine();
                System.out.println("Password:");
                String password = scanner.nextLine();
                userService.register(login, password);
                break;
            case 2:
                System.out.println("Login:");
                String login1 = scanner.nextLine();
                System.out.println("Password:");
                String password1 = scanner.nextLine();
                Optional<User> user = userService.login(login1, password1);
                if (user.isPresent()) {
                    Wallet wallet = new Wallet.DefaultWallet(user.get(), new Factory.FileFactory());
                    wallet.reload();
                    System.out.println("зашел кошель");
                } else {
                    System.err.println("нет такого числа");
                }
        }
    }
}