package com.mcmah113.mcmah113expensesiq;

public class User {
    private int userId;
    private String username;
    private String password;
    private String language;
    private String locale;

    public User(int userId, String username, String password,
         String language, String locale) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.language = language;
        this.locale = locale;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getLanguage() {
        return language;
    }

    public String getLocale() {
        return locale;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
