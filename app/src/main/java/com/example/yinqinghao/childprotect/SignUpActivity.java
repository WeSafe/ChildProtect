package com.example.yinqinghao.childprotect;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.example.yinqinghao.childprotect.entity.Group;
import com.example.yinqinghao.childprotect.entity.Person;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = SignUpActivity.class.getName();
    //UI reference
    private EditText mEmailEditText;
    private EditText mPwdEditText;
    private EditText mRePwdEditText;
    private EditText mFirstNameEditText;
    private EditText mLastNameEditText;
    private EditText mPhoneEditText;

    private ActionProcessButton mSignIn;
    private View mRegisterForm;
    private View mProgress;
    //firebase components
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDb;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private View.OnFocusChangeListener mFocusChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        setTitle("Sign Up");
        initFirebase();
        mDb = FirebaseDatabase.getInstance();

        mEmailEditText = (EditText) findViewById(R.id.edit_register_email);
        mPwdEditText = (EditText) findViewById(R.id.edit_register_pwd);
        mRePwdEditText = (EditText) findViewById(R.id.edit_register_repwd);
        mFirstNameEditText = (EditText) findViewById(R.id.edit_register_fname);
        mLastNameEditText = (EditText) findViewById(R.id.edit_register_lname);
        mPhoneEditText = (EditText) findViewById(R.id.edit_register_phone);

        mSignIn = (ActionProcessButton) findViewById(R.id.btn_sign_up);
        mRegisterForm = findViewById(R.id.register_form);
        mProgress = findViewById(R.id.register_progress);

        mFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        };
        mEmailEditText.setOnFocusChangeListener(mFocusChangeListener);
        mPwdEditText.setOnFocusChangeListener(mFocusChangeListener);
        mRePwdEditText.setOnFocusChangeListener(mFocusChangeListener);
        mFirstNameEditText.setOnFocusChangeListener(mFocusChangeListener);
        mLastNameEditText.setOnFocusChangeListener(mFocusChangeListener);
        mPhoneEditText.setOnFocusChangeListener(mFocusChangeListener);

        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSignUp();
            }
        });
    }

    /**
     * Initialise the firebase auth
     */
    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
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

    /**
     * Reset the errors
     */
    private void resetErrors() {
        // Reset errors.
        mEmailEditText.setError(null);
        mPwdEditText.setError(null);
        mRePwdEditText.setError(null);
        mFirstNameEditText.setError(null);
        mLastNameEditText.setError(null);
        mPhoneEditText.setError(null);
    }

    /**
     * When the user attempt sign up
     */
    private void attemptSignUp() {
        resetErrors();
        //get data from the views
        String email = mEmailEditText.getText().toString();
        String pwd = mPwdEditText.getText().toString();
        String rePwd = mRePwdEditText.getText().toString();
        String fname = mFirstNameEditText.getText().toString();
        String lname = mLastNameEditText.getText().toString();
        String phone = mPhoneEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        //validations
        if (TextUtils.isEmpty(phone)) {
            mPhoneEditText.setError(getString(R.string.error_field_required));
            focusView = mPhoneEditText;
            cancel = true;
        } else if (phone.length() < 10) {
            mPhoneEditText.setError("The length of code should be more than 10.");
            focusView = mPhoneEditText;
            cancel = true;
        }

        if (TextUtils.isEmpty(lname)) {
            mLastNameEditText.setError(getString(R.string.error_field_required));
            focusView = mLastNameEditText;
            cancel = true;
        }

        if (TextUtils.isEmpty(fname)) {
            mFirstNameEditText.setError(getString(R.string.error_field_required));
            focusView = mFirstNameEditText;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(rePwd)) {
            mRePwdEditText.setError(getString(R.string.error_field_required));
            focusView = mRePwdEditText;
            cancel = true;
        } else if (!rePwd.equals(pwd)) {
            mRePwdEditText.setError("Passwords are not the same.");
            focusView = mRePwdEditText;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(pwd)) {
            mPwdEditText.setError(getString(R.string.error_field_required));
            focusView = mPwdEditText;
            cancel = true;
        } else if (pwd.length() < 6) {
            mPwdEditText.setError(getString(R.string.error_invalid_password));
            focusView = mPwdEditText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailEditText.setError(getString(R.string.error_field_required));
            focusView = mEmailEditText;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailEditText.setError(getString(R.string.error_invalid_email));
            focusView = mEmailEditText;
            cancel = true;
        } else if (email.length() > 255) {
            mEmailEditText.setError("The length of input should be less than 255 characters");
            focusView = mEmailEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            createNewUser(email, pwd);
        }
    }

    /**
     * Create a new user
     * @param email     email address
     * @param password      password
     */
    private void createNewUser(String email, final String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            showProgress(false);
                        } else {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Name, email address, and profile photo Url
                                final String email = user.getEmail();

                                // The user's ID, unique to the Firebase project. Do NOT use this value to
                                // authenticate with your backend server, if you have one. Use
                                // FirebaseUser.getToken() instead.
                                final String uid = user.getUid();
                                // Get data from views
                                String fname = mFirstNameEditText.getText().toString();
                                String lname = mLastNameEditText.getText().toString();
                                String phone = mPhoneEditText.getText().toString();
                                // Create a new user object
                                Person person = new Person(uid, email, fname, lname, Long.valueOf(phone));
                                DatabaseReference refUser = mDb.getReference("userInfo");
                                // Save it into firebase
                                refUser.child(uid).setValue(person);
                                //Create a new Group
                                DatabaseReference refGroup = mDb.getReference("group");
                                String gid = refGroup.push().getKey();
                                String token = FirebaseInstanceId.getInstance().getToken();
                                Map<String, String> users = new HashMap<>();
                                users.put(uid, token);
                                Group group = new Group(gid, fname + "'s Group",uid, users);
                                refGroup.child(gid).setValue(group);

                                DatabaseReference refU = mDb.getReference("user")
                                        .child(uid).child(gid);
                                refU.setValue(true);

                                List<String> groupIds = new ArrayList<>();
                                groupIds.add(gid);
                                String groupIdsStr = new Gson().toJson(groupIds);
                                SharedPreferences sp = getSharedPreferences("ID", Context.MODE_PRIVATE);
                                SharedPreferences.Editor eLogin= sp.edit();
                                eLogin.putString("groupIds", groupIdsStr);
                                eLogin.putString("uid", uid);
                                eLogin.apply();
                                //Back to Home page
                                Intent returnIntent = getIntent();
                                returnIntent.putExtra("email", email);
                                returnIntent.putExtra("firstName", fname);
                                returnIntent.putExtra("lastName", lname);
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                                showProgress(false);

                            } else {
                                Toast.makeText(SignUpActivity.this, "can't get user info",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Shows the progress UI and hides the register form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Email validation
     * @param email     Email address
     * @return      true or false
     */
    private boolean isEmailValid(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
