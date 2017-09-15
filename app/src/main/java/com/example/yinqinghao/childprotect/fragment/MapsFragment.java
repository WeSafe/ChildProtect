package com.example.yinqinghao.childprotect.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aigestudio.wheelpicker.WheelPicker;
import com.example.yinqinghao.childprotect.Manifest;
import com.example.yinqinghao.childprotect.asyncTask.GetLocationTask;
import com.example.yinqinghao.childprotect.R;
import com.example.yinqinghao.childprotect.clusterMarker.MarkerRender;
import com.example.yinqinghao.childprotect.clusterMarker.MyInfoWindowAdapter;
import com.example.yinqinghao.childprotect.entity.Group;
import com.example.yinqinghao.childprotect.entity.Person;
import com.example.yinqinghao.childprotect.entity.LocationData;
import com.example.yinqinghao.childprotect.clusterMarker.MarkerItem;
import com.example.yinqinghao.childprotect.entity.SharedData;
import com.example.yinqinghao.childprotect.entity.Zone;
import com.example.yinqinghao.childprotect.receiver.LocationAlarmReceiver;
import com.example.yinqinghao.childprotect.service.LocationService;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.clustering.ClusterManager;
import com.wooplr.spotlight.utils.SpotlightSequence;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MapsFragment extends android.app.Fragment implements OnMapReadyCallback,
        GetLocationTask.LocationResponse {
    private GoogleMap mMap;

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 123;
    private static final int ZONE_ACTIVITY = 876;
    private static final String TAG = "MapsFragment";
    private GetLocationTask locationTask;
    private Location mLocation;

    private MapView mMapView;
    private View mView;
    private FloatingActionMenu mMenuHistory;
    private FloatingActionButton mButtenRealTime;
    private FloatingActionButton mButtonUploadLocation;
    private FloatingActionButton mButtonPause;
    private TextView mTextDate;
    private TextView mTextSpeed;
    private View mHisTutor;
    private Spinner mSpinner;

    private FirebaseDatabase mDb;
    private ValueEventListener mLocationDataValueListner;
    private ValueEventListener mZoneDataListener;
    private ValueEventListener mFriendIdValueListener;
    private ValueEventListener mGroupValueListener;
    private ValueEventListener myLocationListener;
    private Query myLocationQuery;

    private Handler mUiHandler = new Handler();
    private Handler mRouteHandler;

    private Stack<Runnable> mHisCallbacks;
    private List<String> mGroupIds;
//    private List<Group> mGroups;
    private List<String> mGroupNames;
    private String mCurrentGid;
    private Group mCurrentGroup;
    private Person mMe;
    private String uid;
//    private MarkerItem mMyMarkerItem;
    private Map<String, Person> mFriends;
    private Map<String, MarkerItem> mFriendMarkerItems;
    private List<Circle> mZones;
    private List<Marker> mCenters;
    private List<Polyline> mLines;
    private ClusterManager<MarkerItem> mClusterManager;
    private long mTodayTime;
    private boolean isFirst = true;
    private boolean showTutorial = true;
    private LocationAlarmReceiver.SpeedListener mSpeedListener;

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
            mButtonUploadLocation = (FloatingActionButton) mView.findViewById(R.id.fab_upload);
            mButtonPause = (FloatingActionButton) mView.findViewById(R.id.fab_stop);
            mHisTutor = mView.findViewById(R.id.historyTutor);
            mTextDate = (TextView) mView.findViewById(R.id.txt_date);
            mMenuHistory.hideMenuButton(false);
            mButtenRealTime.hide(false);
            mButtonUploadLocation.hide(false);
            mButtonPause.hide(false);
            mTextSpeed = (TextView) mView.findViewById(R.id.txt_speed);
            mSpeedListener = new LocationAlarmReceiver.SpeedListener() {
                @Override
                public void postGetSpeed(double speed) {
                    String s = String.format("%.1f", (speed*3600)/1000) + " km/h";
                    mTextSpeed.setText(s);
                }
            };
            LocationAlarmReceiver.registerSpeedListener(mSpeedListener);

            mTodayTime = Person.getDatetime();
            mFriendMarkerItems = new HashMap<>();
            mZones = new ArrayList<>();
            mCenters = new ArrayList<>();
            mGroupNames = new ArrayList<>();
            mHisCallbacks = new Stack<>();

            getFamilyIds();
            initListener();
            setSpinner();
        }
    }

    private void setSpinner() {
        for (String s : mGroupIds) {
            DatabaseReference refGroups = mDb.getReference("group")
                    .child(s);
            refGroups.addListenerForSingleValueEvent(mGroupValueListener);
        }
    }

    private void getFamilyIds() {
        SharedPreferences sp = getActivity().getSharedPreferences("ID", Context.MODE_PRIVATE);
        String groupIds = sp.getString("groupIds",null);
        if (groupIds != null) {
            Type listType = new TypeToken<List<String>>(){}.getType();
            mGroupIds = new Gson().fromJson(groupIds, listType);
        }
        uid = sp.getString("uid", null);
    }

    private void showHistoryPicker(Person person) {
        final Dialog d = new Dialog(getActivity());
        d.setTitle("DatePicker");
        d.setContentView(R.layout.picker);
        Button btnOK = (Button) d.findViewById(R.id.btn_ok);
        Button btnCancel = (Button) d.findViewById(R.id.btn_cancel);
        final WheelPicker wheelPicker = (WheelPicker) d.findViewById(R.id.main_wheel_center);
        final Map<String,Map<String, LocationData>> locationMap = person.getLocationDatas();
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
                showDate(strDate);
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

    private void showDate(String date) {
        mTextDate.setText(date);
        mTextDate.setVisibility(View.VISIBLE);
    }

    private void drawRoute(Map<String, LocationData> locationDatas) {
        removeLocationListener();
        TreeMap<String, LocationData> dataTreeMap = Person.sortLocationsAESC(locationDatas);
        mRouteHandler = new Handler();
        final PolylineOptions routeOption = new PolylineOptions();
        Object [] locations = dataTreeMap.values().toArray();
        long delay = 800;
        double lat = 0;
        double lng = 0;

        if (mClusterManager != null) {
            for (Marker marker: mClusterManager.getMarkerCollection().getMarkers()) {
                marker.remove();
            }

            for (Marker marker : mClusterManager.getClusterMarkerCollection().getMarkers()) {
                marker.remove();
            }

            mClusterManager.clearItems();
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
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        LatLng camera = new LatLng(l.getLat(), l.getLng());
                        routeOption.add(camera);
                        Polyline polyline = mMap.addPolyline(routeOption);
                        mLines.add(polyline);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(camera, 15));
                    }
                };

                mHisCallbacks.add(r);
                mRouteHandler.postDelayed(r, delay);
                delay += 800;
            }
            lat = l.getLat();
            lng = l.getLng();
        }
    }

    private void setmFab() {
        mMenuHistory.removeAllMenuButtons();
        for (final Person person : mFriends.values()) {
            final FloatingActionButton childHistoryFab = new FloatingActionButton(getActivity());
            childHistoryFab.setButtonSize(FloatingActionButton.SIZE_MINI);
            childHistoryFab.setLabelText(person.getFirstName());
            childHistoryFab.setImageResource(R.drawable.ic_child_care_black_24dp);
            mMenuHistory.addMenuButton(childHistoryFab);
            childHistoryFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHistoryPicker(person);
                    mMenuHistory.close(true);
                }
            });
        }
        mMenuHistory.setClosedOnTouchOutside(true);

        mButtenRealTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopHistroyRoute();
                mMap.clear();
                mClusterManager.clearItems();
                mTextDate.setVisibility(View.GONE);
                markMyLocation();
                getChildLocation();
            }
        });

        mButtonUploadLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LocationService.class);
                getActivity().startService(intent);
                SharedData.setStartedService(true);
                mButtonUploadLocation.hide(false);
                mButtonPause.show(false);
//                updateMyLocationMarker();
                Toast.makeText(getActivity(), "Uploading your location now.", Toast.LENGTH_SHORT).show();
            }
        });

        mButtonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), LocationService.class);
                getActivity().stopService(i);
                SharedData.setStartedService(false);
                SharedData.clearSpeedData();
                mButtonUploadLocation.show(false);
                mButtonPause.hide(false);
                removeMyLocationMarker();
                Toast.makeText(getActivity(), "Stop uploading.", Toast.LENGTH_SHORT).show();
            }
        });

        showFab();
    }

    private void stopHistroyRoute() {
        while (mHisCallbacks!= null && mHisCallbacks.size() != 0) {
            Runnable r = mHisCallbacks.pop();
            if (mRouteHandler != null) {
                mRouteHandler.removeCallbacks(r);
            }
        }
    }
//    private void updateMyLocationMarker() {
//        myLocationQuery = mDb.getReference("userInfo")
//                .child(uid)
//                .child("locationDatas")
//                .child(mTodayTime+"")
//                .orderByKey()
//                .limitToLast(1);
//        myLocationQuery.addValueEventListener(myLocationListener);
//    }

    private void removeMyLocationMarker() {
        if (myLocationQuery != null) {
            myLocationQuery.removeEventListener(myLocationListener);
        }
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
            }
        }, 550);

        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mButtonUploadLocation.show(true);
                showTutorial(mButtenRealTime, mHisTutor, mButtonUploadLocation, mSpinner);
            }
        }, 700);
    }

    private void setmCurrentGid(int position) {
        mCurrentGid = mGroupIds.get(position);
        SharedPreferences sp = getActivity().getSharedPreferences("ID", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("currentGid", mCurrentGid);
        editor.putString("currentGName", mGroupNames.get(position));
        editor.apply();
    }

    private void initListener() {
        mDb = FirebaseDatabase.getInstance();

//        myLocationListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (isFirst) {
//                    isFirst = false;
//                    return;
//                }
//                if (dataSnapshot.exists()) {
//                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                        LocationData locationData = ds.getValue(LocationData.class);
//                        setmMyMarkerItem(new LatLng(locationData.getLat(),locationData.getLng()));
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };

        mGroupValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Group g = dataSnapshot.getValue(Group.class);
//                    mGroups.add(g);
                    mGroupNames.add(g.getName());
                }

                if (mGroupIds.size() == mGroupNames.size()) {
                    Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
                    mSpinner = new Spinner(getActivity());
                    ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getActivity(),
                            R.layout.spinner_item,
                            mGroupNames);
                    mSpinner.setAdapter(spinnerArrayAdapter);
                    mSpinner.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                    mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            mMap.clear();
                            mClusterManager.clearItems();
                            markMyLocation();
                            setmCurrentGid(position);
                            getChildLocation();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    toolbar.removeViewAt(1);
                    toolbar.addView(mSpinner);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

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
                    Person person = mFriends.get(childId);
                    if (person == null ) return;
                    Map<String,Map<String, LocationData>> childLocations = person.getLocationDatas();
                    if (childLocations.containsKey(mTodayTime+"")) {
                        childLocations.get(mTodayTime+"").put(locationData.getDatetime().getTime()+"",locationData);
                    } else {
                        Map<String, LocationData> locationDatas = new HashMap<>();
                        locationDatas.put(locationData.getDatetime().getTime()+"",locationData);
                        childLocations.put(mTodayTime + "", locationDatas);
                    }

                    if (mFriendMarkerItems.containsKey(childId)) {
                        MarkerItem childMarker = mFriendMarkerItems.get(childId);
                        if (mClusterManager != null) {
                            mClusterManager.removeItem(childMarker);
                        }

                        String speed = String.format("%.1f", (locationData.getSpeed()*3600)/1000) + " km/h";
                        String snippet = "Speed: " + speed + "\n" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(locationData.getDatetime());

                        MarkerItem newMarkerItem = new MarkerItem(new LatLng(locationData.getLat(),locationData.getLng()),
                                person.getFirstName(), snippet);
                        mClusterManager.addItem(newMarkerItem);
                        mClusterManager.cluster();
                        mFriendMarkerItems.put(childId, newMarkerItem);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadChildrenLocation:onCancelled", databaseError.toException());
            }
        };

//        mFriendValueListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    DatabaseReference refFriends = mDb.getReference("family")
//                            .child(mFamilyId);
//                    String id = "";
//                    mFriends = new HashMap<>();
//                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                        Person person = ds.getValue(Person.class);
//                        if (person.getUid().equals(mAuth.getCurrentUser().getUid())) {
//                            mMe = person;
//                            continue;
//                        }
//                        id = person.getUid();
//                        mFriends.put(id, person);
//                        addChildMarker(person);
//                        com.google.firebase.database.Query queryLocation = refChildren.child(id)
//                                .child("locationDatas").child(mTodayTime+"").orderByKey()
//                                .limitToLast(1);
//                        queryLocation.addValueEventListener(mLocationDataValueListner);
//                    }
//                    setmFab();
//
//                    if (mFriends.size() > 0) {
//                        if (mFriends.get(id).getMostRecentLocation() == null) return;
//                        LocationData locationData = mFriends.get(id).getMostRecentLocation();
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                                new LatLng(locationData.getLat(), locationData.getLng()), 13));
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.w(TAG, "loadChildren:onCancelled", databaseError.toException());
//            }
//        };
        mZoneDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (Circle c : mZones) {
                        c.remove();
                    }
                    mCenters = new ArrayList<>();
                    mZones = new ArrayList<>();
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

        mFriendIdValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mCurrentGroup = dataSnapshot.getValue(Group.class);
                    final List<String> userIds = new ArrayList<>(mCurrentGroup.getUsers().keySet());
                    mFriends = new HashMap<>();
                    for (String s : userIds) {
                        final DatabaseReference refUid = mDb.getReference("userInfo").child(s);
                        refUid.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String id = "";
                                if (dataSnapshot.exists()) {
                                    Person person = dataSnapshot.getValue(Person.class);
                                    if (person.getUid().equals(uid)) {
                                        mMe = person;
//                                        SharedData.addUser(mMe.getUid(), mMe);
                                    } else {
                                        id = person.getUid();
                                        mFriends.put(id, person);
                                        addChildMarker(person);
                                        com.google.firebase.database.Query queryLocation = refUid
                                                .child("locationDatas").child(mTodayTime + "").orderByKey()
                                                .limitToLast(1);
                                        queryLocation.addValueEventListener(mLocationDataValueListner);
                                    }
                                }
                                setFab(userIds,id);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
//        mFidValueListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    mFamilyId = dataSnapshot.getValue().toString();
//                    DatabaseReference refChildren = mDb.getReference("family")
//                            .child(mFamilyId);
//                    refChildren.addListenerForSingleValueEvent(mFriendValueListener);
//                    getZones();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.w(TAG, "loadFamilyId:onCancelled", databaseError.toException());
//            }
//        };
    }

    private void setFab(List<String> userIds, String id) {
        if (mFriends.size() == userIds.size() -1 || userIds.size() == 1) {
            setmFab();

            if (mFriends.size() > 0) {
                if (mFriends.get(id) == null || mFriends.get(id).getMostRecentLocation() == null) return;
                LocationData locationData = mFriends.get(id).getMostRecentLocation();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(locationData.getLat(), locationData.getLng()), 13));
            }
        }
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
        DatabaseReference refFriendsID = mDb.getReference("group").child(mCurrentGid);
        refFriendsID.addListenerForSingleValueEvent(mFriendIdValueListener);
        getZones();
    }

    private void getZones() {
        DatabaseReference refZone = mDb.getReference("zone")
                .child(mCurrentGid);
        refZone.addValueEventListener(mZoneDataListener);
    }

    private void removeLocationListener() {
        DatabaseReference refChildren = mDb.getReference("family")
                .child(mCurrentGid);
        for (Person person : mFriends.values()) {
            com.google.firebase.database.Query queryLocation = refChildren.child(person.getUid())
                    .child("locationDatas").child(mTodayTime+"").orderByKey()
                    .limitToLast(1);
            queryLocation.removeEventListener(mLocationDataValueListner);
        }
    }

    private void addChildMarker(Person c) {
        if (mFriendMarkerItems.size() != 0) {
            for (MarkerItem mi: mFriendMarkerItems.values()) {
                mClusterManager.removeItem(mi);
            }
        }
        LocationData locationData = c.getMostRecentLocation();
        if (locationData == null)   return;
        MarkerItem newMarkerItem = new MarkerItem(new LatLng(locationData.getLat(),locationData.getLng()),
                c.getFirstName(), new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(locationData.getDatetime()));
        mClusterManager.addItem(newMarkerItem);
        mClusterManager.cluster();
        mFriendMarkerItems.put(c.getUid(), newMarkerItem);

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
//                    try {
//                        if (locationTask == null) {
//                            locationTask = new GetLocationTask(this, getActivity(), 4);
//                        }
//                        locationTask.execute();
//                    } catch (IllegalStateException ex) {
//
//                    }
                    try {
                        mMap.setMyLocationEnabled(true);
                    } catch (SecurityException ex) {

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
        checkPermission();
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter(getActivity()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.8668, 145.016), 13));
        setUpClusterer();
//        getChildLocation();
//        locationTask = new GetLocationTask(this, getActivity(), 4);
        markMyLocation();
//        try {
//            locationTask.execute();
//        } catch (IllegalStateException ex) {
//
//        }
    }

    private void setUpClusterer() {
        mClusterManager = new ClusterManager<>(getActivity(), mMap);
        MarkerRender render = new MarkerRender(getActivity(),mMap,mClusterManager);
        render.setMinClusterSize(1);
        mClusterManager.setRenderer(render);
        mMap.setOnCameraIdleListener(mClusterManager);
    }

    private void markMyLocation() {
        locationTask = new GetLocationTask(this, getActivity(), 1);
        locationTask.execute();
    }

//    @Override
//    public void locationProcessFinish(Location location) {
//        locationTask = null;
//        mLocation = location;
//        if (mLocation == null) {
//            Log.d(TAG, "can't get the location");
//            return;
//        }
//        Location camera = mLocation;
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                new LatLng(camera.getLatitude(), camera.getLongitude()), 13));
//
//        LatLng myLatLng = new LatLng(mLocation.getLatitude(),mLocation.getLongitude());
//        setmMyMarkerItem(myLatLng);
//    }

//    private void setmMyMarkerItem(LatLng myLatLng) {
//        if (mMyMarkerItem != null) {
//            mClusterManager.removeItem(mMyMarkerItem);
//        }
//        mMyMarkerItem = new MarkerItem(myLatLng, "My Location", "");
//        mMyMarkerItem.setmIcon(BitmapDescriptorFactory.defaultMarker(
//                BitmapDescriptorFactory.HUE_AZURE));
//        mClusterManager.addItem(mMyMarkerItem);
//        mClusterManager.cluster();
//    }

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

    @Override
    public void onResume() {
        if (mMap != null && !mMap.isMyLocationEnabled()) {
            try {
                mMap.setMyLocationEnabled(true);
            } catch (SecurityException ex) {

            }
        }

        if (SharedData.isStartedService()) {
            mButtonUploadLocation.hide(false);
            mButtonPause.show(false);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mMap != null && mMap.isMyLocationEnabled()) {
            try {
                mMap.setMyLocationEnabled(false);
            } catch (SecurityException ex) {

            }
        }
        super.onPause();
    }

    private void showTutorial(View view, View view2, View view3, View view4) {
        if (SharedData.isShowTutorial1()) {
            SpotlightSequence.getInstance(getActivity(),null)
                    .addSpotlight(view,
                            "Real-Time Location", "See the location of your family and friends ", SharedData.getRandomStr())
                    .addSpotlight(view2,
                            "History Route", "Click here to see history route of family and friends", SharedData.getRandomStr())
                    .addSpotlight(view3,
                            "Update Location", "Click here to switch on/off location", SharedData.getRandomStr())
                    .addSpotlight(view4,
                            "Friends Groups", "Click here to see your social personal safety network of friends and family", SharedData.getRandomStr())
                    .startSequence();
        }
    }

    @Override
    public void locationProcessFinish(Location location) {
        locationTask = null;
        if (location != null) {
            CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(),location.getLongitude()))
                .zoom(13).build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        }
    }
}
