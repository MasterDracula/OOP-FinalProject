package org.alex.model;


public record User(String login, String password) implements Id<String> {

    @Override
    public String getKey() {
        return login;
    }
}
