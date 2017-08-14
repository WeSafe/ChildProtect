package com.example.yinqinghao.childprotect.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.yinqinghao.childprotect.GetLocationTask;
import com.example.yinqinghao.childprotect.R;
import com.example.yinqinghao.childprotect.entity.Child;
import com.example.yinqinghao.childprotect.entity.LocationData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class MapsFragment extends android.app.Fragment implements OnMapReadyCallback,
        GetLocationTask.LocationResponse {

    private GoogleMap mMap;

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 123;
    private static final String TAG = "MapsFragment";
    private GetLocationTask locationTask;
    private Location mLocation;
    private MapView mMapView;
    private View mView;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDb;
    private ValueEventListener mFidValueListener;
    private ValueEventListener mChildrenValueListener;
    private ValueEventListener mLocationDataValueListner;

    private String mFamilyId;
    private List<Child> mChildren;
//    private Map<String, LocationData> mChildrenLocation;

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mView = getView();
        if (mView != null) {
            MapsInitializer.initialize(this.getActivity());
            mMapView = (MapView) mView.findViewById(R.id.map);
            mMapView.onCreate(savedInstanceState);
            mMapView.onResume();
            mMapView.getMapAsync(this);
            mFamilyId = "";
            mChildren = new ArrayList<>();
            initListener();
            getChildLocation();
        }
    }

    private void initListener() {
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance();
//        mLocationDataValueListner = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    LocationData locationData = dataSnapshot.getValue(LocationData.class);
//                    mChildrenLocation.put(dataSnapshot.getKey(),locationData);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.w(TAG, "loadChildrenLocation:onCancelled", databaseError.toException());
//            }
//        };

        mChildrenValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Child child = ds.getValue(Child.class);
                        mChildren.add(child);
                        addChildMarker(child);
//                        String childId = child.getUid();
//                        DatabaseReference refLocation = mDb.getReference("location").child(childId)
//                                .orderByKey().limitToLast(1).getRef();
//                        refLocation.addListenerForSingleValueEvent(mLocationDataValueListner);
                        
                        //TODO change the listener to valuechangedlinstener
                    }

                    if (mChildren.size() > 0) {
                        LocationData locationData = mChildren.get(0).getMostRecentLocation();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(locationData.getLat(), locationData.getLng()), 11));
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadChildren:onCancelled", databaseError.toException());
            }
        };

        mFidValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mFamilyId = dataSnapshot.getValue().toString();
                    DatabaseReference refChildren = mDb.getReference("family").child(mFamilyId)
                            .child("child");
                    refChildren.addListenerForSingleValueEvent(mChildrenValueListener);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadFamilyId:onCancelled", databaseError.toException());
            }
        };
    }

    private void getChildLocation() {
        String parentId = mAuth.getCurrentUser().getUid();
        DatabaseReference refFID = mDb.getReference("user").child(parentId).child("familyId");
        refFID.addListenerForSingleValueEvent(mFidValueListener);
    }

    private void addChildMarker(Child c) {
        LocationData locationData = c.getMostRecentLocation();
        Marker child = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(locationData.getLat(),locationData.getLng())).title(c.getFirstName())
                .anchor(0.0f, 1.0f));
        child.showInfoWindow();
    }

    /**
     * Check the location service permission and request it if there is no permission
     */
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23 ) {
            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            } else
                locationTask.execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationTask.execute();
                }
                else
                    Toast.makeText(getActivity(), "Can't get the location, please grant the permission",
                            Toast.LENGTH_SHORT);
                return;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationTask = new GetLocationTask(this, getActivity());
        checkPermission();
    }

    @Override
    public void locationProcessFinish(Location location) {
        mLocation = location;
        Marker me = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(mLocation.getLatitude(),mLocation.getLongitude())).title("My LocationData")
                .anchor(0.0f, 1.0f));
        Location camera = mLocation;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(camera.getLatitude(), camera.getLongitude()), 11));
        me.showInfoWindow();
    }
}
