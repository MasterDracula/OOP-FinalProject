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

    record Result(Reason reason, String message) {
        public static Result success() {
            return new Result(Reason.OK, null);
        }

        public static Result over(String message) {
            return new Result(Reason.OVER, message);
        }
    }

    enum Reason {
        OVER, OK, NOT_FOUND
    }

    final class DefaultWallet implements Wallet {
        private final User user;
        private final Map<Category, Double> balances = new HashMap<>();
        private final Storage<String, Category> categories;
        private final Map<Category, Storage<Long, Category.Value>> values;
        private final Factory factory;
        private double balance;

        public DefaultWallet(User user, Factory factory) {
            this.factory = factory;
            this.user = user;
            this.categories = factory.getCategoryStorage(user);
            this.values = new HashMap<>();
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
                calculateBalance();
            });
        }

        @Override
        public Result operation(Wallet wallet, String categoryName, Amount amount) {
            double v = amount.value();

            Optional<Category> category = categories.get(categoryName);
            if (category.isEmpty()) {
                return new Result(Reason.NOT_FOUND, String.format("Категория %s не наедена", categoryName));
            }

            if (category.get().kind() == Category.Kind.Output) {
                if (v >= 0.) {
                    v *= -1;
                }
            }

            Category targetCategory = category.get();
            //wallet logic
            Double balance = balances.get(targetCategory);
            if (balance == null) {
                return new Result(Reason.NOT_FOUND, String.format("Баланс по категории %s, не посчитан", categoryName));
            }
            if (v < 0 && balance - v < targetCategory.limit()) {
                return new Result(Reason.OVER, String.format("Баланс по категории %s, не достаточен", categoryName));
            }
            Storage<Long, Category.Value> valueStorage = values.get(targetCategory);
            valueStorage.add(Category.Value.of(v), true);
            this.balance += v;
            balances.put(targetCategory, balance + v);
            return Result.success();
        }
    }
}
