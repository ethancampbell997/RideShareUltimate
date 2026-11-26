
package edu.uga.cs.rideshareultimate;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;
import android.content.Intent;

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
 * view all rides that have been posted
 */
public class ViewRidesActivity extends AppCompatActivity {

    private ListView ridesListView;
    private ArrayList<String> ridesList;
    private ArrayAdapter<String> ridesAdapter;
    private ArrayList<String> rideIds;
    private DatabaseReference ridesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_rides);

        ridesListView = findViewById(R.id.ridesListView);
        ridesList = new ArrayList<>();
        rideIds = new ArrayList<>();

        ridesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                ridesList
        );

        ridesListView.setAdapter(ridesAdapter);

        ridesListView.setOnItemClickListener((parent, view, position, id) -> {

            String selectedRideId = rideIds.get(position);

            Intent intent = new Intent(
                    ViewRidesActivity.this,
                    AcceptRideActivity.class
            );

            intent.putExtra("rideId", selectedRideId);

            startActivity(intent);
        });

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
                rideIds.clear();
                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Ride ride = rideSnapshot.getValue(Ride.class);
                    //only displays if it's a ride offer, so no requests
                    if (ride != null &&
                            "offer".equals(ride.getRideType()) &&
                            ride.getRiderId() == null) {

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
                        rideIds.add(ride.getRideId());
                    }
                }

                ridesAdapter.notifyDataSetChanged();

                if (ridesList.isEmpty()) {
                    Toast.makeText(
                            ViewRidesActivity.this,
                            "No one has posted a ride yet!.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {

                Toast.makeText(
                        ViewRidesActivity.this,
                        "Error: " + error.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
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
