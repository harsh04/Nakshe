package harshmathur.nic.soi.com.nakshe;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

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

import java.io.Serializable;


/**
 * Created by Harsh Mathur on 14-07-2017.
 */

public class GPSLocation  implements  LocationListener {

        private Context context;
        private LocationManager locationManager;
        private double latitude;
        private double longitude;
        LatLng locationUser;
        private Criteria criteria;
        private String provider;
        boolean allowance = false;


        public GPSLocation(Context context) {
            this.context = context;
            locationManager = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            provider = locationManager.getBestProvider(criteria, true);
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 0, this);

        }



        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * android.location.LocationListener#onLocationChanged(android.location.
         * Location)
         */
        @Override
        public void onLocationChanged(Location location) {
            latitude =  (location.getLongitude());/// * 1E6);
            longitude = (location.getLatitude());// * 1E6);
        }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.location.LocationListener#onProviderDisabled(java.lang.String)
     */
        @Override
        public void onProviderDisabled(String arg0) {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         *
         * @see
         * android.location.LocationListener#onProviderEnabled(java.lang.String)
         */
        @Override
        public void onProviderEnabled(String arg0) {
            // TODO Auto-generated method stub

        }

    public LatLng getUserLocation(boolean b) {
        allowance = b;
        return locationUser;
    }

        /*
         * (non-Javadoc)
         *
         * @see android.location.LocationListener#onStatusChanged(java.lang.String,
         * int, android.os.Bundle)
         */

}
