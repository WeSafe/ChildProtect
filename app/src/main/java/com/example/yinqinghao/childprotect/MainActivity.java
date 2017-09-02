package com.example.yinqinghao.childprotect;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brouding.simpledialog.SimpleDialog;
import com.example.yinqinghao.childprotect.entity.Route;
import com.example.yinqinghao.childprotect.entity.SharedData;
import com.example.yinqinghao.childprotect.fragment.MapsFragment;
import com.github.tbouron.shakedetector.library.ShakeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wooplr.spotlight.utils.SpotlightSequence;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    private static final int ZONE_ACTIVITY = 876;

    private DrawerLayout drawer;
    private TextView usernameTextView;
    private TextView emailTextView;
    private ImageView sosImageView;
    private MenuItem accountNavItem;

    private Fragment mMapFragment;
    private FirebaseAuth mAuth;
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
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivityForResult(intent,1);
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

    private void setUpShakeToSos() {
        ShakeDetector.create(this, new ShakeDetector.OnShakeListener() {
            @Override
            public void OnShake() {
                popupStart();
                ShakeDetector.stop();
            }
        });
        ShakeDetector.updateConfiguration(8, 3);
    }

    private void popupStart() {
        new SimpleDialog.Builder(this)
                .setTitle("Do you want to send SOS message", true)
                .onConfirm(new SimpleDialog.BtnCallback() {
                    @Override
                    public void onClick(@NonNull SimpleDialog dialog, @NonNull SimpleDialog.BtnAction which) {
                        Intent intent = new Intent(MainActivity.this, SOSActivity.class);
                        startActivity(intent);
                        ShakeDetector.start();
                    }
                })
                .setBtnConfirmText("Yes")
                .setBtnConfirmTextColor("#e6b115")
                .setBtnCancelText("No")
                .onCancel(new SimpleDialog.BtnCallback() {
                    @Override
                    public void onClick(@NonNull SimpleDialog dialog, @NonNull SimpleDialog.BtnAction which) {
                        ShakeDetector.start();
                    }
                })
                .show();
    }

    private void showTutorial(View view) {
        if (SharedData.isShowTutorial3()) {
            SpotlightSequence.getInstance(this,null)
                    .addSpotlight(view,
                            "SOS", "Shake your phone 3 times or click it to get help", SharedData.getRandomStr())
                    .startSequence();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ShakeDetector.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ShakeDetector.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShakeDetector.destroy();
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
            case R.id.nav_map:
                if (mMapFragment == null) {
                    mMapFragment = new MapsFragment();
                }
                nextFragment = mMapFragment;
                break;
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
        SharedPreferences sp = getSharedPreferences("ID", Context.MODE_PRIVATE);
        SharedPreferences.Editor eLogin= sp.edit();
        eLogin.clear();
        eLogin.commit();
        SharedData.clear();
        mAuth.signOut();
        drawer.closeDrawer(GravityCompat.START);
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
