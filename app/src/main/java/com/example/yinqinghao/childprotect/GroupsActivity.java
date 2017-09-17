package com.example.yinqinghao.childprotect;

import android.*;
import android.Manifest;
import android.content.Context;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.brouding.simpledialog.SimpleDialog;
import com.example.yinqinghao.childprotect.QRHelper.QRHelper;
import com.example.yinqinghao.childprotect.adapter.GridAdapter;
import com.example.yinqinghao.childprotect.entity.Group;
import com.example.yinqinghao.childprotect.entity.SharedData;
import com.example.yinqinghao.childprotect.entity.Zone;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupsActivity extends AppCompatActivity {

    private GridView mGroupGrid;
    private FloatingActionMenu mMenuAdd;
    private final int ADD_NEW_GROUP = 678;
    private final int Edit_GROUP = 876;
    private final int RESULT_QUIT = 777;
    private final int RESULT_QRCODE = 10101;
    private final int GALLERY_REQUEST = 333;
    private final int SCAN_REQUEST =6667;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 451;
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 145;
    private int count = 0;

    private FirebaseDatabase mDb;
    private FirebaseAuth mAuth;

    private List<Group> mGroups;
    private AdapterView.OnItemClickListener mGridClickListener;
    private AdapterView.OnItemLongClickListener mGridLongClickListener;
    private SimpleDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        setTitle("My Groups");

        SharedData.pushContext(this);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance();

        mGroupGrid = (GridView) findViewById(R.id.gv_group);
        mMenuAdd = (FloatingActionMenu) findViewById(R.id.menu_create_group);
        setGridListener();
        getData();
        setmMenuAdd();
    }

    private void setmMenuAdd() {
        final FloatingActionButton scanFab = new FloatingActionButton(this);
        scanFab.setButtonSize(FloatingActionButton.SIZE_MINI);
        scanFab.setLabelText("Scan QR Code");
        scanFab.setImageResource(R.drawable.ic_filter_center_focus_24dp);
        mMenuAdd.addMenuButton(scanFab);
        scanFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    if (checkSelfPermission(Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    } else {
                        scanQRCode();
                    }
                } else {
                    scanQRCode();
                }
            }
        });

        FloatingActionButton readFab = new FloatingActionButton(this);
        readFab.setButtonSize(FloatingActionButton.SIZE_MINI);
        readFab.setLabelText("Read QR Code From Image");
        readFab.setImageResource(R.drawable.ic_image_24dp);
        mMenuAdd.addMenuButton(readFab);
        readFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
    }
    private void readImageFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
        mMenuAdd.close(false);
    }

    private void scanQRCode() {
        Intent intent = new Intent(GroupsActivity.this, ScanActivity.class);
        startActivityForResult(intent, SCAN_REQUEST);
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

    private void setGridListener() {
        mGridClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Group group = mGroups.get(position);
                Intent intent = new Intent(GroupsActivity.this, AddGroupActivity.class);
                intent.putExtra("group", group);
                startActivityForResult(intent, Edit_GROUP);
            }
        };
    }


    private void getData(){
        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference refGroup = mDb.getReference("user")
                .child(uid);
        refGroup.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    count = 0;
                    for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                        String gid = dataSnapshot1.getKey();
                        DatabaseReference refG = mDb.getReference("group")
                                .child(gid);
                        refG.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (count == 0) {
                                        mGroups = new ArrayList<>();
                                    }
                                    Group group = dataSnapshot.getValue(Group.class);
                                    mGroups.add(group);
                                    showData();
                                    count ++;
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    mGroupGrid.setOnItemClickListener(mGridClickListener);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showData() {
        GridAdapter adapter = new GridAdapter(GroupsActivity.this, true, mGroups);
        mGroupGrid.setAdapter(adapter);
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
        if (requestCode == Edit_GROUP) {
            if (resultCode == RESULT_QUIT) {
                Group g = data.getParcelableExtra("group");
                for (Group g1: mGroups) {
                    if (g.getId().equals(g1.getId())) {
                        mGroups.remove(g1);
                        break;
                    }
                }
                if (mGroups.size() == 0) {
                    SharedPreferences sp = getSharedPreferences("ID", Context.MODE_PRIVATE);
                    DatabaseReference refGroup = mDb.getReference("group");
                    String newGid = refGroup.push().getKey();
                    String token = FirebaseInstanceId.getInstance().getToken();
                    Map<String, String> users = new HashMap<>();
                    String uid = mAuth.getCurrentUser().getUid();
                    users.put(uid, token);
                    String fname = sp.getString("fName","");
                    Group group = new Group(newGid, fname + "'s Group",uid, users);
                    refGroup.child(newGid).setValue(group);

                    DatabaseReference refU = mDb.getReference("user")
                            .child(uid).child(newGid);
                    refU.setValue(true);
                    mGroups.add(group);
                }
                showData();
            }

            if (resultCode == RESULT_OK) {
                Group g = data.getParcelableExtra("group");
                for (Group g1: mGroups) {
                    if (g.getId().equals(g1.getId())) {
                        g1.setName(g.getName());
                        break;
                    }
                }
                showData();
            }
        }

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
                    createNewGroup(mid, token);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }

        if (requestCode == SCAN_REQUEST) {
            if (resultCode == RESULT_OK) {
                String result = data.getStringExtra("result");
                String [] r = result.split(",");
                String mid = r[0];
                String token = r[1];
                createNewGroup(mid, token);
            }
        }
    }

    private void createNewGroup(String mid, final String token) {
        DatabaseReference refGroup = mDb.getReference("group");
        final String gid = refGroup.push().getKey();
        String uid = mAuth.getCurrentUser().getUid();
        String uToken = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences sp = getSharedPreferences("ID", Context.MODE_PRIVATE);
        String fname = sp.getString("fName","");
        Map<String, String> map = new HashMap<>();
        map.put(uid,uToken);
        map.put(mid, token);
        final Group group = new Group(gid,fname +"'s Group", uid, map);
        refGroup.child(gid).setValue(group);

        DatabaseReference refu = mDb.getReference("user")
                .child(uid)
                .child(gid);
        DatabaseReference refm = mDb.getReference("user")
                .child(mid)
                .child(gid);

        refu.setValue(true);
        refm.setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    DatabaseReference refNotification = mDb.getReference("notification")
                            .child(token);

                    String msg = "refresh:" + gid + ":" + group.getName() + ":join";
                    refNotification.push().child(msg).setValue(true);
                    mLoadingDialog.dismiss();
                } else {
                    Toast.makeText(GroupsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        Intent intent = new Intent(this, AddGroupActivity.class);
        intent.putExtra("group", group);
        mGroups.add(group);
        startActivityForResult(intent, Edit_GROUP);
    }

    /**
     * options item selected
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
                    returnIntent.putParcelableArrayListExtra("groups", (ArrayList<? extends Parcelable>) mGroups);
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
            case R.id.action_qr:
                Intent intent = new Intent(this, QRActivity.class);
                intent.putExtra("uid", mAuth.getCurrentUser().getUid());
                intent.putExtra("token", FirebaseInstanceId.getInstance().getToken());
                startActivityForResult(intent,RESULT_QRCODE);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.qr_menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        mMenuAdd.close(false);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        SharedData.popContext();
        super.onDestroy();
    }
}
