package com.example.yinqinghao.childprotect;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yinqinghao.childprotect.entity.LocationData;
import com.example.yinqinghao.childprotect.fragment.MapsFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout drawer;
    private TextView usernameTextView;
    private TextView emailTextView;
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
        mAuth = FirebaseAuth.getInstance();
//        data();
    }

//    private void data() {
//        FirebaseDatabase db = FirebaseDatabase.getInstance();
//        Date now = new Date();
//        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
//        String date = format.format(now);
//        Date temp = null;
//        try {
//            temp = format.parse(date);
//        } catch (Exception ex) {
//
//        }
//        long a = temp.getTime();
//        LatLng monash = new LatLng(-37.876823,145.045837);
//        LocationData l = new LocationData(now,monash,100);
//        db.getReference("location/DK8Nl733lYNRd1MWftTfTI8SwPD2/"+ a).setValue(l);
//        LocationData c = l;
//        c.setBatteryStatus(99);
//        db.getReference("location/DK8Nl733lYNRd1MWftTfTI8SwPD2/"+ (a +1)).setValue(c);
//        db.getReference("location/DK8Nl733lYNRd1MWftTfTI8SwPD2/"+ (a - 1)).setValue(l);
//        db.getReference("location/DK8Nl733lYNRd1MWftTfTI8SwPD2").orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()){
//                    Map<String,String> a = (HashMap<String,String>)dataSnapshot.getValue();
////                    LocationData locationData = dataSnapshot.getValue(LocationData.class);
////                    Log.d("MainActivity",dataSnapshot.toString());
//                    Toast.makeText(MainActivity.this, dataSnapshot.toString(), Toast.LENGTH_LONG).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }


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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

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
            case R.id.nav_logout:
                mAuth.signOut();
                return true;
            default:
                break;
        }

        fragmentManager.beginTransaction().replace(R.id.content_frame, nextFragment).commit();
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
                if (mMapFragment == null)
                    mMapFragment = new MapsFragment();
                Fragment home = mMapFragment;
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, home).commit();
                //close the drawer
                //set the user information
                String email = data.getStringExtra("email");
                String fname = data.getStringExtra("firstName");
                String lname = data.getStringExtra("lastName");
                name = fname + " " + lname;
                setUserInfo(email, name);
                //display the welcome Toast
                Toast.makeText(this,"Welcome " + name, Toast.LENGTH_SHORT).show();
            }
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
