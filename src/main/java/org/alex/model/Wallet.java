package org.alex.model;

import org.alex.storage.Factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface Wallet {
    double balance();
    void reload();
    Result operation(Wallet wallet, String category, Amount amount);

    default Result operation(String category, Amount amount) {
        return operation(this, category, amount);
    }

    void createCategory(String name, Category.Kind kind, Double limit);
    void printSummary();
    Result transferFunds(User recipient, String category, Amount amount);

    record Result(Reason reason, String message) {
        public static Result success() {
            return new Result(Reason.OK, null);
        }

        public static Result over(String message) {
            return new Result(Reason.OVER, message);
        }

        public static Result budgetExceeded(String message) {
            return new Result(Reason.BUDGET_EXCEEDED, message);
        }

        public static Result expensesExceedIncome(String message) {
            return new Result(Reason.EXPENSES_EXCEED_INCOME, message);
        }

        public static Result notFound(String message) {
            return new Result(Reason.NOT_FOUND, message);
        }
    }

    enum Reason {
        OVER, OK, NOT_FOUND, BUDGET_EXCEEDED, EXPENSES_EXCEED_INCOME
    }

    final class DefaultWallet implements Wallet {
        private final User user;
        private final Map<Category, Double> balances = new HashMap<>();
        private final Storage<String, Category> categories;
        private final Map<Category, Storage<Long, Category.Value>> values = new HashMap<>();
        private final Factory factory;
        private double balance;

        public DefaultWallet(User user, Factory factory) {
            this.factory = factory;
            this.user = user;
            this.categories = factory.getCategoryStorage(user);
            reload();
        }

        private void calculateBalance() {
            balances.clear();
            this.values.forEach((category, storage) -> {
                balances.put(category, storage.values().stream().map(v -> v.amount().value()).reduce(0., Double::sum));
            });
            balance = this.balances.values().stream().reduce(0., Double::sum);
        }

        @Override
        public double balance() {
            return balance;
        }

        @Override
        public void reload() {
            this.values.clear();
            categories.reload();
            categories.values().forEach(category -> {
                this.values.put(category, factory.getCategoryValueStorage(user, category));
            });
            calculateBalance();
        }

        @Override
        public Result operation(Wallet wallet, String categoryName, Amount amount) {
            double v = amount.value();

            Optional<Category> category = categories.get(categoryName);
            if (category.isEmpty()) {
                return new Result(Reason.NOT_FOUND, String.format("Категория %s не найдена", categoryName));
            }

            if (category.get().kind() == Category.Kind.Output && v > 0) {
                v *= -1;
            }

            Category targetCategory = category.get();
            Double balance = balances.get(targetCategory);
            if (balance == null) {
                balance = 0.0;
            }

            if (v < 0 && balance + v < -targetCategory.limit()) {
                return new Result(Reason.OVER, String.format("Бюджет по категории %s недостаточен", categoryName));
            }

            Storage<Long, Category.Value> valueStorage = values.get(targetCategory);
            valueStorage.add(Category.Value.of(v), true);
            balances.put(targetCategory, balance + v);
            calculateBalance();

            if (v < 0 && balance + v < -targetCategory.limit()) {
                return Result.budgetExceeded(String.format("Лимит бюджета по категории %s превышен", categoryName));
            }

            if (getTotalExpense() > getTotalIncome()) {
                return Result.expensesExceedIncome("Расходы превышают доходы!");
            }

            return Result.success();
        }

        @Override
        public void createCategory(String name, Category.Kind kind, Double limit) {
            if (categories.get(name).isPresent()) {
                System.out.println("Категория с таким названием уже существует.");
                return;
            }

            if (kind == Category.Kind.Input) {
                limit = 0.0; // на доход лимит не нужен
            }

            Category category = new Category(name, kind, limit);
            categories.add(category, true);
            values.put(category, factory.getCategoryValueStorage(user, category));
            calculateBalance();
        }

        @Override
        public void printSummary() {
            System.out.println("Общий доход: " + getTotalIncome());
            System.out.println("Общие расходы: " + getTotalExpense());
            System.out.println("Доходы по категориям:");
            balances.forEach((category, amount) -> {
                if (category.kind() == Category.Kind.Input) {
                    System.out.println(category.name() + ": " + amount);
                }
            });
            System.out.println("Расходы по категориям и остаток бюджета:");
            balances.forEach((category, amount) -> {
                if (category.kind() == Category.Kind.Output) {
                    double remainingBudget = category.limit() + amount;
                    System.out.println(category.name() + ": " + amount + ", Оставшийся бюджет: " + remainingBudget);
                }
            });
        }

        public double getTotalIncome() {
            return balances.entrySet().stream()
                    .filter(entry -> entry.getKey().kind() == Category.Kind.Input)
                    .mapToDouble(Map.Entry::getValue)
                    .sum();
        }

        public double getTotalExpense() {
            return balances.entrySet().stream()
                    .filter(entry -> entry.getKey().kind() == Category.Kind.Output)
                    .mapToDouble(Map.Entry::getValue)
                    .sum();
        }

        @Override
        public Result transferFunds(User recipient, String category, Amount amount) {
            Wallet recipientWallet = new DefaultWallet(recipient, factory);

            Result resultExpense = operation(this, category, new Amount(-amount.value(), amount.currency()));
            if (resultExpense.reason() != Reason.OK) {
                return resultExpense;
            }

            Result resultIncome = recipientWallet.operation(recipientWallet, category, amount);
            if (resultIncome.reason() != Reason.OK) {
                operation(this, category, amount);
                return resultIncome;
            }

            return Result.success();
        }

        public Map<Category, Double> getBalances() {
            return balances;
        }
    }
}
