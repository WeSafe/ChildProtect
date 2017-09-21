package com.example.yinqinghao.childprotect;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.brouding.simpledialog.SimpleDialog;
import com.dd.processbutton.FlatButton;
import com.example.yinqinghao.childprotect.entity.Feedback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hsalf.smilerating.BaseRating;
import com.hsalf.smilerating.SmileRating;

import java.util.Date;

public class FeedbackActivity extends AppCompatActivity {
    private SmileRating mFunctionRating;
    private SmileRating mDesignRating;
    private SmileRating mEaseRating;
    private SmileRating mOverallRating;
    private EditText mCommentText;
    private EditText mEmailText;
    private FlatButton mSubmit;
    private SimpleDialog mLoadingDialog;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDb;

    private View.OnClickListener mListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        setTitle("Feedback");

        mFunctionRating = (SmileRating) findViewById(R.id.function_rating);
        mDesignRating = (SmileRating) findViewById(R.id.design_rating);
        mEaseRating = (SmileRating) findViewById(R.id.use_rating);
        mOverallRating = (SmileRating) findViewById(R.id.overall_rating);

        mCommentText = (EditText) findViewById(R.id.txt_comments);
        mEmailText = (EditText) findViewById(R.id.txt_feedback_email);
        mSubmit = (FlatButton) findViewById(R.id.btn_sub);
        init();

        mSubmit.setOnClickListener(mListener);

    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance();

        mFunctionRating.setSelectedSmile(2);
        mDesignRating.setSelectedSmile(2);
        mEaseRating.setSelectedSmile(2);
        mOverallRating.setSelectedSmile(2);

        mListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        };
    }

    private void submit() {
        showProgress();
        mEmailText.setError(null);
        int functionRating = mFunctionRating.getRating();
        int designRating = mDesignRating.getRating();
        int easeRating = mEaseRating.getRating();
        int overallRating = mOverallRating.getRating();
        String comment = mCommentText.getText().toString();
        String email = mEmailText.getText().toString().trim();
        if (email.length() != 0 && !isEmailValid(email)) {
            mEmailText.setError("Please enter valid email address");
            mLoadingDialog.dismiss();
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();
        Feedback feedback = new Feedback(new Date(), functionRating, designRating,
                easeRating, overallRating, comment, uid, email);
        DatabaseReference ref = mDb.getReference("feedback");
        String key = ref.push().getKey();
        ref.child(key).setValue(feedback).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(FeedbackActivity.this, "Thanks for your feedback.", Toast.LENGTH_SHORT).show();
                    finish();
                    if (mLoadingDialog != null) {
                        mLoadingDialog.dismiss();
                    }
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
