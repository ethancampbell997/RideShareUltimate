package edu.uga.cs.rideshareultimate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Main dashboard displaying the signed-in user's ride-points balance and logout control.
 */
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView tvWelcomeUser, tvRidePoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvRidePoints = findViewById(R.id.tvRidePoints);
        Button btnLogOut = findViewById(R.id.btnLogOut);
        Button btnPostRide = findViewById(R.id.button2);
        Button btnViewRides = findViewById(R.id.button3);

        btnLogOut.setOnClickListener(v -> {
            mAuth.signOut();
            goToLogin();
        });
        //intent for posting rides page
        btnPostRide.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PostRidesActivity.class);
            startActivity(intent);
        });
        btnViewRides.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ViewRidesActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            goToLogin();
        } else {
            tvWelcomeUser.setText("Signed in as: " + currentUser.getEmail());
            loadRidePoints(currentUser.getUid());
        }
    }

    private void loadRidePoints(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    tvRidePoints.setText("Ride Points: " + user.getRidePoints());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load points.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}