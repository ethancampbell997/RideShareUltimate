
package edu.uga.cs.rideshareultimate;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Allows users to manage their posted rides.
 */
public class ManageRideActivity extends AppCompatActivity {

    private TextView tvRideDetails;
    private Button btnEditRide, btnDeleteRide;

    private DatabaseReference rideRef;
    private String rideId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_ride);

        tvRideDetails = findViewById(R.id.tvRideDetails);
        btnEditRide = findViewById(R.id.btnEditRide);
        btnDeleteRide = findViewById(R.id.btnDeleteRide);

        currentUserId = FirebaseAuth.getInstance()
                .getCurrentUser().getUid();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Retrieve ride ID passed from ViewMyRidesActivity
        rideId = getIntent().getStringExtra("rideId");

        if (rideId == null) {
            Toast.makeText(this, "Ride not found.",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rideRef = FirebaseDatabase.getInstance()
                .getReference("rides")
                .child(rideId);

        loadRide();

        btnEditRide.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ManageRideActivity.this,
                    EditRideActivity.class
            );

            intent.putExtra("rideId", rideId);
            startActivity(intent);
        });

        btnDeleteRide.setOnClickListener(v -> confirmDelete());
    }

    private void loadRide() {

        rideRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Ride ride = snapshot.getValue(Ride.class);

                if (ride == null) {
                    Toast.makeText(ManageRideActivity.this,
                            "This ride no longer exists.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Only allow the owner to manage the ride
                boolean isOwner =
                        currentUserId.equals(ride.getDriverId()) ||
                                currentUserId.equals(ride.getRiderId());

                if (!isOwner) {
                    Toast.makeText(ManageRideActivity.this,
                            "You cannot manage this ride.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                String pickupDate = new SimpleDateFormat(
                        "MMM d, yyyy 'at' h:mm a",
                        Locale.getDefault()
                ).format(new Date(ride.getPickupDateTime()));

                String userEmail = ride.getDriverId() != null
                        ? ride.getDriverEmail()
                        : ride.getRiderEmail();

                String rideDetails =
                        "Ride Type: " + ride.getRideType().toUpperCase() +
                                "\n\nFrom: " + ride.getStartLocation() +
                                "\n\nTo: " + ride.getEndLocation() +
                                "\n\nPickup: " + pickupDate +
                                "\n\nPosted By: " + userEmail +
                                "\n\nAvailable Seats: " + ride.getAvailableSeats() +
                                "\n\nCost: " + ride.getCost() + " ride points" +
                                "\n\nDescription: " + ride.getDescription();

                tvRideDetails.setText(rideDetails);
            }

            @Override
            public void onCancelled(DatabaseError error) {

                Toast.makeText(ManageRideActivity.this,
                        "Error: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmDelete() {

        new AlertDialog.Builder(this)
                .setTitle("Delete Ride")
                .setMessage("Are you sure you want to delete this ride?")
                .setPositiveButton("Delete", (dialog, which) -> deleteRide())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteRide() {

        rideRef.removeValue().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                Toast.makeText(ManageRideActivity.this,
                        "Ride deleted successfully!",
                        Toast.LENGTH_SHORT).show();

                finish();

            } else {

                Toast.makeText(ManageRideActivity.this,
                        "Failed to delete ride.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Implement Back button listener method.
        // This method may be used for other actions from the ActionBar menu, if provided in the layout.
        int id = item.getItemId();

        // android.R.id.home is the built-in designation of the back button widget.
        if( id == android.R.id.home ) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected( item );
    }
}
