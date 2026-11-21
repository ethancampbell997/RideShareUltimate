package edu.uga.cs.rideshareultimate;

/**
 * Represents a registered user in the Firebase Realtime Database.
 */
public class User {
    private String uid;
    private String email;
    private int ridePoints;

    // Required empty constructor for Firebase
    public User() {
    }

    public User(String uid, String email, int ridePoints) {
        this.uid = uid;
        this.email = email;
        this.ridePoints = ridePoints;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRidePoints() {
        return ridePoints;
    }

    public void setRidePoints(int ridePoints) {
        this.ridePoints = ridePoints;
    }
}