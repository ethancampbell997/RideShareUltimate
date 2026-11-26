
package edu.uga.cs.rideshareultimate;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ViewMyRidesActivity extends AppCompatActivity {

    private ListView acceptedRidesListView;
    private ListView unacceptedRidesListView;

    private ArrayList<String> acceptedRidesList;
    private ArrayList<String> unacceptedRidesList;

    private ArrayList<String> acceptedRideIds;
    private ArrayList<String> unacceptedRideIds;

    private ArrayAdapter<String> acceptedRidesAdapter;
    private ArrayAdapter<String> unacceptedRidesAdapter;

    private DatabaseReference ridesRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_my_rides);

        acceptedRidesListView =
                findViewById(R.id.acceptedRidesListView);

        unacceptedRidesListView =
                findViewById(R.id.unacceptedRidesListView);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FirebaseUser currentUser =
                FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this,
                    "You must be logged in.",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = currentUser.getUid();

        acceptedRidesList = new ArrayList<>();
        unacceptedRidesList = new ArrayList<>();

        acceptedRideIds = new ArrayList<>();
        unacceptedRideIds = new ArrayList<>();

        acceptedRidesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                acceptedRidesList
        );

        unacceptedRidesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                unacceptedRidesList
        );

        acceptedRidesListView.setAdapter(acceptedRidesAdapter);
        unacceptedRidesListView.setAdapter(unacceptedRidesAdapter);

        acceptedRidesListView.setOnItemClickListener(
                (parent, view, position, id) ->
                        openRide(acceptedRideIds.get(position))
        );

        unacceptedRidesListView.setOnItemClickListener(
                (parent, view, position, id) ->
                        openRide(unacceptedRideIds.get(position))
        );

        ridesRef = FirebaseDatabase.getInstance()
                .getReference("rides");

        loadAcceptedRides();
        loadUnacceptedRides();
    }

    /**
     * Opens the selected ride.
     */
    private void openRide(String rideId) {

        Intent intent = new Intent(
                ViewMyRidesActivity.this,
                ManageRideActivity.class
        );

        intent.putExtra("rideId", rideId);

        startActivity(intent);
    }

    /**
     * Loads rides where both the driver and rider have accepted.
     */
    private void loadAcceptedRides() {

        ridesRef.orderByChild("pickupDateTime")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        acceptedRidesList.clear();
                        acceptedRideIds.clear();

                        for (DataSnapshot rideSnapshot :
                                snapshot.getChildren()) {

                            Ride ride =
                                    rideSnapshot.getValue(Ride.class);

                            if (ride == null) {
                                continue;
                            }

                            boolean isCurrentUserInvolved =
                                    currentUserId.equals(ride.getDriverId()) ||
                                            currentUserId.equals(ride.getRiderId());

                            boolean hasDriver =
                                    ride.getDriverId() != null &&
                                            !ride.getDriverId().isEmpty();

                            boolean hasRider =
                                    ride.getRiderId() != null &&
                                            !ride.getRiderId().isEmpty();

                            if (isCurrentUserInvolved &&
                                    hasDriver && hasRider) {

                                acceptedRidesList.add(
                                        formatRideInfo(ride)
                                );

                                acceptedRideIds.add(ride.getRideId());
                            }
                        }

                        acceptedRidesAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                        Toast.makeText(
                                ViewMyRidesActivity.this,
                                "Error loading accepted rides: " +
                                        error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    /**
     * Loads rides where the current user is involved,
     * but either the driver or rider has not accepted yet.
     */
    private void loadUnacceptedRides() {

        ridesRef.orderByChild("pickupDateTime")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        unacceptedRidesList.clear();
                        unacceptedRideIds.clear();

                        for (DataSnapshot rideSnapshot :
                                snapshot.getChildren()) {

                            Ride ride =
                                    rideSnapshot.getValue(Ride.class);

                            if (ride == null) {
                                continue;
                            }

                            boolean isCurrentUserInvolved =
                                    currentUserId.equals(ride.getDriverId()) ||
                                            currentUserId.equals(ride.getRiderId());

                            boolean hasDriver =
                                    ride.getDriverId() != null &&
                                            !ride.getDriverId().isEmpty();

                            boolean hasRider =
                                    ride.getRiderId() != null &&
                                            !ride.getRiderId().isEmpty();

                            if (isCurrentUserInvolved &&
                                    (!hasDriver || !hasRider)) {

                                unacceptedRidesList.add(
                                        formatRideInfo(ride)
                                );

                                unacceptedRideIds.add(ride.getRideId());
                            }
                        }

                        unacceptedRidesAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                        Toast.makeText(
                                ViewMyRidesActivity.this,
                                "Error loading unaccepted rides: " +
                                        error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    /**
     * Formats ride information for display in either list.
     */
    private String formatRideInfo(Ride ride) {

        String pickupDate = new SimpleDateFormat(
                "MMM d, yyyy 'at' h:mm a",
                Locale.getDefault()
        ).format(new Date(ride.getPickupDateTime()));

        return "From: " + ride.getStartLocation() +
                "\nTo: " + ride.getEndLocation() +
                "\nDriver: " + ride.getDriverEmail() +
                "\nRider: " + ride.getRiderEmail() +
                "\nPickup: " + pickupDate +
                "\nAvailable Seats: " + ride.getAvailableSeats() +
                "\nCost: " + ride.getCost() + " ride points" +
                "\nDescription: " + ride.getDescription();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
