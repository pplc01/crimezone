package com.ppl.crimezone.model;

/**
 * Created by adesudiman on 3/29/2014.
 */
public class User {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private boolean twiiterLoginStatus;
    private boolean fbLoginStatus;


    public User(String username, String email, String password){
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(String username, String email, String password, String firstName, String lastName, boolean twiiterLoginStatus, boolean fbLoginStatus){
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.twiiterLoginStatus = twiiterLoginStatus;
        this.fbLoginStatus = fbLoginStatus;
    }


    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    public String getEmail(){
        return email;
    }
    public String getFirstName(){
        return firstName;
    }
    public String getLastName(){
        return lastName;
    }
    public boolean getTwitterLoginStatus(){
        return twiiterLoginStatus;
    }
    public boolean getFbLoginStatus(){
        return fbLoginStatus;
    }
    public void setFirstName(String firstName){
       this.firstName = firstName;
    }

    public void setLastName(String lastName){
       this.lastName = lastName;
    }











}
