package com.marlontrujillo.eru.user;

import javafx.beans.property.*;

import javax.persistence.*;

/**
 * Created by mtrujillo on 6/05/14.
 */
@Entity
@Table(name = "user", schema = "public")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type",discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue(value = "USER")
public class User {

    private IntegerProperty id;
    private StringProperty userName;
    private StringProperty firstName;
    private StringProperty lastName;
    private StringProperty email;
    private StringProperty password;
    private BooleanProperty online;
    private StringProperty group;

    public User() {
        this.id         = new SimpleIntegerProperty(0);
        this.userName   = new SimpleStringProperty("");
        this.firstName  = new SimpleStringProperty("");
        this.lastName   = new SimpleStringProperty("");
        this.email      = new SimpleStringProperty("");
        this.password   = new SimpleStringProperty("");
        this.online     = new SimpleBooleanProperty(false);
        this.group      = new SimpleStringProperty("");
    }

    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    public int getId() {
        return id.get();
    }
    public IntegerProperty idProperty() {
        return id;
    }
    public void setId(int id) {
        this.id.set(id);
    }


    public String getUserName() {
        return userName.get();
    }
    public StringProperty userNameProperty() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName.set(userName);
    }

    public String getFirstName() {
        return firstName.get();
    }
    public StringProperty firstNameProperty() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public String getLastName() {
        return lastName.get();
    }
    public StringProperty lastNameProperty() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public String getEmail() {
        return email.get();
    }
    public StringProperty emailProperty() {
        return email;
    }
    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getPassword() {
        return password.get();
    }
    public StringProperty passwordProperty() {
        return password;
    }
    public void setPassword(String password) {
        this.password.set(password);
    }

    public boolean isOnline() {
        return online.get();
    }
    public BooleanProperty onlineProperty() {
        return online;
    }
    public void setOnline(boolean online) {
        this.online.set(online);
    }

    @Column (name = "project_group")
    public String getGroup() {
        return group.get();
    }
    public StringProperty groupProperty() {
        return group;
    }
    public void setGroup(String group) {
        this.group.set(group);
    }

    @Override
    public String toString(){
        return userName + " - " + email;
    }

}
