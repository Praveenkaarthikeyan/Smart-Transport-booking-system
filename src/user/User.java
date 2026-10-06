package user;

public abstract class User {

    protected String id;
    protected String name;

    protected User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public abstract void login();

    public abstract void logout();
}