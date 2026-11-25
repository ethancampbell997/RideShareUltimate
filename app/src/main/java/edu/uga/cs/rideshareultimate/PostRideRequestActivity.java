
package edu.uga.cs.rideshareultimate;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
 * Handles posting new ride requests to Firebase Realtime Database.
 */
public class PostRideRequestActivity extends AppCompatActivity {

    private static final int RIDE_COST = 10;

    private EditText etStartLocation, etEndLocation;
    private EditText etPickupDateTime;
    private EditText etDescription;

    private FirebaseAuth mAuth;
    private DatabaseReference ridesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_ride_request);

        mAuth = FirebaseAuth.getInstance();
        ridesRef = FirebaseDatabase.getInstance().getReference("rides");

        etStartLocation = findViewById(R.id.editTextText);
        etEndLocation = findViewById(R.id.editTextText2);
        etPickupDateTime = findViewById(R.id.editTextText3);
        etDescription = findViewById(R.id.editTextText5);

        Button btnPostRide = findViewById(R.id.button);

        btnPostRide.setOnClickListener(v -> postRide());
    }

    /**
     * Posts the ride request to the Firebase Realtime Database.
     */
    private void postRide() {

        String startLocation = etStartLocation.getText().toString().trim();
        String endLocation = etEndLocation.getText().toString().trim();
        String pickupDateTimeText = etPickupDateTime.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // All required fields must be filled out
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
            Toast.makeText(this, "You must be logged in to post a ride request.",
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
                "request",
                null,
                currentUser.getUid(),
                null,
                currentUser.getEmail(),
                startLocation,
                endLocation,
                pickupDateTime,
                System.currentTimeMillis(),
                1,
                RIDE_COST,
                description
        );

        // Save ride request to Firebase Realtime DB
        ridesRef.child(rideId).setValue(newRide)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(PostRideRequestActivity.this,
                                "Ride request posted successfully!",
                                Toast.LENGTH_SHORT).show();

                        finish();

                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Failed to post ride request.";

                        Toast.makeText(PostRideRequestActivity.this,
                                "Error: " + msg,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
