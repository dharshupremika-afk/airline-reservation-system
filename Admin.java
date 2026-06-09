package airline.model;

public class Admin extends User {
    private static final long serialVersionUID = 1L;

    public Admin(int userId, String name, String email, String password) {
        super(userId, name, email, password);
    }

    @Override
    public String getRole() {
        return "Admin";
    }
}
