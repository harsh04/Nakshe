package harshmathur.nic.soi.com.nakshe;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    FirebaseDatabase database;
    String block_list;
    GoogleApiClient googleApiClient;
    Geocoder geocoder;
    private Criteria criteria;
    private String provider;
    List<Address> addresses;
    ImageView showIcon;
    TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }else {
            todoNextTask();
        }

    }

    private void todoNextTask() {
        showIcon = (ImageView) findViewById(R.id.icon);
        message = (TextView) findViewById(R.id.message);

        GPSLocation gps = new GPSLocation(this);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        provider = locationManager.getBestProvider(criteria, true);
        database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("block_list");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        final Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(location == null) {
                    showIcon.setImageResource(R.drawable.warning);
                    message.setText("Restating Application");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = getBaseContext().getPackageManager()
                                    .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        }
                    }, 700);
                }
            }
        }, 7000);
        if(location!=null){
            showIcon.setImageResource(R.drawable.check);
            message.setText("Authorizing");
            // Read from the database in real time
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    block_list = dataSnapshot.getValue(String.class);
                    try {
                        checkUserCurrentLocationAllowance(location,block_list);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    message.setText("Unable to authenticate!");
                    showIcon.setImageResource(R.drawable.error);
                    Log.w("block_list", "Failed to read value.", error.toException());
                }
            });
        }

    }


    private void checkUserCurrentLocationAllowance(Location location, String block_list) throws IOException {
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        boolean al = true;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
        if (!country.equalsIgnoreCase("India")) {
            //to prevent usage of this application outside India
            message.setText("Sorry! Use of this application Outside India is not Authorized");
            showIcon.setImageResource(R.drawable.warning);
            al =false;
        }
        if (block_list.toLowerCase().contains(state.toLowerCase())) {
            //to prevent usage of this application outside India
            message.setText("Sorry! Use of this application at your location is not Authorized");
            showIcon.setImageResource(R.drawable.warning);
            al =false;
        }
        if(al) {
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            i.putExtra("user_latitude",location.getLatitude());
            i.putExtra("user_longitude",location.getLongitude());
            i.putExtra("user_state",state);
            startActivity(i);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    todoNextTask();
                } else {
                    finish();
                }
                return;
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
