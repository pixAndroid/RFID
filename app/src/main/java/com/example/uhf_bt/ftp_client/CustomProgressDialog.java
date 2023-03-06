package com.example.uhf_bt.ftp_client;


import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.example.uhf_bt.R;


/********************************************
 *     Created by DailyCoding on 23-May-22.  *
 ********************************************/

public class CustomProgressDialog {

    private static Dialog mProgressdialog;
    public static void showDialog(Context mContext, boolean dialog_view) {
        if (dialog_view == true){
            showdialog (mContext);
        }else if (dialog_view == false){
            try {
                mProgressdialog.dismiss();
            }catch (Exception e){
            }
        }
    }

    private static void showdialog(Context mContext) {
        mProgressdialog = new Dialog(mContext);
        mProgressdialog.setContentView(R.layout.layout_progress_dialog);
        mProgressdialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mProgressdialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Window window = mProgressdialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.NO_GRAVITY;
        mProgressdialog.setCancelable(false);
        mProgressdialog.show();

    }

}
