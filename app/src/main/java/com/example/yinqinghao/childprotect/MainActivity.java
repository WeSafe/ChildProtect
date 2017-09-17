package com.example.yinqinghao.childprotect;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brouding.simpledialog.SimpleDialog;
import com.example.yinqinghao.childprotect.entity.Group;
import com.example.yinqinghao.childprotect.entity.SharedData;
import com.example.yinqinghao.childprotect.fragment.MapsFragment;
import com.example.yinqinghao.childprotect.service.LocationService;
import com.example.yinqinghao.childprotect.service.RouteService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wooplr.spotlight.utils.SpotlightSequence;

import java.lang.reflect.Type;
import java.util.List;

import safety.com.br.android_shake_detector.core.ShakeCallback;
import safety.com.br.android_shake_detector.core.ShakeDetector;
import safety.com.br.android_shake_detector.core.ShakeOptions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    private static final int ZONE_ACTIVITY = 876;
    private static final int GROUP_ACTIVITY = 9870;

    private DrawerLayout drawer;
    private TextView usernameTextView;
    private TextView emailTextView;
    private ImageView sosImageView;
    private MenuItem accountNavItem;
    private SimpleDialog mDialog;
    private ShakeDetector mShakeDetector;
    private ShakeCallback mShakeCallack;

    private Fragment mMapFragment;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDb;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");



        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    if (mMapFragment == null)
                        mMapFragment = new MapsFragment();
                    Fragment home = mMapFragment;
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, home).commit();
                    SharedPreferences sp = getSharedPreferences("ID", Context.MODE_PRIVATE);
                    String name = sp.getString("name",null);
                    String email = sp.getString("email", null);
                    setUserInfo(email,name);
                    setUpShakeToSos();
                    popupSplashActivity();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivityForResult(intent,1);
                    SharedData.setIsSplashed(false);
                    return;
                }
            }
        };

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        accountNavItem = navigationView.getMenu().getItem(3);
        //find the login views
        usernameTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txtUserName);
        emailTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txtUserEmail);
        sosImageView = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.img_sos);
        sosImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SOSActivity.class);
                startActivity(intent);
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showTutorial(sosImageView);
                        }
                    }, 500);
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();
        setUpShakeToSos();
    }

    private void popupSplashActivity() {
        if (!SharedData.isSplashed()) {
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            SharedData.setIsSplashed(true);
        }
    }

    private void setUpShakeToSos() {
        SharedData.clearContext();
        SharedData.pushContext(this);
        ShakeOptions options = new ShakeOptions()
                .background(true)
                .interval(1000)
                .shakeCount(3)
                .sensibility(2.0f);

        mShakeDetector = new ShakeDetector(options).start(this);
    }

    private void showTutorial(View view) {
        if (SharedData.isShowTutorial3()) {
            SpotlightSequence.getInstance(this,null)
                    .addSpotlight(view,
                            "SOS", "Shake your phone 3 times to trigger an alert and find the nearest safe house", SharedData.getRandomStr())
                    .startSequence();
        }
    }

    @Override
    protected void onResume() {
        if (mShakeDetector != null && !mShakeDetector.isRunning()) {
            mShakeDetector.start(this);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mShakeDetector != null) {
            mShakeDetector.stopShakeDetector(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mShakeDetector != null) {
            mShakeDetector.destroy(getBaseContext());
        }
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment nextFragment = null;
        FragmentManager fragmentManager = getFragmentManager();

        switch (id) {
//            case R.id.nav_map:
//                if (mMapFragment == null) {
//                    mMapFragment = new MapsFragment();
//                }
//                nextFragment = mMapFragment;
//                break;
            case R.id.nav_group:
                Intent intentGroup = new Intent(this, GroupsActivity.class);
                startActivityForResult(intentGroup, GROUP_ACTIVITY);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            case R.id.nav_zones:
                Intent intent = new Intent(this, ZonesActivity.class);
                startActivityForResult(intent, ZONE_ACTIVITY);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            case R.id.nav_routes:
                Intent intentRoute = new Intent(this, RoutesActivity.class);
//                startActivityForResult(intentRoute, ZONE_ACTIVITY);
                startActivity(intentRoute);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            case R.id.nav_logout:
                signOut();
                return true;
            default:
                break;
        }

        fragmentManager.beginTransaction().replace(R.id.content_frame, nextFragment).commit();
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        Intent intent = new Intent(this, LocationService.class);
        Intent intent2 = new Intent(this, RouteService.class);
        stopService(intent);
        stopService(intent2);
        mDb = FirebaseDatabase.getInstance();
        String id = mAuth.getCurrentUser().getUid();
        SharedPreferences sp = getSharedPreferences("ID", Context.MODE_PRIVATE);
        List<String> gids = SharedData.getmGroupId();
        if (gids != null) {
            for (String gid: gids) {
                DatabaseReference refToken = mDb.getReference("group")
                        .child(gid)
                        .child("users")
                        .child(id);
                refToken.setValue("Offline");
            }
        }

        SharedPreferences.Editor eLogin= sp.edit();
        eLogin.clear();
        eLogin.commit();
        SharedData.clear();
        mAuth.signOut();
        drawer.closeDrawer(GravityCompat.START);
        mShakeDetector.stopShakeDetector(this);
    }

    /**
     * get result of other activities
     * @param requestCode   request code
     * @param resultCode    result code
     * @param data  data sent back
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String name = "";
        //if the result is ok
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                mMapFragment = new MapsFragment();
                Fragment home = mMapFragment;
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, home).commit();
                //close the drawer
                //set the user information
                String email = data.getStringExtra("email");
                String fname = data.getStringExtra("firstName");
                String lname = data.getStringExtra("lastName");
                SharedPreferences sp = getSharedPreferences("ID", Context.MODE_PRIVATE);
                SharedPreferences.Editor eLogin= sp.edit();
                name = fname + " " +lname;
                eLogin.putString("name", name);
                eLogin.putString("email", email);
                eLogin.apply();
                setUserInfo(email, name);
                //display the welcome Toast
                Toast.makeText(this,"Welcome " + name, Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == ZONE_ACTIVITY
                && resultCode == RESULT_OK
                && mMapFragment != null) {
            mMapFragment.onActivityResult(requestCode,resultCode,data);
        }

        if (requestCode == GROUP_ACTIVITY
                && resultCode == RESULT_OK
                && mMapFragment != null) {
            mMapFragment.onActivityResult(requestCode,resultCode,data);
        }
    }

    /**
     * set the user information to the views
     * @param email email address
     * @param name  user name
     */
    private void setUserInfo(String email, String name) {
        emailTextView.setText(email);
        usernameTextView.setText(name);
    }
}
