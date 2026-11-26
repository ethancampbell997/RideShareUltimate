
package edu.uga.cs.rideshareultimate;

import android.os.Bundle;
import android.app.AlertDialog;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Allows drivers to accept posted ride requests
 */
public class AcceptRideRequestActivity extends AppCompatActivity {

    private TextView tvRideDetails;
    private Button btnAcceptRide;

    private DatabaseReference ridesRef;
    private String rideId;

    private FirebaseUser currentUser;
    private Ride currentRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_ride_request);

        tvRideDetails = findViewById(R.id.tvRideDetails);
        btnAcceptRide = findViewById(R.id.btnAcceptRide);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (currentUser == null) {
            Toast.makeText(this,
                    "You must be logged in.",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ridesRef = FirebaseDatabase.getInstance().getReference("rides");

        rideId = getIntent().getStringExtra("rideId");

        if (rideId == null) {
            Toast.makeText(this,
                    "Ride request not found.",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnAcceptRide.setEnabled(false);

        loadRide();

        btnAcceptRide.setOnClickListener(v -> confirmAcceptance());
    }

    /**
     * Loads the selected ride request from Firebase
     */
    private void loadRide() {

        ridesRef.child(rideId)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        currentRide = snapshot.getValue(Ride.class);

                        if (currentRide == null) {
                            Toast.makeText(AcceptRideRequestActivity.this,
                                    "This ride request no longer exists.",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        if (!"request".equals(currentRide.getRideType()) ||
                                (currentRide.getDriverId() != null &&
                                        !currentRide.getDriverId().isEmpty())) {

                            Toast.makeText(AcceptRideRequestActivity.this,
                                    "This ride request is no longer available.",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        if (currentUser.getUid().equals(currentRide.getRiderId())) {
                            Toast.makeText(AcceptRideRequestActivity.this,
                                    "You cannot accept your own ride request.",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        displayRide();
                        btnAcceptRide.setEnabled(true);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(AcceptRideRequestActivity.this,
                                "Error loading ride: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Displays ride request information.
     */
    private void displayRide() {

        String pickupDate = new SimpleDateFormat(
                "MMM d, yyyy 'at' h:mm a",
                Locale.getDefault()
        ).format(new Date(currentRide.getPickupDateTime()));

        String rideDetails =
                "From: " + currentRide.getStartLocation() +
                        "\n\nTo: " + currentRide.getEndLocation() +
                        "\n\nRider: " + currentRide.getRiderEmail() +
                        "\n\nPickup: " + pickupDate +
                        "\n\nCost: " + currentRide.getCost() + " ride points" +
                        "\n\nDescription: " + currentRide.getDescription();

        tvRideDetails.setText(rideDetails);
    }

    /**
     * Confirms that the user wants to accept the ride request
     */
    private void confirmAcceptance() {

        if (currentRide == null) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Accept Ride Request")
                .setMessage("Are you sure you want to accept this ride request?")
                .setPositiveButton("Accept", (dialog, which) -> acceptRide())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Accepts the ride request and records the driver's information
     */
    private void acceptRide() {

        btnAcceptRide.setEnabled(false);

        ridesRef.child(rideId).runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(
                    com.google.firebase.database.MutableData rideData) {

                Ride ride = rideData.getValue(Ride.class);

                if (ride == null ||
                        !"request".equals(ride.getRideType()) ||
                        (ride.getDriverId() != null &&
                                !ride.getDriverId().isEmpty()) ||
                        currentUser.getUid().equals(ride.getRiderId())) {

                    return Transaction.abort();
                }

                // Record the driver's information.
                ride.setDriverId(currentUser.getUid());
                ride.setDriverEmail(currentUser.getEmail());

                rideData.setValue(ride);

                return Transaction.success(rideData);
            }

            @Override
            public void onComplete(
                    DatabaseError error,
                    boolean committed,
                    DataSnapshot snapshot) {

                btnAcceptRide.setEnabled(true);

                if (error != null) {

                    Toast.makeText(AcceptRideRequestActivity.this,
                            "Error accepting ride request: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();

                } else if (committed) {

                    Toast.makeText(AcceptRideRequestActivity.this,
                            "Ride request accepted successfully!",
                            Toast.LENGTH_SHORT).show();

                    finish();

                } else {

                    Toast.makeText(AcceptRideRequestActivity.this,
                            "This ride request is no longer available.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
