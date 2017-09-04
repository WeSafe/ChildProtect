package com.example.yinqinghao.childprotect;

import android.*;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yinqinghao.childprotect.asyncTask.GetJsonHttpTask;
import com.example.yinqinghao.childprotect.asyncTask.GetJsonTask;
import com.example.yinqinghao.childprotect.asyncTask.GetLocationTask;
import com.example.yinqinghao.childprotect.entity.Group;
import com.example.yinqinghao.childprotect.entity.LocationData;
import com.example.yinqinghao.childprotect.entity.Person;
import com.example.yinqinghao.childprotect.entity.Route;
import com.example.yinqinghao.childprotect.entity.SafeHouse;
import com.example.yinqinghao.childprotect.entity.SharedData;
import com.example.yinqinghao.childprotect.service.LocationService;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SOSActivity extends AppCompatActivity implements OnMapReadyCallback,
        GetLocationTask.LocationResponse, GetJsonTask.AsyncResponse, GetJsonHttpTask.AsyncResponse{

    private final String BASE_GOOGLE_ROUTE_URL = "https://maps.googleapis.com/maps/api/directions/json?";
//    private final String BASE_MY_SERVER_URL = "http://118.138.189.107:8080/ieWebServices/rest/sh/getNearestSafeHouse";
    private final String BASE_MY_SERVER_URL = "http://118.139.77.223:8080/ieWebServices/rest/sh/getNearestSafeHouse";

    private GoogleMap mMap;
    private FirebaseDatabase mDb;
    private FirebaseAuth mAuth;

    private MapView mMapView;
    private TextView mTextDD;
    private FloatingActionButton mNavigationButton;

    private GetLocationTask locationTask;
    private GetJsonTask getJsonTask;
    private GetJsonHttpTask getHttpTask;

    private Polyline mRouteLine;
    private Marker mMyMarker;

    private ValueEventListener mLocationValueListener;
    private ValueEventListener mParentTokenListener;
    private ValueEventListener mChildListener;
    private View.OnClickListener mNavigationOnClickListener;

    private boolean isFirstTime = true;
    private long mTodayTime;
    private String mMyId;
    private String mGid;
    private String mAddress;
    private Person mUser;
    private List<String> mParentTokens;
    private LatLng mDestination;
    private LatLng mLatlng;
    private SafeHouse mSafeHouse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);
        setTitle("The closest safe place");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedData.setIsSos(true);

        MapsInitializer.initialize(this);
        mMapView = (MapView) findViewById(R.id.map_sos);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);
        mDb = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mTodayTime = Person.getDatetime();

        mTextDD = (TextView) findViewById(R.id.txt_dd2);
        mNavigationButton = (FloatingActionButton) findViewById(R.id.fab_nav);
        mNavigationButton.hide(false);

        SharedPreferences sp = getSharedPreferences("ID", Context.MODE_PRIVATE);
        mMyId = sp.getString("uid", null);
        mGid = sp.getString("currentGid", null);
        initListener();
        getMyLocation();
        showSnackBar();
    }

    private String getAddress(LatLng latLng) {
        Geocoder geocoder;
        List<android.location.Address> addresses = null;
        geocoder = new Geocoder(SOSActivity.this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String res = address + ", " + city + ", " + state + ", " + postalCode + ", " + country;
            return res;
//        String knownName = addresses.get(0).getFeatureName();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showSnackBar() {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar =  Snackbar.make(parentLayout, "Call the police.", Snackbar.LENGTH_INDEFINITE)
                .setAction("Yes", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)mNavigationButton.getLayoutParams();
                        params.rightMargin = 8;
                        params.bottomMargin = 8;
                        mNavigationButton.setLayoutParams(params);
                        if (isPermissionGranted()) {
                            callPolice();
                        }
//                        mNavigationButton.setLayoutParams(new ViewGroup.MarginLayoutParams());
                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ));
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(getResources().getColor(R.color.backgroundColor));
        snackbar.show();
    }

    private void callPolice() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "123456"));
        try {
            startActivity(intent);
        } catch (SecurityException ex) {

        }
    }

    private void initListener() {
        mLocationValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    LocationData locationData = null;
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        locationData = ds.getValue(LocationData.class);
                    }
                    if (locationData == null) return;
                    if (mMyMarker != null)
                        mMyMarker.remove();
                    LatLng myLatlng = new LatLng(locationData.getLat(), locationData.getLng());
                    mMyMarker = mMap.addMarker(new MarkerOptions()
                            .position(myLatlng)
                            .title("my location"));
                    mMyMarker.showInfoWindow();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mParentTokenListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mParentTokens = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Group g = ds.getValue(Group.class);
                        Map<String, String> user = g.getUsers();
                        for (String key: user.keySet()) {
                            if (!key.equals(mMyId) && !mParentTokens.contains(user.get(key))) {
                                mParentTokens.add(user.get(key));
                                DatabaseReference refNotification = mDb.getReference("notification")
                                        .child(user.get(key));
                                String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                                        .format(new Date().getTime());

                                String msg = mUser.getFirstName() + " has trouble at " + mAddress +
                                        " at " + date ;
                                refNotification.push().child(msg).setValue(true);
                            }
                        }
                    }
                    Toast.makeText(SOSActivity.this, "Sos message has been sent to all your friends.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mChildListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mUser = dataSnapshot.getValue(Person.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void sendNotification() {
        DatabaseReference refMe = mDb.getReference("userInfo")
                .child(mMyId);
        refMe.addListenerForSingleValueEvent(mChildListener);

        DatabaseReference refParents = mDb.getReference("group");
        refParents.addListenerForSingleValueEvent(mParentTokenListener);
    }

    private void getMyLocation() {
        if (!SharedData.isStartedService()) {
            Intent intent = new Intent(this, LocationService.class);
            startService(intent);
            DatabaseReference refMe = mDb.getReference("userInfo")
                    .child(mAuth.getCurrentUser().getUid());
            com.google.firebase.database.Query queryLocation = refMe
                    .child("locationDatas").child(mTodayTime + "").orderByKey()
                    .limitToLast(1);
            queryLocation.addValueEventListener(mLocationValueListener);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            mMap = googleMap;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.86705, 145.026),12));
            locationTask = new GetLocationTask(this,this,2);
            locationTask.execute();
        }
    }

    private void getDestiantion(LatLng myLocation) {
        getHttpTask = new GetJsonHttpTask(this);
        String parameters = "/" + myLocation.latitude + "/" + myLocation.longitude;
        getHttpTask.execute(BASE_MY_SERVER_URL, parameters);
    }

    @Override
    public void processFinish(String output) {
        getJsonTask = null;
        Gson gson = new Gson();
        if (output.length() != 0) {
            JsonObject jsonObject = gson.fromJson(output, JsonObject.class);
            if (jsonObject.get("status").getAsString().equals("OK")) {
                JsonObject routes = jsonObject.get("routes").getAsJsonArray().get(0).getAsJsonObject();
                JsonArray legs = routes.get("legs").getAsJsonArray();
                long dis = 0;
                long dur = 0;
                for (JsonElement element : legs) {
                    JsonObject leg = element.getAsJsonObject();
                    dis += leg.get("distance").getAsJsonObject().get("value").getAsInt();
                    dur += leg.get("duration").getAsJsonObject().get("value").getAsInt();
                }
                JsonObject polyline = routes.get("overview_polyline").getAsJsonObject();
                String points = polyline.get("points").getAsString();
                List<LatLng> locations = PolyUtil.decode(points);
                mRouteLine = mMap.addPolyline(new PolylineOptions().addAll(locations));
//                showDD((double)dis, dur);
                showDD((double)dis);
            } else {
                Toast.makeText(this, "Can't get the route, please adjust the start/end/waypoint location",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDD(double distance) {
        String dis = "";
        if (distance < 1000) {
            dis = distance + "m";
        } else {
            dis = (distance / 1000) + "km";
        }

        String placeName = mSafeHouse.getPlaceName();
        String address = mSafeHouse.getStreetNum() + " " + mSafeHouse.getRoad() + " " + mSafeHouse.getRoadType()
                + ", " + mSafeHouse.getState() + ", " + mSafeHouse.getPostcode();
        String str = " " + placeName + "( " + dis + ") \n " + address;

        mTextDD.setText(str);
        mTextDD.setVisibility(View.VISIBLE);
    }

    @Override
    public void locationProcessFinish(Location location) {
        locationTask = null;
        if (location != null) {
            if (isFirstTime) {
                mLatlng = new LatLng(location.getLatitude(), location.getLongitude());
                getDestiantion(mLatlng);
            }
            if (mMyMarker != null)
                mMyMarker.remove();
            mMyMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .anchor(0.0f, 1.0f)
                    .title("me"));
            mMyMarker.showInfoWindow();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMyMarker.getPosition(),15));
        }
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(this, LocationService.class);
        if (SharedData.isStartedService()) {
            startService(intent);
        } else {
            stopService(intent);
        }
        SharedData.setIsSos(false);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void httpProcessFinish(String output) {
        getHttpTask = null;
        Gson gson = new Gson();
        if (output.length() != 0) {
            mSafeHouse = gson.fromJson(output, SafeHouse.class);
            double lat = mSafeHouse.getLat();
            double lng = mSafeHouse.getLng();
            mDestination = new LatLng(lat, lng);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(mDestination)
                    .anchor(0.0f, 1.0f)
                    .title("destination")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.sh)));
            marker.showInfoWindow();



            String key = getString(R.string.GOOGLE_DIRECTION_API_KEY);
            final LatLng startLocation = mLatlng;
            final LatLng endLocation = mDestination;
            String origin = startLocation.latitude + "," + startLocation.longitude;
            String destination = endLocation.latitude + "," + endLocation.longitude;
            String parameters = "origin=" + origin + "&destination=" + destination +"&mode=walking&key=" + key;
            getJsonTask = new GetJsonTask(this);
            getJsonTask.execute(BASE_GOOGLE_ROUTE_URL, parameters);
            mAddress = getAddress(startLocation);
            sendNotification();
            isFirstTime = false;

            mNavigationOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?&daddr=%f,%f", endLocation.latitude, endLocation.longitude);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    try
                    {
                        startActivity(intent);
                    }
                    catch(ActivityNotFoundException ex)
                    {
                        try
                        {
                            Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            startActivity(unrestrictedIntent);
                        }
                        catch(ActivityNotFoundException innerEx)
                        {
                            Toast.makeText(SOSActivity.this, "Please install a maps application", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            };
            mNavigationButton.setOnClickListener(mNavigationOnClickListener);
            mNavigationButton.show(true);
        }
    }


    public  boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("SOSActivity", "Permission is granted");
                return true;
            } else {

                Log.v("SOSActivity", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("SOSActivity", "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {

            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
                    callPolice();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
