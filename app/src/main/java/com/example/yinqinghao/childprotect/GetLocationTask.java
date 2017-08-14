package com.example.yinqinghao.childprotect;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;

/**
 * Created by yinqinghao on 1/5/17.
 *
 * Get the current location
 */
public class GetLocationTask extends AsyncTask<String, Void, Location> {
    //call back method
    public interface LocationResponse {
        void locationProcessFinish(Location location);
    }
    private GetLocationTask.LocationResponse delegate = null;
    private Context context;
    //lcoation manager
    private static LocationManager locationManager;
    //location
    private static Location lo;
    private static double longitude;
    private static double latitude;
    private Activity activity;
    //location listerer
    private static MyLocationListener myLocationListener;

    public GetLocationTask(GetLocationTask.LocationResponse delegate, Context context, Activity activity) {
        this.delegate = delegate;
        this.context = context;
        this.activity = activity;
        myLocationListener = new MyLocationListener();
    }

    @Override
    protected Location doInBackground(String... params) {
        int i = 0;
        if (Looper.myLooper() == null)
            Looper.prepare();
        while (latitude == 0 && i != 20) {
            try {
                //get the current location
                getLocationInfo(context);
            } catch (Exception e) {
                e.printStackTrace();
                //remove the location listener
                locationManager.removeUpdates(myLocationListener);
            }
            i++;
        }
        return lo;
    }

    /**
     * Get the current location
     *
     * @param context
     */
    public static void getLocationInfo(Context context) {
        try {
            longitude = 0.0;
            latitude = 0.0;
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            //Get GPS status
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            //Get the newwork status
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            //if the network is enabled, get the location by network(fingerprinting) first (fast but not accurate)
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
                if (locationManager != null) {
                    lo = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    longitude = lo.getLongitude();
                    latitude = lo.getLatitude();
                }
            }
            //get the location by gps (trilateration)
            if (isGPSEnabled) {
                locationManager.removeUpdates(myLocationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
                if (locationManager != null) {
                    lo = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    longitude = lo.getLongitude();
                    latitude = lo.getLatitude();
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * after processing
     *
     * @param location
     */
    @Override
    protected void onPostExecute(Location location) {
        locationManager.removeUpdates(myLocationListener);
        delegate.locationProcessFinish(location);
    }

    /**
     * MyLocationListener for location change, and other events
     */
    public static class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}