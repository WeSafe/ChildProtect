package com.example.yinqinghao.childprotect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.brouding.simpledialog.SimpleDialog;
import com.example.yinqinghao.childprotect.adapter.GridAdapter;
import com.example.yinqinghao.childprotect.entity.Route;
import com.example.yinqinghao.childprotect.entity.Zone;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RoutesActivity extends AppCompatActivity {
    private GridView mRoutesGrid;
    private FloatingActionButton mAddBtn;
    private Intent mIntent;
    private static final int ADD_NEW_Route = 4321;
    private static final int Edit_Route = 1234;

    private FirebaseDatabase mDb;

    private String mCurrentGid;
    private List<Route> mRoutes;
    private AdapterView.OnItemClickListener mGridClickListener;
    private AdapterView.OnItemLongClickListener mGridLongClickListener;
    private SimpleDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        SharedPreferences sp = getSharedPreferences("ID", Context.MODE_PRIVATE);
        mCurrentGid = sp.getString("currentGid",null);
        mDb = FirebaseDatabase.getInstance();

        mRoutesGrid = (GridView) findViewById(R.id.gridview_route);
        mAddBtn = (FloatingActionButton) findViewById(R.id.fab_add_route);
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoutesActivity.this, AddRouteActivity.class);
                startActivityForResult(intent, ADD_NEW_Route);
            }
        });
        setGridListener();
        getData();
    }

    private void setGridListener() {
        mGridClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Route route = mRoutes.get(position);
                Intent intent = new Intent(RoutesActivity.this, AddRouteActivity.class);
                intent.putExtra("route", route);
                startActivityForResult(intent, Edit_Route);
            }
        };


        mGridLongClickListener = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Route route = mRoutes.get(position);
                new SimpleDialog.Builder(RoutesActivity.this)
                        .setTitle("You want to delete this route?", true)
                        .onConfirm(new SimpleDialog.BtnCallback() {
                            @Override
                            public void onClick(@NonNull SimpleDialog dialog, @NonNull SimpleDialog.BtnAction which) {
                                showProgress();
                                DatabaseReference refRoute = mDb.getReference("route")
                                        .child(mCurrentGid)
                                        .child(route.getId());
                                refRoute.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mLoadingDialog.dismiss();
                                            mRoutes.remove(route);
                                            showData();
                                        }
                                    }
                                });
                            }
                        })
                        .setBtnConfirmText("OK")
                        .setBtnConfirmTextColor("#e6b115")
                        .setBtnCancelText("Cancel")
                        .show();
                return true;
            }
        };
    }

    private void getData(){
        mRoutes = new ArrayList<>();
        DatabaseReference refZone = mDb.getReference("route")
                .child(mCurrentGid);
        refZone.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                        Route route = dataSnapshot1.getValue(Route.class);
                        mRoutes.add(route);
                    }
                    showData();
                    mRoutesGrid.setOnItemClickListener(mGridClickListener);
                    mRoutesGrid.setOnItemLongClickListener(mGridLongClickListener);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showData() {
        GridAdapter adapter = new GridAdapter(RoutesActivity.this, mRoutes, true);
        mRoutesGrid.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NEW_Route) {
            if (resultCode == RESULT_OK) {
                Route route = data.getParcelableExtra("route");
                mRoutes.add(route);
                showData();
            }
        }

        if (requestCode == Edit_Route) {
            if (resultCode == RESULT_OK) {
                getData();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent parentIntent = NavUtils.getParentActivityIntent(this);
                if(parentIntent != null) {
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showProgress() {
        mLoadingDialog = new SimpleDialog.Builder(this)
                .setContent("Loading",100)
                .setProgressGIF(R.raw.simple_dialog_progress_default)
                .setBtnCancelText("Cancel")
                .setBtnCancelTextColor("#2861b0")
                .setBtnCancelShowTime(30000)
                .show();
    }
}
