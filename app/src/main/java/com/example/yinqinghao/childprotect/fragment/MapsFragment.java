package com.example.yinqinghao.childprotect.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.aigestudio.wheelpicker.WheelPicker;
import com.example.yinqinghao.childprotect.asyncTask.GetLocationTask;
import com.example.yinqinghao.childprotect.R;
import com.example.yinqinghao.childprotect.clusterMarker.MarkerRender;
import com.example.yinqinghao.childprotect.entity.Child;
import com.example.yinqinghao.childprotect.entity.LocationData;
import com.example.yinqinghao.childprotect.clusterMarker.MarkerItem;
import com.example.yinqinghao.childprotect.entity.Zone;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
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
import com.google.maps.android.clustering.ClusterManager;
import com.wooplr.spotlight.utils.SpotlightSequence;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MapsFragment extends android.app.Fragment implements OnMapReadyCallback,
        GetLocationTask.LocationResponse {
    private GoogleMap mMap;

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 123;
    private static final int ZONE_ACTIVITY = 876;
    private static final String TAG = "MapsFragment";
    private GetLocationTask locationTask;
//    private GetGPSTask locationTask;
    private Location mLocation;
    private MapView mMapView;
    private View mView;

    private FloatingActionMenu mMenuHistory;
    private FloatingActionButton mButtenRealTime;
    private View mHisTutor;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDb;
    private ValueEventListener mFidValueListener;
    private ValueEventListener mChildrenValueListener;
    private ValueEventListener mLocationDataValueListner;
    private ValueEventListener mZoneDataListener;

    private Handler mUiHandler = new Handler();

    private String mFamilyId;
//    private Marker mMyMarker;
    private MarkerItem mMyMarkerItem;
    private Map<String, Child> mChildren;
//    private Map<String, Marker> mChildMarkers;
    private Map<String, MarkerItem> mChildMarkerItems;
    private List<Circle> mZones;
    private List<Marker> mCenters;
    private List<Polyline> mLines;
    private ClusterManager<MarkerItem> mClusterManager;
    private long mTodayTime;

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

            mMenuHistory = (FloatingActionMenu) mView.findViewById(R.id.menu_history);
            mButtenRealTime = (FloatingActionButton) mView.findViewById(R.id.fab_realtime);
            mHisTutor = mView.findViewById(R.id.historyTutor);
            mMenuHistory.hideMenuButton(false);
            mButtenRealTime.hide(false);

            mTodayTime = Child.getDatetime();
//            mChildMarkers = new HashMap<>();
            mChildMarkerItems = new HashMap<>();
            mZones = new ArrayList<>();
            mCenters = new ArrayList<>();
            mFamilyId = "";

            initListener();
        }
    }


    private void showHistoryPicker(Child child) {
        final Dialog d = new Dialog(getActivity());
        d.setTitle("DatePicker");
        d.setContentView(R.layout.picker);
        Button btnOK = (Button) d.findViewById(R.id.btn_ok);
        Button btnCancel = (Button) d.findViewById(R.id.btn_cancel);
        final WheelPicker wheelPicker = (WheelPicker) d.findViewById(R.id.main_wheel_center);
        final Map<String,Map<String, LocationData>> locationMap = child.getLocationDatas();
        final List<String> data = new ArrayList<>();
        String sDate = "";
        final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        List<String> dates = new ArrayList<>(locationMap.keySet());
        Collections.sort(dates, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                Long l1 = Long.valueOf(o1);
                Long l2 = Long.valueOf(o2);
                return l2.compareTo(l1);
            }
        });
        for (String sTime: dates) {
            Date date = new Date(Long.parseLong(sTime));
            sDate = format.format(date);
            data.add(sDate);
        }
        wheelPicker.setData(data);
        wheelPicker.setCyclic(false);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = wheelPicker.getCurrentItemPosition();
                String strDate = data.get(i);
                String key = null;
                try {
                    key = format.parse(strDate).getTime() + "";
                } catch (Exception ex) {
                    Log.e(TAG,ex.getMessage());
                }
                drawRoute(locationMap.get(key));
                d.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    private void drawRoute(Map<String, LocationData> locationDatas) {
        removeLocationListener();
        TreeMap<String, LocationData> dataTreeMap = Child.sortLocationsAESC(locationDatas);
        Handler routeHandler = new Handler();
        final PolylineOptions routeOption = new PolylineOptions();
        Object [] locations = dataTreeMap.values().toArray();
        long delay = 800;
        double lat = 0;
        double lng = 0;

        if (mClusterManager != null) {
            for (MarkerItem markerItem: mChildMarkerItems.values())
                mClusterManager.removeItem(markerItem);

            if (mMyMarkerItem != null)
                mClusterManager.removeItem(mMyMarkerItem);
        }

        if (mLines != null) {
            for (Polyline p : mLines) {
                p.remove();
            }
        }

        mLines = new ArrayList<>();

        for (final Object o: locations) {
            final LocationData l = (LocationData) o;
            if (lat != l.getLat() || lng != l.getLng()) {

                routeHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LatLng camera = new LatLng(l.getLat(), l.getLng());
                        routeOption.add(camera);
                        Polyline polyline = mMap.addPolyline(routeOption);
                        mLines.add(polyline);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(camera, 13));
                    }
                }, delay);
                delay += 100;
            }
            lat = l.getLat();
            lng = l.getLng();
        }
    }

    private void setmFab() {
        mMenuHistory.removeAllMenuButtons();
        for (final Child child: mChildren.values()) {
            final FloatingActionButton childHistoryFab = new FloatingActionButton(getActivity());
            childHistoryFab.setButtonSize(FloatingActionButton.SIZE_MINI);
            childHistoryFab.setLabelText(child.getFirstName());
            childHistoryFab.setImageResource(R.drawable.ic_child_care_black_24dp);
            mMenuHistory.addMenuButton(childHistoryFab);
            childHistoryFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHistoryPicker(child);
                    mMenuHistory.close(true);
                }
            });
        }
        mMenuHistory.setClosedOnTouchOutside(true);

        mButtenRealTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                mClusterManager.clearItems();
                markMyLocation();
                getChildLocation();
            }
        });
        showFab();
    }

    private void showFab() {
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMenuHistory.showMenuButton(true);
            }
        }, 400);

        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mButtenRealTime.show(true);
                showTutorial(mButtenRealTime, mHisTutor);
            }
        }, 550);
    }

    private void initListener() {
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance();
        mLocationDataValueListner = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    LocationData locationData = null;
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        locationData = ds.getValue(LocationData.class);
                    }
                    if (locationData == null) return;
                    String childId = locationData.getUid();
                    Child child = mChildren.get(childId);
                    Map<String,Map<String, LocationData>> childLocations = child.getLocationDatas();
                    if (childLocations.containsKey(mTodayTime+"")) {
                        childLocations.get(mTodayTime+"").put(locationData.getDatetime().getTime()+"",locationData);
                    } else {
                        Map<String, LocationData> locationDatas = new HashMap<>();
                        locationDatas.put(locationData.getDatetime().getTime()+"",locationData);
                        childLocations.put(mTodayTime + "", locationDatas);
                    }

//                    if (mChildMarkers.containsKey(childId)) {
//                        Marker childMarker = mChildMarkers.get(childId);
//                        childMarker.remove();
//                        Marker newMarker = mMap.addMarker(new MarkerOptions()
//                                .position(new LatLng(locationData.getLat(),locationData.getLng()))
//                                .title(child.getFirstName())
//                                .snippet(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(locationData.getDatetime()))
//                                .anchor(0.0f, 1.0f));
//                        mChildMarkers.put(childId,newMarker);
//                        newMarker.showInfoWindow();
//                    }

                    if (mChildMarkerItems.containsKey(childId)) {
                        MarkerItem childMarker = mChildMarkerItems.get(childId);
                        if (mClusterManager != null) {
                            mClusterManager.removeItem(childMarker);
                        }

                        MarkerItem newMarkerItem = new MarkerItem(new LatLng(locationData.getLat(),locationData.getLng()),
                                child.getFirstName(), new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(locationData.getDatetime()));
                        mClusterManager.addItem(newMarkerItem);
                        mChildMarkerItems.put(childId, newMarkerItem);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadChildrenLocation:onCancelled", databaseError.toException());
            }
        };

        mChildrenValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DatabaseReference refChildren = mDb.getReference("family").child(mFamilyId)
                            .child("child");
                    String childId = "";
                    mChildren = new HashMap<>();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Child child = ds.getValue(Child.class);
                        childId = child.getUid();
                        mChildren.put(childId, child);
                        addChildMarker(child);
                        com.google.firebase.database.Query queryLocation = refChildren.child(childId)
                                .child("locationDatas").child(mTodayTime+"").orderByKey()
                                .limitToLast(1);
                        queryLocation.addValueEventListener(mLocationDataValueListner);
                    }
                    setmFab();

                    if (mChildren.size() > 0) {
                        if (mChildren.get(childId).getMostRecentLocation() == null) return;
                        LocationData locationData = mChildren.get(childId).getMostRecentLocation();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(locationData.getLat(), locationData.getLng()), 13));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadChildren:onCancelled", databaseError.toException());
            }
        };

        mZoneDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Zone zone = ds.getValue(Zone.class);
                        drawCircle(zone);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mFidValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mFamilyId = dataSnapshot.getValue().toString();
//                    saveFid();
                    DatabaseReference refChildren = mDb.getReference("family").child(mFamilyId)
                            .child("child");
                    refChildren.addListenerForSingleValueEvent(mChildrenValueListener);
                    getZones();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadFamilyId:onCancelled", databaseError.toException());
            }
        };
    }

    private void drawCircle(Zone zone) {
        long radius = zone.getRadius();
        LatLng latLng = new LatLng(zone.getLat(),zone.getLng());
        boolean isSafe = zone.getStatus().equals("safe");
        int strokeColor = ContextCompat.getColor(getActivity() ,
                isSafe ? R.color.safeStrokeColor : R.color.dangerStrokeColor);
        int fillColor = ContextCompat.getColor(getActivity() ,
                isSafe ? R.color.safeFillColor : R.color.dangerFillColor);
        String des = zone.getDes();

        Marker center = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .anchor(0.0f, 1.0f)
                .title(des));
        center.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(radius)
                .strokeColor(strokeColor)
                .fillColor(fillColor)
                .strokeWidth(3)
                .clickable(true);
        Circle circle = mMap.addCircle(circleOptions);
        mCenters.add(center);
        mZones.add(circle);
    }

    private void getChildLocation() {
        String parentId = mAuth.getCurrentUser().getUid();
        DatabaseReference refFID = mDb.getReference("user").child(parentId).child("familyId");
        refFID.addListenerForSingleValueEvent(mFidValueListener);
    }

    private void getZones() {
        DatabaseReference refZone = mDb.getReference("zone")
                .child(mFamilyId);
        refZone.addListenerForSingleValueEvent(mZoneDataListener);
    }

    private void removeLocationListener() {
        DatabaseReference refChildren = mDb.getReference("family").child(mFamilyId)
                .child("child");
        for (Child child: mChildren.values()) {
            com.google.firebase.database.Query queryLocation = refChildren.child(child.getUid())
                    .child("locationDatas").child(mTodayTime+"").orderByKey()
                    .limitToLast(1);
            queryLocation.removeEventListener(mLocationDataValueListner);
        }
    }

    private void addChildMarker(Child c) {
        LocationData locationData = c.getMostRecentLocation();
        if (locationData == null)   return;
//        Marker child = mMap.addMarker(new MarkerOptions()
//                .position(new LatLng(locationData.getLat(),locationData.getLng()))
//                .title(c.getFirstName())
//                .snippet(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(locationData.getDatetime()))
//                .anchor(0.0f, 1.0f));
//        mChildMarkers.put(c.getUid(),child);
//        child.showInfoWindow();
        MarkerItem newMarkerItem = new MarkerItem(new LatLng(locationData.getLat(),locationData.getLng()),
                c.getFirstName(), new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(locationData.getDatetime()));
        mClusterManager.addItem(newMarkerItem);
        mClusterManager.cluster();
        mChildMarkerItems.put(c.getUid(), newMarkerItem);

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
            }
//                locationTask.execute();
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
                    try {
                        if (locationTask == null) {
                            locationTask = new GetLocationTask(this, getActivity(), 4);
                        }
                        locationTask.execute();
                    } catch (IllegalStateException ex) {

                    }
                } else
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
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.8668, 145.016), 13));
        setUpClusterer();
        getChildLocation();
        locationTask = new GetLocationTask(this, getActivity(), 4);
        checkPermission();
        try {
            locationTask.execute();
        } catch (IllegalStateException ex) {

        }
    }

    private void setUpClusterer() {
        mClusterManager = new ClusterManager<>(getActivity(), mMap);
        MarkerRender render = new MarkerRender(getActivity(),mMap,mClusterManager);
        render.setMinClusterSize(1);
        mClusterManager.setRenderer(render);
        mMap.setOnCameraIdleListener(mClusterManager);
    }

    private void markMyLocation() {
        locationTask = new GetLocationTask(this, getActivity(), 4);
        locationTask.execute();
    }

    @Override
    public void locationProcessFinish(Location location) {
        locationTask = null;
        mLocation = location;
        if (mLocation == null) {
            Log.d(TAG, "can't get the location");
            return;
        }
        Location camera = mLocation;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(camera.getLatitude(), camera.getLongitude()), 13));

        LatLng myLatLng = new LatLng(mLocation.getLatitude(),mLocation.getLongitude());
//        mMyMarker = mMap.addMarker(new MarkerOptions()
//                .position(myLatLng)
//                .title("My Location")
//                .icon(BitmapDescriptorFactory.defaultMarker(
//                        BitmapDescriptorFactory.HUE_AZURE))
//                .anchor(0.0f, 1.0f));
        mMyMarkerItem = new MarkerItem(myLatLng, "My Location", "");
        mMyMarkerItem.setmIcon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_AZURE));
        mClusterManager.addItem(mMyMarkerItem);
        mClusterManager.cluster();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ZONE_ACTIVITY) {
            if (resultCode == getActivity().RESULT_OK) {
                for (int i = 0; i < mCenters.size(); i++) {
                    mZones.get(i).remove();
                    mCenters.get(i).remove();
                }
                List<Zone> zones = data.getParcelableArrayListExtra("zones");
                for (Zone zone: zones) {
                    drawCircle(zone);
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showTutorial(View view, View view2) {
        SpotlightSequence.getInstance(getActivity(),null)
                .addSpotlight(view,
                        "Real-Time Location", "Click to know the location of your kids ", "+++wq+sasssss=q++")
                .addSpotlight(view2,
                        "History Route", "Click here to see the history route of your kids", "-s-sasqwsssq=---")
                .startSequence();
    }
}
