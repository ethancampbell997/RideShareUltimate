
package edu.uga.cs.rideshareultimate;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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
 * View all available ride requests.
 */
public class ViewRideRequestsActivity extends AppCompatActivity {

    private ListView ridesListView;
    private ArrayList<String> ridesList;
    private ArrayAdapter<String> ridesAdapter;
    private ArrayList<String> rideIds;
    private DatabaseReference ridesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_ride_requests);

        ridesListView = findViewById(R.id.ridesListView);
        ridesList = new ArrayList<>();
        rideIds = new ArrayList<>();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ridesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                ridesList
        );

        ridesListView.setAdapter(ridesAdapter);

        ridesListView.setOnItemClickListener((parent, view, position, id) -> {

            String selectedRideId = rideIds.get(position);

            Intent intent = new Intent(
                    ViewRideRequestsActivity.this,
                    AcceptRideRequestActivity.class
            );

            intent.putExtra("rideId", selectedRideId);

            startActivity(intent);
        });

        ridesRef = FirebaseDatabase.getInstance().getReference("rides");

        loadRides();
    }

    private void loadRides() {

        // Gets rides ordered by the soonest pickupDateTime.
        ridesRef.orderByChild("pickupDateTime")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        ridesList.clear();
                        rideIds.clear();

                        for (DataSnapshot rideSnapshot : snapshot.getChildren()) {

                            Ride ride = rideSnapshot.getValue(Ride.class);

                            // Only displays unaccepted ride requests.
                            if (ride != null &&
                                    "request".equals(ride.getRideType()) &&
                                    (ride.getDriverId() == null ||
                                            ride.getDriverId().isEmpty())) {

                                String pickupDate = new SimpleDateFormat(
                                        "MMM d, yyyy 'at' h:mm a",
                                        Locale.getDefault()
                                ).format(new Date(ride.getPickupDateTime()));

                                String rideInfo =
                                        "From: " + ride.getStartLocation() +
                                                "\nTo: " + ride.getEndLocation() +
                                                "\nRider: " + ride.getRiderEmail() +
                                                "\nPickup: " + pickupDate +
                                                "\nCost: " + ride.getCost() + " ride points" +
                                                "\nDescription: " + ride.getDescription();

                                ridesList.add(rideInfo);
                                rideIds.add(ride.getRideId());
                            }
                        }

                        ridesAdapter.notifyDataSetChanged();

                        if (ridesList.isEmpty()) {
                            Toast.makeText(
                                    ViewRideRequestsActivity.this,
                                    "No ride requests are currently available.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                        Toast.makeText(
                                ViewRideRequestsActivity.this,
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
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
