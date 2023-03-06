package com.example.uhf_bt.ftp_client;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.uhf_bt.R;


/********************************************
 *     Created by DailyCoding on 27-May-22.  *
 ********************************************/

public class FtpSettingsDialog {

    public static void showDialog(Activity activity){
        Dialog dialog = new Dialog(activity);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_ftp_settings);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {

            }
        });


        EditText et_ftp_address = dialog.findViewById(R.id.et_ftp_address);
        EditText et_port = dialog.findViewById(R.id.et_port);
        EditText et_username = dialog.findViewById(R.id.et_username);
        EditText et_password = dialog.findViewById(R.id.et_password);
        Button btn_save = dialog.findViewById(R.id.btn_save);


        et_ftp_address.setText(FtpPref.readFtpAddress(activity));
        et_port.setText(FtpPref.readFtpPort(activity));
        et_username.setText(FtpPref.readFtpUsername(activity));
        et_password.setText(FtpPref.readFtpPassword(activity));

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = et_ftp_address.getText().toString().trim();
                String port = et_port.getText().toString().trim();
                String username = et_username.getText().toString().trim();
                String password = et_password.getText().toString().trim();

                if (!address.equals("")) {
                    if (!port.equals("")) {
                        if (!username.equals("")) {
                            if (!password.equals("")) {
                                FtpPref.saveFtpAddress(activity, address);
                                FtpPref.saveFtpPort(activity, port);
                                FtpPref.saveFtpUsername(activity, username);
                                FtpPref.saveFtpPassword(activity, password);

                                Toast.makeText(activity, "FTP Credential Saved", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(activity, "Address field is empty :(", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(activity, "Address field is empty :(", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(activity, "Port field is empty :(", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(activity, "Address field is empty :(", Toast.LENGTH_SHORT).show();
                }
            }
        });



        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.bg_ftp_dialog));
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setAttributes(lp);

        dialog.show();
    }

}
