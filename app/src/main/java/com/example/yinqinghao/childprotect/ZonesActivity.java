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
import com.example.yinqinghao.childprotect.entity.Zone;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZonesActivity extends AppCompatActivity {
    private GridView mZonesGrid;
    private FloatingActionButton mAddBtn;
    private Intent mIntent;
    private static final int ADD_NEW_ZONE = 432;
    private static final int Edit_ZONE = 234;

    private FirebaseDatabase mDb;

    private String mFamilyId;
    private List<Zone> mZones;
    private AdapterView.OnItemClickListener mGridClickListener;
    private AdapterView.OnItemLongClickListener mGridLongClickListener;
    private SimpleDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zones);

        SharedPreferences sp = getSharedPreferences("ID", Context.MODE_PRIVATE);
        mFamilyId = sp.getString("familyId",null);
        mDb = FirebaseDatabase.getInstance();

        mZonesGrid = (GridView) findViewById(R.id.gridview);
        mAddBtn = (FloatingActionButton) findViewById(R.id.fab_add_zone);
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ZonesActivity.this, AddZoneActivity.class);
                startActivityForResult(intent, ADD_NEW_ZONE);
            }
        });
        setGridListener();
        getData();
    }

    private void setGridListener() {
        mGridClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Zone zone = mZones.get(position);
                Intent intent = new Intent(ZonesActivity.this, AddZoneActivity.class);
                intent.putExtra("zone", zone);
                startActivityForResult(intent, Edit_ZONE);
            }
        };


        mGridLongClickListener = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Zone zone = mZones.get(position);
                new SimpleDialog.Builder(ZonesActivity.this)
                        .setTitle("You want to delete this zone?", true)
                        .onConfirm(new SimpleDialog.BtnCallback() {
                            @Override
                            public void onClick(@NonNull SimpleDialog dialog, @NonNull SimpleDialog.BtnAction which) {
                                showProgress();
                                DatabaseReference refZone = mDb.getReference("zone")
                                        .child(mFamilyId)
                                        .child(zone.getId());
                                refZone.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mLoadingDialog.dismiss();
                                            mZones.remove(zone);
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

    private void showProgress() {
        mLoadingDialog = new SimpleDialog.Builder(this)
                .setContent("Loading",100)
                .setProgressGIF(R.raw.simple_dialog_progress_default)
                .setBtnCancelText("Cancel")
                .setBtnCancelTextColor("#2861b0")
                .setBtnCancelShowTime(30000)
                .show();
    }

    private void getData(){
        mZones = new ArrayList<>();
        DatabaseReference refZone = mDb.getReference("zone")
                .child(mFamilyId);
        refZone.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                        Zone zone = dataSnapshot1.getValue(Zone.class);
                        mZones.add(zone);
                    }
                    showData();
                    mZonesGrid.setOnItemClickListener(mGridClickListener);
                    mZonesGrid.setOnItemLongClickListener(mGridLongClickListener);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NEW_ZONE) {
            if (resultCode == RESULT_OK) {
                Zone zone = data.getParcelableExtra("zone");
                mZones.add(zone);
                showData();
            }
        }

        if (requestCode == Edit_ZONE) {
            if (resultCode == RESULT_OK) {
                getData();
            }
        }
    }

    private void showData() {
        GridAdapter adapter = new GridAdapter(ZonesActivity.this, mZones);
        mZonesGrid.setAdapter(adapter);
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
                    returnIntent.putParcelableArrayListExtra("zones", (ArrayList<? extends Parcelable>) mZones);
                    setResult(RESULT_OK, returnIntent);
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
}
