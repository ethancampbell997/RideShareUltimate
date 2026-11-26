
package edu.uga.cs.rideshareultimate;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Allows users to edit their unaccepted ride offers and requests
 */
public class EditRideActivity extends AppCompatActivity {

    private EditText etStartLocation, etEndLocation;
    private EditText etPickupDateTime;
    private EditText etDescription;
    private Button btnSaveChanges;
    private DatabaseReference rideRef;
    private String currentUserId;
    private Ride currentRide;

    /**
     * When activity is created
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_ride);
        //initalizes all the buttons
        etStartLocation = findViewById(R.id.editStartLocation);
        etEndLocation = findViewById(R.id.editEndLocation);
        etPickupDateTime = findViewById(R.id.editPickupDateTime);
        etDescription = findViewById(R.id.editDescription);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        currentUserId = FirebaseAuth.getInstance()
                .getCurrentUser().getUid();

        String rideId = getIntent().getStringExtra("rideId");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    /**
     * Loads the rides and places them onto screen
     */
    private void loadRide() {
        rideRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                currentRide = snapshot.getValue(Ride.class);
                if (currentRide == null) {
                    Toast.makeText(EditRideActivity.this,
                            "Ride no longer exists.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                //checks to see if the logged in user is the one who created the offer/request
                boolean isOwner =
                        currentUserId.equals(currentRide.getDriverId()) ||
                                currentUserId.equals(currentRide.getRiderId());
                if (!isOwner) {
                    Toast.makeText(EditRideActivity.this,
                            "You cannot edit this ride.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                etStartLocation.setText(currentRide.getStartLocation());
                etEndLocation.setText(currentRide.getEndLocation());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

                etPickupDateTime.setText(
                        dateFormat.format(
                                new Date(currentRide.getPickupDateTime())
                        )
                );

                etDescription.setText(currentRide.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditRideActivity.this,
                        "Error: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveChanges() {

        if (currentRide == null) {
            return;
        }

        String startLocation =
                etStartLocation.getText().toString().trim();

        String endLocation =
                etEndLocation.getText().toString().trim();

        String pickupDateTimeText =
                etPickupDateTime.getText().toString().trim();

        String description =
                etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(startLocation)) {
            etStartLocation.setError("Start location is required");
            return;
        }

        if (TextUtils.isEmpty(endLocation)) {
            etEndLocation.setError("Destination is required");
            return;
        }

        if (TextUtils.isEmpty(pickupDateTimeText)) {
            etPickupDateTime.setError("Pickup date and time are required");
            return;
        }

        long pickupDateTime;

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

            dateFormat.setLenient(false);

            Date date = dateFormat.parse(pickupDateTimeText);
            pickupDateTime = date.getTime();

        } catch (ParseException e) {
            etPickupDateTime.setError("Use format: yyyy-MM-dd HH:mm");
            return;
        }

        // Update only the fields users are allowed to modify
        java.util.Map<String, Object> updates = new java.util.HashMap<>();

        updates.put("startLocation", startLocation);
        updates.put("endLocation", endLocation);
        updates.put("pickupDateTime", pickupDateTime);
        updates.put("description", description);
        //updates the record in the firebase db
        rideRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditRideActivity.this,
                                "Ride updated successfully!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditRideActivity.this,
                                "Failed to update ride.",
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
