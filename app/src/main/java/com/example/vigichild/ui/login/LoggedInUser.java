package com.example.vigichild.ui.login;

public class LoggedInUser {

    String displayName;
    String email;
    String retrieveID;
    String mode;

    public LoggedInUser(String displayName, String email, String retrieveID) {
        this.displayName = displayName;
        this.email = email;
        this.retrieveID = retrieveID;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRetrieveID() {
        return retrieveID;
    }

    public void setRetrieveID(String retrieveID) {
        this.retrieveID = retrieveID;
    }
}
