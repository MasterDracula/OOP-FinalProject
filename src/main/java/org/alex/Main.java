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
        userStorage.flush();

        Scanner scanner = new Scanner(System.in);

        boolean running = true;

        while (running) {
            System.out.println("Хотите зарегистрироваться (1) или войти (2)?");
            int choice = getIntInput(scanner);

            switch (choice) {
                case 1:
                    System.out.println("Логин:");
                    String login = scanner.nextLine();
                    System.out.println("Пароль:");
                    String password = scanner.nextLine();
                    userService.register(login, password);
                    break;
                case 2:
                    System.out.println("Логин:");
                    String login1 = scanner.nextLine();
                    System.out.println("Пароль:");
                    String password1 = scanner.nextLine();
                    Optional<User> user = userService.login(login1, password1);
                    if (user.isPresent()) {
                        Wallet wallet = new Wallet.DefaultWallet(user.get(), new Factory.FileFactory());
                        wallet.reload();
                        System.out.println("Добро пожаловать в ваш кошелек!");

                        boolean walletRunning = true;
                        while (walletRunning) {
                            System.out.println("Выберите действие: 1 Добавить категорию 2 Добавить доход 3 Добавить расход 4 Просмотреть баланс 5 Выйти 6 Перевод средств");
                            int action = getIntInput(scanner);

                            switch (action) {
                                case 1:
                                    System.out.println("Введите название категории:");
                                    String categoryName = scanner.nextLine();
                                    System.out.println("Введите тип категории (Input/Output):");
                                    String kind = scanner.nextLine();
                                    Double limit = null;
                                    if (Category.Kind.valueOf(kind) == Category.Kind.Output) {
                                        System.out.println("Введите лимит категории:");
                                        limit = getDoubleInput(scanner);
                                    }
                                    wallet.createCategory(categoryName, Category.Kind.valueOf(kind), limit);
                                    System.out.println("Категория добавлена.");
                                    break;
                                case 2:
                                    System.out.println("Введите категорию:");
                                    String incomeCategory = scanner.nextLine();
                                    System.out.println("Введите сумму:");
                                    double incomeAmount = getDoubleInput(scanner);
                                    Wallet.Result resultIncome = wallet.operation(wallet, incomeCategory, new Amount(incomeAmount, Amount.Currency.RUB));
                                    if (resultIncome.reason() == Wallet.Reason.OK) {
                                        System.out.println("Доход добавлен.");
                                    } else {
                                        System.out.println("Ошибка: " + resultIncome.message());
                                    }
                                    break;
                                case 3:
                                    System.out.println("Введите категорию:");
                                    String expenseCategory = scanner.nextLine();
                                    System.out.println("Введите сумму:");
                                    double expenseAmount = getDoubleInput(scanner);
                                    Wallet.Result resultExpense = wallet.operation(wallet, expenseCategory, new Amount(-expenseAmount, Amount.Currency.RUB));
                                    if (resultExpense.reason() == Wallet.Reason.OK) {
                                        System.out.println("Расход добавлен.");
                                    } else if (resultExpense.reason() == Wallet.Reason.BUDGET_EXCEEDED) {
                                        System.out.println("Ошибка: " + resultExpense.message());
                                    } else if (resultExpense.reason() == Wallet.Reason.EXPENSES_EXCEED_INCOME) {
                                        System.out.println("Ошибка: " + resultExpense.message());
                                    } else {
                                        System.out.println("Ошибка: " + resultExpense.message());
                                    }
                                    break;
                                case 4:
                                    wallet.printSummary();
                                    break;
                                case 5:
                                    walletRunning = false;
                                    System.out.println("Выход из кошелька...");
                                    break;
                                case 6:
                                    System.out.println("Введите логин получателя:");
                                    String recipientLogin = scanner.nextLine();
                                    System.out.println("Введите категорию перевода:");
                                    String transferCategory = scanner.nextLine();
                                    System.out.println("Введите сумму:");
                                    double transferAmount = getDoubleInput(scanner);
                                    Optional<User> recipient = userService.getUserByLogin(recipientLogin);
                                    if (recipient.isPresent()) {
                                        Wallet.Result resultTransfer = wallet.transferFunds(recipient.get(), transferCategory, new Amount(transferAmount, Amount.Currency.RUB));
                                        if (resultTransfer.reason() == Wallet.Reason.OK) {
                                            System.out.println("Перевод выполнен.");
                                        } else {
                                            System.out.println("Ошибка: " + resultTransfer.message());
                                        }
                                    } else {
                                        System.out.println("Получатель не найден.");
                                    }
                                    break;
                                default:
                                    System.out.println("Неверное действие.");
                                    break;
                            }
                        }
                    } else {
                        System.err.println("Неправильный логин или пароль");
                    }
                    break;
                default:
                    System.out.println("Неверный выбор.");
                    break;
            }
        }
        scanner.close();
    }

    private static int getIntInput(Scanner scanner) {
        while (true) {
            try {
                int input = scanner.nextInt();
                scanner.nextLine();
                return input;
            } catch (java.util.InputMismatchException e) {
                System.out.println("Неверное действие. Пожалуйста, введите число.");
                scanner.nextLine();
            }
        }
    }

    private static double getDoubleInput(Scanner scanner) {
        while (true) {
            try {
                double input = scanner.nextDouble();
                scanner.nextLine();
                return input;
            } catch (java.util.InputMismatchException e) {
                System.out.println("Неверное действие. Пожалуйста, введите число.");
                scanner.nextLine();
            }
        }
    }
}