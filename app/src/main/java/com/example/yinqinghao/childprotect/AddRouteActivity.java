package com.example.yinqinghao.childprotect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.FlatButton;
import com.example.yinqinghao.childprotect.asyncTask.GetJsonTask;
import com.example.yinqinghao.childprotect.asyncTask.GetLocationTask;
import com.example.yinqinghao.childprotect.entity.Route;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;

import java.lang.reflect.Type;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AddRouteActivity extends AppCompatActivity implements OnMapReadyCallback,
        GetLocationTask.LocationResponse, GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener,
        GetJsonTask.AsyncResponse {
    private final String BASE_GOOGLE_ROUTE_URL = "https://maps.googleapis.com/maps/api/directions/json?";
    private boolean isEdit;
    private String mMode;
    private String mLatlngs;

    private GoogleMap mMap;
    private FirebaseDatabase mDb;

    private MapView mMapView;
    private EditText mDesEditText;
    private FlatButton mConfirm;
    private FlatButton mAddWayPoint;
    private FlatButton mGetRoute;
    private RadioGroup mRouteRadioGroup;
    private TextView mTextDD;

    private GetLocationTask locationTask;
    private GetJsonTask getJsonTask;

    private Marker mStartMarker;
    private Marker mEndMarker;
    private List<Marker> mWayPoints;
    private Polyline mRouteLine;
    private Route mRoute;

    private View.OnClickListener mAddWayPointListener;
    private View.OnClickListener mGetRouteListener;
    private View.OnClickListener mConfirmListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_route);

        MapsInitializer.initialize(this);
        mMapView = (MapView) findViewById(R.id.map_add_route);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);
        mDb = FirebaseDatabase.getInstance();

        mRouteRadioGroup = (RadioGroup) findViewById(R.id.radioGroup_route);
        mConfirm = (FlatButton) findViewById(R.id.btn_confirm_route);
        mAddWayPoint = (FlatButton) findViewById(R.id.btn_add_wp);
        mGetRoute = (FlatButton) findViewById(R.id.btn_get_route);
        mDesEditText = (EditText) findViewById(R.id.txt_route_des);
        mTextDD = (TextView) findViewById(R.id.txt_dd);
        mDesEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        mWayPoints = new ArrayList<>();

        initListener();
        mAddWayPoint.setOnClickListener(mAddWayPointListener);
        mGetRoute.setOnClickListener(mGetRouteListener);
        mConfirm.setOnClickListener(mConfirmListener);
    }

    private void initListener() {
        mAddWayPointListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double offset = 0.005 / 2;
                LatLng location = new LatLng(mMap.getCameraPosition().target.latitude + offset,
                        mMap.getCameraPosition().target.longitude + offset);

                Marker wayPoint = mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .anchor(0.0f, 1.0f)
                        .title("click to delete, long click to drag")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                        .draggable(true));
                wayPoint.setTag("waypoint");
                wayPoint.showInfoWindow();
                mWayPoints.add(wayPoint);
            }
        };

        mGetRouteListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRouteLine != null) {
                    mRouteLine.remove();
                    mRouteLine = null;
                }
                String key = getString(R.string.GOOGLE_DIRECTION_API_KEY);
                LatLng startLocation = mStartMarker.getPosition();
                LatLng endLocation = mEndMarker.getPosition();
                String origin = startLocation.latitude + "," + startLocation.longitude;
                String destination = endLocation.latitude + "," + endLocation.longitude;
                mMode = "";
                switch (mRouteRadioGroup.getCheckedRadioButtonId()) {
                    case R.id.rb_driving:
                        mMode = "driving";
                        break;
                    case R.id.rb_walking:
                        mMode = "walking";
                        break;
                    case R.id.rb_bicycling:
                        mMode = "bicycling";
                        break;
                    case R.id.rb_transit:
                        mMode = "transit";
                        break;
                    default:
                        mMode = "driving";
                        break;
                }
                String wayPoints = "";
                List<LatLng> latLngs = new ArrayList<>();
                latLngs.add(mStartMarker.getPosition());
                latLngs.add(mEndMarker.getPosition());
                for (Marker marker : mWayPoints) {
                    LatLng position = marker.getPosition();
                    wayPoints += position.latitude + "," + position.longitude + "|";
                    latLngs.add(position);
                }
                int len = wayPoints.length();
                if (len != 0) {
                    wayPoints = wayPoints.substring(0,len-1);
                }
                mLatlngs = new Gson().toJson(latLngs);
                String parameters = "origin=" + origin + "&destination=" + destination + "&waypoints=" + wayPoints + "&mode=" + mMode + "&key=" + key;
                getJsonTask = new GetJsonTask(AddRouteActivity.this);
                getJsonTask.execute(BASE_GOOGLE_ROUTE_URL, parameters);
            }
        };

        mConfirmListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDesEditText.setError(null);
                if (mDesEditText.getText().toString().trim().length() == 0) {
                    Toast.makeText(AddRouteActivity.this, "Route description is required", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mRoute == null) {
                    Toast.makeText(AddRouteActivity.this, "Please get the route first", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences sp = getSharedPreferences("ID", Context.MODE_PRIVATE);
                final String familyId = sp.getString("currentGid",null);
                DatabaseReference refRoute = mDb.getReference("route")
                        .child(familyId);
                String des = mDesEditText.getText().toString().trim();
                Intent intent = getIntent();
                Route route = intent.getParcelableExtra("route");
                isEdit = route != null;
                String routeId;
                if (!isEdit) {
                    routeId = refRoute.push().getKey();
                } else {
                    routeId = route.getId();
                }
                mRoute.setId(routeId);
                mRoute.setDes(des);
                mRoute.setCreateDate(new Date());
                refRoute.child(routeId).setValue(mRoute).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("route",mRoute);
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        } else {
                            Toast.makeText(AddRouteActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap == null)
            return;
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        Intent intent = getIntent();
        Route route = intent.getParcelableExtra("route");
        if (route == null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.8668, 145.016), 13));
            locationTask = new GetLocationTask(this, this, 2);
            locationTask.execute();
        } else {
            String des = route.getDes();
            mMode = route.getMode();
            String points = route.getPoints();
            String latlngs = route.getLatlngs();
            Type listType = new TypeToken<List<LatLng>>(){}.getType();
            List<LatLng> ll = new Gson().fromJson(latlngs, listType);
            double dis = route.getDistance();
            long dur = route.getDuration();

            mDesEditText.setText(des);
            int id = 0;
            switch (mMode) {
                case "driving":
                    id = R.id.rb_driving;
                    break;
                case "walking":
                    id = R.id.rb_walking;
                    break;
                case "bicycling":
                    id = R.id.rb_bicycling;
                    break;
                case "transit":
                    id = R.id.rb_transit;
                    break;
                default:
                    id = R.id.rb_driving;
            }
            mRouteRadioGroup.check(id);
            setMarkers(ll);
            List<LatLng> locations = PolyUtil.decode(points);
            mRouteLine = mMap.addPolyline(new PolylineOptions().addAll(locations));
            mRoute = route;
            showDD(dis,dur);
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * press the back button
     * @param item  back button
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent parentIntent = NavUtils.getParentActivityIntent(this);
                if(parentIntent != null) {
                    Intent returnIntent = new Intent();
                    setResult(RESULT_CANCELED, returnIntent);
                    finish();
                    return true;
                } else {
                    parentIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
                            | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(parentIntent);
                    finish();
                    return true;
                }
            case R.id.action_start:
                Intent intent = new Intent(this, RouteGeoActivity.class);
                intent.putExtra("route", mRoute);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void locationProcessFinish(Location location) {
        locationTask = null;
        if (location == null) {
            Toast.makeText(this, "can't get current location.", Toast.LENGTH_SHORT).show();
            return;
        }
        setMarkers(location);
    }

    private void setMarkers(Location location) {
        double offset = 0.005;
        LatLng startLatLag = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng endLatLag = new LatLng(location.getLatitude()+offset, location.getLongitude()+offset);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                startLatLag, 14));
        mStartMarker = mMap.addMarker(new MarkerOptions()
                .position(startLatLag)
                .anchor(0.0f, 1.0f)
                .draggable(true));

        mEndMarker = mMap.addMarker(new MarkerOptions()
                .position(endLatLag)
                .anchor(0.0f, 1.0f)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .draggable(true));
    }

    private void setMarkers(List<LatLng> list) {
        mStartMarker = mMap.addMarker(new MarkerOptions()
                .position(list.get(0))
                .anchor(0.0f, 1.0f)
                .draggable(true));

        mEndMarker = mMap.addMarker(new MarkerOptions()
                .position(list.get(1))
                .anchor(0.0f, 1.0f)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .draggable(true));

        for (int i = 2; i < list.size(); i++) {
            Marker wayPoint = mMap.addMarker(new MarkerOptions()
                    .position(list.get(i))
                    .anchor(0.0f, 1.0f)
                    .title("click to delete, long click to drag")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                    .draggable(true));
            wayPoint.setTag("waypoint");
            wayPoint.showInfoWindow();
            mWayPoints.add(wayPoint);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mStartMarker.getPosition(), 14));
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        if (marker.getTag()!= null && marker.getTag().equals("waypoint")) {
            marker.hideInfoWindow();
        }
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getTag()!= null && marker.getTag().equals("waypoint")) {
            mWayPoints.remove(marker);
            marker.remove();
            return true;
        }
        return false;
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
                for (JsonElement element: legs) {
                    JsonObject leg = element.getAsJsonObject();
                    dis += leg.get("distance").getAsJsonObject().get("value").getAsInt();
                    dur += leg.get("duration").getAsJsonObject().get("value").getAsInt();
                }
                JsonObject polyline = routes.get("overview_polyline").getAsJsonObject();
                String points = polyline.get("points").getAsString();
                List<LatLng> locations = PolyUtil.decode(points);
                mRouteLine = mMap.addPolyline(new PolylineOptions().addAll(locations));
                mRoute = new Route(new Date(),"", points,"",dis,dur, mMode, mLatlngs);
                showDD((double)dis, dur);
            } else {
                Toast.makeText(this, "Can't get the route, please adjust the start/end/waypoint location",
                        Toast.LENGTH_SHORT).show();
            }

        }

    }

    private void showDD(double distance, long duration) {
        String dis = "";
        if (distance < 1000) {
            dis = distance + "m";
        } else {
            dis = (distance / 1000) + "km";
        }

        long days = TimeUnit.SECONDS.toDays(duration);
        duration -= TimeUnit.DAYS.toSeconds(days);
        long hours = TimeUnit.SECONDS.toHours(duration);
        duration -= TimeUnit.HOURS.toSeconds(hours);
        long minutes = TimeUnit.SECONDS.toMinutes(duration);
        duration -= TimeUnit.MINUTES.toSeconds(minutes);
        long seconds = duration;
        String dur = "";
        if (days == 0 && hours == 0 && minutes == 0) {
            dur = seconds + "s";
        } else if (days == 0 && hours == 0) {
            dur = minutes + "mins, " + seconds + "s";
        } else  if (days == 0) {
            dur = hours + "hours, " + minutes + "mins, " + seconds + "s";
        } else {
            dur = days + "days, " + hours + "hours, " + minutes + "mins, " + seconds + "s";
        }

        String str = "Distance: " + dis + ". " + "Duration: " + dur + ".";
        mTextDD.setText(str);
        mTextDD.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Intent intent = getIntent();
        if (intent.getParcelableExtra("route") != null) {
            getMenuInflater().inflate(R.menu.start, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }
}
