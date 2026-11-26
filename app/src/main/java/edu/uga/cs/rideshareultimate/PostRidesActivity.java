
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Handles posting new rides to Firebase Realtime Database.
 */
public class PostRidesActivity extends AppCompatActivity {

    private static final int RIDE_COST = 10;

    private EditText etStartLocation, etEndLocation;
    private EditText etPickupDateTime, etAvailableSeats;
    private EditText etDescription;

    private FirebaseAuth mAuth;
    private DatabaseReference ridesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_rides);

        mAuth = FirebaseAuth.getInstance();
        ridesRef = FirebaseDatabase.getInstance().getReference("rides");

        etStartLocation = findViewById(R.id.editTextText);
        etEndLocation = findViewById(R.id.editTextText2);
        etPickupDateTime = findViewById(R.id.editTextText3);
        etAvailableSeats = findViewById(R.id.editTextText4);
        etDescription = findViewById(R.id.editTextText5);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Button btnPostRide = findViewById(R.id.button);

        btnPostRide.setOnClickListener(v -> postRide());
    }


    /**
     * Posts the ride to the Firebase realtime db
     */
    private void postRide() {

        String startLocation = etStartLocation.getText().toString().trim();
        String endLocation = etEndLocation.getText().toString().trim();
        String pickupDateTimeText = etPickupDateTime.getText().toString().trim();
        String availableSeatsText = etAvailableSeats.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        //all fields must be filled out
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

        if (TextUtils.isEmpty(availableSeatsText)) {
            etAvailableSeats.setError("Available seats are required");
            return;
        }

        int availableSeats;

        try {
            availableSeats = Integer.parseInt(availableSeatsText);
        } catch (NumberFormatException e) {
            etAvailableSeats.setError("Please enter a valid number");
            return;
        }

        if (availableSeats <= 0) {
            etAvailableSeats.setError("Must have at least one available seat");
            return;
        }

        // Convert pickup date/time into a timestamp
        long pickupDateTime;

        try {
            SimpleDateFormat dateFormat =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

            dateFormat.setLenient(false);

            Date date = dateFormat.parse(pickupDateTimeText);
            pickupDateTime = date.getTime();

        } catch (ParseException e) {
            etPickupDateTime.setError("Use format: yyyy-MM-dd HH:mm");
            return;
        }

        // Get currently logged-in user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to post a ride.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a unique ride ID
        String rideId = ridesRef.push().getKey();

        if (rideId == null) {
            Toast.makeText(this, "Failed to generate ride ID.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Ride object
        Ride newRide = new Ride(
                rideId,
                "offer",
                currentUser.getUid(),
                null,
                currentUser.getEmail(),
                null,
                startLocation,
                endLocation,
                pickupDateTime,
                System.currentTimeMillis(),
                availableSeats,
                RIDE_COST,
                description
        );

        // Save ride to Firebase Realtime db
        ridesRef.child(rideId).setValue(newRide)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(PostRidesActivity.this,
                                "Ride posted successfully!",
                                Toast.LENGTH_SHORT).show();

                        finish();

                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Failed to post ride.";

                        Toast.makeText(PostRidesActivity.this,
                                "Error: " + msg,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * The code that runs when the back button is pressed
     * @param item The menu item that was selected.
     */
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
