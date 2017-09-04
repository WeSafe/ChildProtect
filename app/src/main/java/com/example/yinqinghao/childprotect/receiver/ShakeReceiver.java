package com.example.yinqinghao.childprotect.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.brouding.simpledialog.SimpleDialog;
import com.example.yinqinghao.childprotect.MainActivity;
import com.example.yinqinghao.childprotect.SOSActivity;
import com.example.yinqinghao.childprotect.entity.SharedData;

/**
 * Created by yinqinghao on 3/9/17.
 */

public class ShakeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (null != intent && intent.getAction().equals("shake.detector")) {
             final Context c = SharedData.peekContext();
                if (!SharedData.isShown() && c != null && !SharedData.isSos()) {
                    SharedData.setIsShown(true);
                    new SimpleDialog.Builder(c)
                            .setTitle("Do you want to send SOS message", true)
                            .onConfirm(new SimpleDialog.BtnCallback() {
                                @Override
                                public void onClick(@NonNull SimpleDialog dialog, @NonNull SimpleDialog.BtnAction which) {
                                    Intent intent = new Intent(c, SOSActivity.class);
                                    c.startActivity(intent);
                                    SharedData.setIsShown(false);
                                }
                            })
                            .setBtnConfirmText("Yes")
                            .setBtnConfirmTextColor("#e6b115")
                            .setBtnCancelText("No")
                            .onCancel(new SimpleDialog.BtnCallback() {
                                @Override
                                public void onClick(@NonNull SimpleDialog dialog, @NonNull SimpleDialog.BtnAction which) {
                                    SharedData.setIsShown(false);
                                }
                            })
                            .show();

                }

        }
    }
}
