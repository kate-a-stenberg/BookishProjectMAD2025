package dev.kateastenberg.bookishproject.models;

/*
This class represents a User.
 */
public class User {

    private String userId, name, email;

    public User(String id, String name, String email) {
        this.userId = id;
        this.email = email;
        this.name = name;
    }

    public String getUserId(){
        return this.userId;
    }

    public String getUserName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setUserId(String id) {
        this.userId = id;
    }

    public void setUserName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
