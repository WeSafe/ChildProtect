package com.example.yinqinghao.childprotect.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aigestudio.wheelpicker.WheelPicker;
import com.example.yinqinghao.childprotect.GroupsActivity;
import com.example.yinqinghao.childprotect.MainActivity;
import com.example.yinqinghao.childprotect.Manifest;
import com.example.yinqinghao.childprotect.adapter.GroupListAdapter;
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
import com.example.yinqinghao.childprotect.service.MessagingService;
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
import com.google.android.gms.maps.model.LatLngBounds;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
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
    private static final int GROUP_ACTIVITY = 9870;
    private static final String TAG = "MapsFragment";
    private GetLocationTask locationTask;

    private MapView mMapView;
    private View mView;
    private FloatingActionMenu mMenuHistory;
    private FloatingActionButton mButtenRealTime;
    private FloatingActionButton mButtonUploadLocation;
    private FloatingActionButton mButtonPause;
    private TextView mTextDate;
//    private TextView mTextSpeed;
    private View mHisTutor;
//    private Spinner mSpinner;
    private ListView mGroupListView;
    private TextView mGroupTextView;
    private ImageView mGroupArrow;

    private FirebaseDatabase mDb;
    private ValueEventListener mLocationDataValueListner;
    private ValueEventListener mZoneDataListener;
    private ValueEventListener mFriendIdValueListener;
    private ValueEventListener mGroupValueListener;
    private ValueEventListener mFidListener;

    private Handler mUiHandler = new Handler();
    private Handler mRouteHandler;
    protected Activity mActivity;

    private Stack<Runnable> mHisCallbacks;
    private List<String> mGroupIds;
    private List<String> mGroupNames;
    private String mCurrentGid;
    private Group mCurrentGroup;
    private Person mMe;
    private String uid;
    private Map<String, Person> mFriends;
    private Map<String, MarkerItem> mFriendMarkerItems;
    private List<Circle> mZones;
    private List<Marker> mCenters;
    private List<Polyline> mLines;
    private ClusterManager<MarkerItem> mClusterManager;
    private long mTodayTime;
    private boolean isFirst = true;
    private boolean isDown = true;
    private int mItemHeight;
    private LocationAlarmReceiver.SpeedListener mSpeedListener;
    private MessagingService.RefreshGroupListener mRefreshGroupListener;
    private AdapterView.OnItemSelectedListener mOnItemSelectedListener;
    private View.OnClickListener mArrowListener;

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
            if (mActivity == null) {
                mActivity = getActivity();
            }
            MapsInitializer.initialize(mActivity);
            mMapView = (MapView) mView.findViewById(R.id.map);
            mMapView.onCreate(savedInstanceState);
            mMapView.onResume();
            mMapView.getMapAsync(this);

            mGroupListView = (ListView) mView.findViewById(R.id.list_group);
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
//            mTextSpeed = (TextView) mView.findViewById(R.id.txt_speed);
//            mSpeedListener = new LocationAlarmReceiver.SpeedListener() {
//                @Override
//                public void postGetSpeed(double speed) {
//                    String s = String.format("%.1f", (speed*3600)/1000) + " km/h";
//                    mTextSpeed.setText(s);
//                }
//            };
//            LocationAlarmReceiver.registerSpeedListener(mSpeedListener);

            mRefreshGroupListener = new MessagingService.RefreshGroupListener() {
                @Override
                public void refreshGroup(String gid, final String gName, final String action) {
//                    List<String> toRemove = new ArrayList<>();
//                    if (mGroupIds != null) {
//                        for (String s : mGroupIds) {
//                            if (s.equals(gid)) {
//                                toRemove.add(s);
//                            }
//                        }
//
//                        mGroupIds.removeAll(toRemove);
//
//                    }
                    Activity activity = (Activity) SharedData.peekContext();
                    if (activity != null) {
                        String title = activity.getTitle().toString();
                        if (title.equals("Scan QR code (" + gName + ")")) {
                            SharedData.popContext();
                            activity.finish();
                            activity = (Activity) SharedData.peekContext();
                            title = activity.getTitle().toString();
                        }

                        if (title.equals(gName)) {
                            SharedData.popContext();
                            activity.finish();
                        }
                    }
                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            if (action.equals("remove")) {
                                Toast.makeText(mActivity, "You are removed from " + gName, Toast.LENGTH_SHORT).show();
                            } else if (action.equals("join")) {
                                Toast.makeText(mActivity, "Welcome to " + gName, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    setSpinner();
                }
            };
            MessagingService.registerListener(mRefreshGroupListener);

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
        DatabaseReference refFid = mDb.getReference("user")
                .child(uid);
        refFid.addListenerForSingleValueEvent(mFidListener);
    }

    private void getFamilyIds() {
        SharedPreferences sp = mActivity.getSharedPreferences("ID", Context.MODE_PRIVATE);
        uid = sp.getString("uid", null);
    }

    private void showHistoryPicker(Person person) {
        final Dialog d = new Dialog(mActivity);
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
        int count = 0;
        for (String sTime: dates) {
            if (++count > 7 ) {
                break;
            }
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
//        mRouteHandler = new Handler();
        final PolylineOptions routeOption = new PolylineOptions();
        Object [] locations = dataTreeMap.values().toArray();
        List<LatLng> latLngs = new ArrayList<>();
        LatLngBounds.Builder builder =  new LatLngBounds.Builder();
        for (Object o: locations) {
            LocationData locationData = (LocationData) o;
            LatLng latLng = new LatLng(locationData.getLat(), locationData.getLng());
            latLngs.add(latLng);
            builder.include(latLng);
        }
//        long delay = 800;
//        double lat = 0;
//        double lng = 0;

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
        routeOption.addAll(latLngs);
        Polyline polyline = mMap.addPolyline(routeOption);
        mLines.add(polyline);
//        LatLng camera = new LatLng(latLngs.get(0).latitude, latLngs.get(0).longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 20));
//        for (final Object o: locations) {
//            final LocationData l = (LocationData) o;
//            if (lat != l.getLat() || lng != l.getLng()) {
//                Runnable r = new Runnable() {
//                    @Override
//                    public void run() {
//                        LatLng camera = new LatLng(l.getLat(), l.getLng());
//                        routeOption.add(camera);
//                        Polyline polyline = mMap.addPolyline(routeOption);
//                        mLines.add(polyline);
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(camera, 15));
//                    }
//                };
//
//                mHisCallbacks.add(r);
//                mRouteHandler.postDelayed(r, delay);
//                delay += 800;
//            }
//            lat = l.getLat();
//            lng = l.getLng();
//        }
    }

    private void setmFab() {
        mMenuHistory.removeAllMenuButtons();
        for (final Person person : mFriends.values()) {
            final FloatingActionButton childHistoryFab = new FloatingActionButton(mActivity);
            childHistoryFab.setButtonSize(FloatingActionButton.SIZE_MINI);
            childHistoryFab.setLabelText(person.getFirstName());
            childHistoryFab.setImageResource(R.drawable.ppl);
            childHistoryFab.setColorNormalResId(R.color.buttonColor);
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
                Intent intent = new Intent(mActivity, LocationService.class);
                mActivity.startService(intent);
                SharedData.setStartedService(true);
                mButtonUploadLocation.hide(false);
                mButtonPause.show(false);
                Toast.makeText(mActivity, "Uploading your location now.", Toast.LENGTH_SHORT).show();
            }
        });

        mButtonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mActivity, LocationService.class);
                mActivity.stopService(i);
                SharedData.setStartedService(false);
                SharedData.clearSpeedData();
                mButtonUploadLocation.show(false);
                mButtonPause.hide(false);
                Toast.makeText(mActivity, "Stop uploading.", Toast.LENGTH_SHORT).show();
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
                showTutorial(mButtenRealTime, mHisTutor, mButtonUploadLocation, mGroupTextView);
            }
        }, 700);
    }

    private void setmCurrentGid(int position) {
        mCurrentGid = mGroupIds.get(position);
        String gname = mGroupNames.get(position);
        SharedPreferences sp = mActivity.getSharedPreferences("ID", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("currentGid", mCurrentGid);
        editor.putString("currentGName", gname);
        editor.apply();
        if (mGroupTextView != null) {
            mGroupTextView.setText(gname);
        }
    }

    private void createNewGroup(List<String> g) {
        SharedPreferences sp = mActivity.getSharedPreferences("ID", Context.MODE_PRIVATE);
        DatabaseReference refGroup = mDb.getReference("group");
        String newGid = refGroup.push().getKey();
        String token = FirebaseInstanceId.getInstance().getToken();
        Map<String, String> users = new HashMap<>();
        users.put(uid, token);
        String fname = sp.getString("fName","");
        Group group = new Group(newGid, fname + "'s Group",uid, users);
        refGroup.child(newGid).setValue(group);

        DatabaseReference refU = mDb.getReference("user")
                .child(uid).child(newGid);
        refU.setValue(true);
        g.add(newGid);
    }

    private void initListener() {
        mDb = FirebaseDatabase.getInstance();
//        mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                mMap.clear();
//                mClusterManager.clearItems();
//                markMyLocation();
//                if (mGroupNames.size() != 0) {
//                    setmCurrentGid(position);
//                }
//                getChildLocation();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        };
        mArrowListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDown) {
                    isDown = false;
                    mGroupArrow.setImageResource(R.drawable.up);
                    mGroupListView.setVisibility(View.VISIBLE);
                } else {
                    isDown = true;
                    mGroupArrow.setImageResource(R.drawable.down);
                    mGroupListView.setVisibility(View.GONE);
                }
            }
        };

        mFidListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mGroupIds = new ArrayList<>();
                mGroupNames = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String fid = ds.getKey().toString();
                        mGroupIds.add(fid);
                    }
                }

                if (mGroupIds.size() == 0) {
                    createNewGroup(mGroupIds);
                }

                SharedData.setmGroupId(mGroupIds);
                for (String s : mGroupIds) {
                    DatabaseReference refGroups = mDb.getReference("group")
                            .child(s);
                    refGroups.addListenerForSingleValueEvent(mGroupValueListener);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        mGroupValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Group g = dataSnapshot.getValue(Group.class);
                    mGroupNames.add(g.getName());
                }

                if (mGroupIds.size() == mGroupNames.size()) {
                    Toolbar toolbar = (Toolbar) mActivity.findViewById(R.id.toolbar);
//                    mSpinner = new Spinner(getActivity());
//                    ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getActivity(),
//                            R.layout.spinner_item,
//                            mGroupNames);
//
//                    mSpinner.setAdapter(spinnerArrayAdapter);
//                    mSpinner.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
//                    mSpinner.setOnItemSelectedListener(mOnItemSelectedListener);
//                    toolbar.removeViewAt(1);
//                    toolbar.addView(mSpinner);
                    GroupListAdapter adapter = new GroupListAdapter(mActivity, mGroupNames);
                    mGroupListView.setAdapter(adapter);

                    CoordinatorLayout.LayoutParams p = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (mGroupNames.size() > 5) {
                        mGroupListView.getLayoutParams().height = 680;
                    } else {
                        mGroupListView.setLayoutParams(p);
                    }
                    mGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            itemClick(position);
                        }
                    });

                    if (isFirst) {
                        isFirst = false;
                        mGroupTextView = new TextView(mActivity);
                        mGroupTextView.setTextSize(20);
                        mGroupArrow = new ImageView(mActivity);
                        mGroupTextView.setTextColor(Color.WHITE);
                        mGroupTextView.setTypeface(null, Typeface.BOLD);
                        mGroupArrow.setImageResource(R.drawable.down);
                        mGroupListView.setVisibility(View.GONE);
                        mGroupArrow.setOnClickListener(mArrowListener);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(40, 40);
                        layoutParams.setMargins(80, 0, 0, 0);
                        mGroupArrow.setLayoutParams(layoutParams);
                        mGroupTextView.setOnClickListener(mArrowListener);
                        toolbar.addView(mGroupTextView);
                        toolbar.addView(mGroupArrow);
                    }

                    SharedPreferences sp = mActivity.getSharedPreferences("ID", Context.MODE_PRIVATE);
                    String currentGid = sp.getString("currentGid", null);
                    if (mCurrentGid == null || currentGid == null
                            || ( currentGid != null && !mGroupIds.contains(currentGid))
                            || (mCurrentGid != null && !mGroupIds.contains(mCurrentGid))) {
                        setmCurrentGid(0);
                        stopHistroyRoute();
                        mMap.clear();
                        mClusterManager.clearItems();
                        mTextDate.setVisibility(View.GONE);
                        markMyLocation();
                        getChildLocation();
                    }

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

    private void itemClick(int pos) {
        mMap.clear();
        mClusterManager.clearItems();
        markMyLocation();
        if (mGroupNames.size() != 0) {
            setmCurrentGid(pos);
        }
        getChildLocation();
        mGroupListView.setVisibility(View.GONE);
        mGroupArrow.setImageResource(R.drawable.down);
        isDown = true;
        mGroupTextView.setText(mGroupNames.get(pos));
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
        int strokeColor = ContextCompat.getColor(mActivity ,
                isSafe ? R.color.safeStrokeColor : R.color.dangerStrokeColor);
        int fillColor = ContextCompat.getColor(mActivity ,
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
                    try {
                        mMap.setMyLocationEnabled(true);
                    } catch (SecurityException ex) {

                    }
                } else
                    Toast.makeText(mActivity, "Can't get the location, please grant the permission",
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
        mMap.setMapStyle(SharedData.getMapStyleOptions());
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter(mActivity));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.8668, 145.016), 13));
        setUpClusterer();
        markMyLocation();
//        try {
//            locationTask.execute();
//        } catch (IllegalStateException ex) {
//
//        }
    }

    private void setUpClusterer() {
        mClusterManager = new ClusterManager<>(mActivity, mMap);
        MarkerRender render = new MarkerRender(mActivity,mMap,mClusterManager);
        render.setMinClusterSize(1);
        mClusterManager.setRenderer(render);
        mMap.setOnCameraIdleListener(mClusterManager);
    }

    private void markMyLocation() {
        locationTask = new GetLocationTask(this, mActivity, 1);
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
            if (resultCode == mActivity.RESULT_OK) {
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

        if (requestCode == GROUP_ACTIVITY) {
//            mGroupIds = new ArrayList<>();
            if (resultCode == mActivity.RESULT_OK) {
//                List<Group> groups = data.getParcelableArrayListExtra("groups");
//                for (Group g: groups) {
//                    mGroupIds.add(g.getId());
//                }
                setSpinner();
            }
            return;
        }
//        super.onActivityResult(requestCode, resultCode, data);
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
            SpotlightSequence.getInstance(mActivity,null)
                    .addSpotlight(view,
                            "Real-Time Location", "See the location of your family and friends ", SharedData.getRandomStr())
                    .addSpotlight(view2,
                            "History Route", "Click here to see history route of family and friends", SharedData.getRandomStr())
                    .addSpotlight(view3,
                            "Share Location", "Click here to switch on/off for sharing location", SharedData.getRandomStr())
                    .addSpotlight(view4,
                            "Friends Groups", "Click here to see your social personal safety network of friends and family", SharedData.getRandomStr())
                    .startSequence();
        }
    }

    public void showTuturial() {
        if (mButtenRealTime == null
                || mHisTutor == null
                || mButtonUploadLocation == null
                || mGroupTextView == null)
            return;
        showTutorial(mButtenRealTime, mHisTutor, mButtonUploadLocation, mGroupTextView);
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

    /**
     * Called when a fragment is first attached to its context.
     * {@link #onCreate(Bundle)} will be called after this.
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            mActivity = (Activity) context;
        }
    }
}
