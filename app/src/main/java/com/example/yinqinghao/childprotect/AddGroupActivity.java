package com.example.yinqinghao.childprotect;

import android.*;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.brouding.simpledialog.SimpleDialog;
import com.example.yinqinghao.childprotect.QRHelper.QRHelper;
import com.example.yinqinghao.childprotect.adapter.GridAdapter;
import com.example.yinqinghao.childprotect.entity.Group;
import com.example.yinqinghao.childprotect.entity.Person;
import com.example.yinqinghao.childprotect.entity.SafeHouse;
import com.example.yinqinghao.childprotect.entity.SharedData;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AddGroupActivity extends AppCompatActivity {
    private GridView mGroupMemberGrid;
    private SimpleDialog mLoadingDialog;

    private FirebaseDatabase mDb;
    private FirebaseAuth mAuth;

    private List<Person> mPeople;
    private Group mGroup;

    private final int RESULT_QUIT = 777;
    private final int GALLERY_REQUEST = 111;
    private final int SCAN_REQUEST = 222;
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 145;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 451;

    private AdapterView.OnItemLongClickListener mGridLongClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        SharedData.pushContext(this);
        Intent intent = getIntent();
        mGroup = intent.getParcelableExtra("group");
        setTitle(mGroup.getName());

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance();

        mGroupMemberGrid = (GridView) findViewById(R.id.gv_group_member);


        setGridListener();
        getData();
    }

    private void setGridListener() {
        mGridLongClickListener = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Person member = mPeople.get(position);
                final String mid = member.getUid();
                if (!mAuth.getCurrentUser().getUid().equals(mGroup.getUserID()))
                    return true;
                if (mid.equals(mAuth.getCurrentUser().getUid()))
                    return true;

                new SimpleDialog.Builder(AddGroupActivity.this)
                        .setTitle("You want to remove this person?", true)
                        .onConfirm(new SimpleDialog.BtnCallback() {
                            @Override
                            public void onClick(@NonNull SimpleDialog dialog, @NonNull SimpleDialog.BtnAction which) {
                                showProgress();
                                final String gid = mGroup.getId();
                                DatabaseReference refGroupUser = mDb.getReference("group")
                                        .child(gid)
                                        .child("users")
                                        .child(mid);
                                refGroupUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            final String token = dataSnapshot.getValue().toString();
                                            DatabaseReference refGroupUser2 = mDb.getReference("group")
                                                    .child(gid)
                                                    .child("users")
                                                    .child(mid);
                                            refGroupUser2.removeValue();
//                                            mPeople = new ArrayList<>();
                                            DatabaseReference refUser = mDb.getReference("user")
                                                    .child(mid)
                                                    .child(gid);
                                            refUser.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        DatabaseReference refNotification = mDb.getReference("notification")
                                                                .child(token);

                                                        String msg = "refresh:" + gid + ":" + mGroup.getName() + ":remove";
                                                        refNotification.push().child(msg).setValue(true);
                                                        if (mLoadingDialog != null) {
                                                            mLoadingDialog.dismiss();
                                                        }
//                                                        mPeople.remove(member);
//                                                        showData();
                                                    } else {
                                                        Toast.makeText(AddGroupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

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
        showProgress();
        String gid = mGroup.getId();
        DatabaseReference refGroup = mDb.getReference("group")
                .child(gid)
                .child("users");
        refGroup.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mPeople = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                        String mid = dataSnapshot1.getKey();
                        final long count = dataSnapshot.getChildrenCount();
                        DatabaseReference refMember = mDb.getReference("userInfo")
                                .child(mid);
                        refMember.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Person member = dataSnapshot.getValue(Person.class);
                                    mPeople.add(member);
                                    if (mPeople.size() == count) {
                                        showData();
                                        if (mLoadingDialog != null) {
                                            mLoadingDialog.dismiss();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    mGroupMemberGrid.setOnItemLongClickListener(mGridLongClickListener);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showData() {
        GridAdapter adapter = new GridAdapter(true, AddGroupActivity.this, mPeople, mGroup.getUserID());
        mGroupMemberGrid.setAdapter(adapter);
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.grid_anim);
        GridLayoutAnimationController controller = new GridLayoutAnimationController(animation, .2f, .2f);
        mGroupMemberGrid.setLayoutAnimation(controller);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_member_setting, menu);
        MenuItem rename = menu.findItem(R.id.rename);
        if (!mAuth.getCurrentUser().getUid().equals(mGroup.getUserID())) {
            rename.setVisible(false);
        }
        return true;
    }

    private void quit() {
        new SimpleDialog.Builder(AddGroupActivity.this)
                .setTitle("You want to quit this group?", true)
                .onConfirm(new SimpleDialog.BtnCallback() {
                    @Override
                    public void onClick(@NonNull SimpleDialog dialog, @NonNull SimpleDialog.BtnAction which) {
                        showProgress();
                        final String gid = mGroup.getId();
                        final String myId = mAuth.getCurrentUser().getUid();
                        final DatabaseReference refGroup = mDb.getReference("group")
                                .child(gid);
                        DatabaseReference refGroupUser = refGroup
                                .child("users");
                        DatabaseReference refGroupMe = refGroupUser
                                .child(myId);
                        DatabaseReference refG = mDb.getReference("user")
                                .child(myId)
                                .child(gid);
                        refGroupMe.removeValue();
                        refG.removeValue();
                        refGroupUser.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String id = "";
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        id = ds.getKey();
                                        break;
                                    }
                                    if (id.length() != 0) {
                                        refGroup.child("userID").setValue(id);
                                    }
                                } else {
                                    refGroup.removeValue();
                                    DatabaseReference refZones = mDb.getReference("zone")
                                            .child(gid);
                                    DatabaseReference refRoute = mDb.getReference("route")
                                            .child(gid);
                                    refZones.removeValue();
                                    refRoute.removeValue();

                                }
                                if (mLoadingDialog != null) {
                                    mLoadingDialog.dismiss();
                                }
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("group", mGroup);
                                setResult(RESULT_QUIT, returnIntent);
                                finish();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                })
                .setBtnConfirmText("OK")
                .setBtnConfirmTextColor("#e6b115")
                .setBtnCancelText("Cancel")
                .show();
    }

    private void rename() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(" ");
        // I'm using fragment here so I'm using getView() to provide ViewGroup
        // but you can provide here any other instance of ViewGroup from your Fragment / Activity
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.rename, (ViewGroup) findViewById(android.R.id.content), false);
        // Set up the input
        final EditText input = (EditText) viewInflated.findViewById(R.id.input_title);
        input.setError(null);
        input.setText(mGroup.getName());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog ad = builder.create();
        ad.show();
        ad.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                showProgress();
                String groupName = input.getText().toString().trim();
                if (groupName.length() == 0) {
                    input.setError("Please input a name.");
                    if (mLoadingDialog != null) {
                        mLoadingDialog.dismiss();
                    }
                    return;
                }
                if (groupName.length() > 20) {
                    input.setError("The length of group name should be within 20");
                    if (mLoadingDialog != null) {
                        mLoadingDialog.dismiss();
                    }
                    return;
                }

                mGroup.setName(groupName);
                final String gid = mGroup.getId();
                AddGroupActivity.this.setTitle(groupName);
                DatabaseReference refGroupName = mDb.getReference("group")
                        .child(gid)
                        .child("name");

                refGroupName.setValue(groupName).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        if (task.isSuccessful()) {
                            DatabaseReference refGroupUser = mDb.getReference("group")
                                    .child(gid)
                                    .child("users");
                            refGroupUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                                            String id = mAuth.getCurrentUser().getUid();
                                            if (!id.equals(ds.getKey())) {
                                                String token = ds.getValue().toString();
                                                DatabaseReference refNotification = mDb.getReference("notification")
                                                        .child(token);

                                                String msg = "refresh:" + gid + ":" + mGroup.getName() + ":rename";
                                                refNotification.push().child(msg).setValue(true);
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            if (mLoadingDialog != null) {
                                mLoadingDialog.dismiss();
                            }
                        } else {
                            Toast.makeText(AddGroupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        if (mLoadingDialog != null) {
                            mLoadingDialog.dismiss();
                        }
                    }
                });
                ad.dismiss();
            }
        });

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
                    returnIntent.putExtra("group", mGroup);
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
            case R.id.rename:
                rename();
                return true;
            case R.id.quit:
                quit();
                return true;
            case R.id.scan:
                scan();
                return true;
            case R.id.read:
                read();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void scan() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            } else {
                scanQRCode();
            }
        } else {
            scanQRCode();
        }
    }

    private void read() {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                readImageFromGallery();
            }
        } else {
            readImageFromGallery();
        }
    }

    private void readImageFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }

    private void scanQRCode() {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra("gName", mGroup.getName());
        startActivityForResult(intent, SCAN_REQUEST);
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
        if (requestCode == GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    QRHelper qrHelper = new QRHelper();
                    String result = qrHelper.readQRCode(selectedImage);
                    String [] r = result.split(",");
                    String mid = r[0];
                    String token = r[1];
                    addNewMember(mid, token);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }

        if (requestCode == SCAN_REQUEST) {
            if (resultCode == RESULT_OK) {
                try {
                    String result = data.getStringExtra("result");
                    String [] r = result.split(",");
                    String mid = r[0];
                    String token = r[1];
                    addNewMember(mid, token);
                } catch (Exception ex) {
                    Toast.makeText(this, "Please Scan again.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void addNewMember(String mid, final String token) {
        final String gid = mGroup.getId();
        DatabaseReference refGroup = mDb.getReference("group")
                .child(gid);
        mGroup.getUsers().put(mid, token);
        refGroup.setValue(mGroup);
//        mPeople = new ArrayList<>();

        DatabaseReference refm = mDb.getReference("user")
                .child(mid)
                .child(gid);

        refm.setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                if (task.isSuccessful()) {
                    DatabaseReference refNotification = mDb.getReference("notification")
                            .child(token);

                    String msg = "refresh:" + gid + ":" + mGroup.getName() + ":join";
                    refNotification.push().child(msg).setValue(true);
                    if (mLoadingDialog != null) {
                        mLoadingDialog.dismiss();
                    }
                } else {
                    Toast.makeText(AddGroupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    @Override
    protected void onDestroy() {
        SharedData.popContext();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                readImageFromGallery();
            } else {
                // User refused to grant permission.
                Toast.makeText(this, "Can't get the permission.", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                scanQRCode();
            } else {
                // User refused to grant permission.
                Toast.makeText(this, "Can't get the permission.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
