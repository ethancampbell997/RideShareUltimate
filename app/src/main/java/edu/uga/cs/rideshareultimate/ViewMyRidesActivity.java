package edu.uga.cs.rideshareultimate;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * view all rides that have been posted
 */
public class ViewMyRidesActivity extends AppCompatActivity {

    private ListView ridesListView;
    private ArrayList<String> ridesList;
    private ArrayAdapter<String> ridesAdapter;

    private DatabaseReference ridesRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_rides);

        ridesListView = findViewById(R.id.ridesListView);

        ridesList = new ArrayList<>();
        ridesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                ridesList
        );

        currentUserId = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        ridesListView.setAdapter(ridesAdapter);

        ridesRef = FirebaseDatabase.getInstance().getReference("rides");

        loadRides();
    }

    private void loadRides() {
        //gets the rides ordered by the soonest pickupDateTime
        ridesRef.orderByChild("pickupDateTime")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        ridesList.clear();
                        for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                            Ride ride = rideSnapshot.getValue(Ride.class);
                            //only displays if it's a ride offer, so no requests
                            if (ride != null &&
                                    (currentUserId.equals(ride.getDriverId()) ||
                                            currentUserId.equals(ride.getRiderId()))) {

                                String pickupDate = new SimpleDateFormat(
                                        "MMM d, yyyy 'at' h:mm a",
                                        Locale.getDefault()
                                ).format(new Date(ride.getPickupDateTime()));

                                String rideInfo =
                                        "From: " + ride.getStartLocation() +
                                                "\nTo: " + ride.getEndLocation() +
                                                "\nDriver: " + ride.getDriverEmail() +
                                                "\nPickup: " + pickupDate +
                                                "\nAvailable Seats: " + ride.getAvailableSeats() +
                                                "\nCost: " + ride.getCost() + " ride points" +
                                                "\nDescription: " + ride.getDescription();

                                ridesList.add(rideInfo);
                            }
                        }

                        ridesAdapter.notifyDataSetChanged();

                        if (ridesList.isEmpty()) {
                            Toast.makeText(
                                    ViewMyRidesActivity.this,
                                    "No one has posted a ride yet!.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {

                        Toast.makeText(
                                ViewMyRidesActivity.this,
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
