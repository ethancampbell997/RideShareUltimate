
package edu.uga.cs.rideshareultimate;

/**
 * Represents a posted ride in the Firebase Realtime Database.
 */
public class Ride {
    private String rideId;
    private String driverId;
    private String riderId;
    private String driverEmail;
    private String startLocation;
    private String endLocation;
    private long pickupDateTime;
    private long postedDateTime;
    private int availableSeats;
    private int cost;
    private String description;

    // Required empty constructor for Firebase
    public Ride() {
    }

    public Ride(String rideId, String driverId, String riderId,
                String driverEmail, String startLocation,
                String endLocation, long pickupDateTime,
                long postedDateTime, int availableSeats,
                int cost, String description) {
        this.rideId = rideId;
        this.driverId = driverId;
        this.riderId = riderId;
        this.driverEmail = driverEmail;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.pickupDateTime = pickupDateTime;
        this.postedDateTime = postedDateTime;
        this.availableSeats = availableSeats;
        this.cost = cost;
        this.description = description;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getRiderId() {
        return riderId;
    }

    public void setRiderId(String riderId) {
        this.riderId = riderId;
    }

    public String getDriverEmail() {
        return driverEmail;
    }

    public void setDriverEmail(String driverEmail) {
        this.driverEmail = driverEmail;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public long getPickupDateTime() {
        return pickupDateTime;
    }

    public void setPickupDateTime(long pickupDateTime) {
        this.pickupDateTime = pickupDateTime;
    }

    public long getPostedDateTime() {
        return postedDateTime;
    }

    public void setPostedDateTime(long postedDateTime) {
        this.postedDateTime = postedDateTime;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
