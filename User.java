package airline.model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int userId;
    private String name;
    private String email;
    private String password;

    public User(int userId, String name, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return "User";
    }

    @Override
    public String toString() {
        return "User ID: " + userId + ", Name: " + name + ", Email: " + email;
    }
}
