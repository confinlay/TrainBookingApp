package be.kuleuven.distributedsystems.cloud.entities;

import java.util.Arrays;

public class User {

    private String email;
    private String[] roles;

    public User(String email, String[] roles) {
        this.email = email;
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public String[] getRoles() {
        return roles;
    }

    public boolean isManager() {
        return this.roles != null && Arrays.stream(this.roles).toList().contains("manager");
    }
}
