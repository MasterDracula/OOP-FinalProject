package org.alex.model;

import java.util.Date;

public record Category(String name, Kind kind, double limit) implements Id<String>, Comparable<Category>{

    @Override
    public String getKey() {
        return name;
    }

    @Override
    public int compareTo(Category o) {
        return name.compareTo(o.name);
    }

    public enum Kind {
        Input, Output
    }

    public record Value(Amount amount, Long date) implements Id<Long> {

        @Override
        public Long getKey() {
            return date;
        }
        public static Value of(double amount){
            return new Value(new Amount(amount), new Date().getTime());
        }
    }
}
