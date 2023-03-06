package com.example.uhf_bt.ftp_client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uhf_bt.R;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPFile;

public class FtpClientActivity extends AppCompatActivity {
    private static final String TAG = "FtpClientActivity";


    Button btn_create_file, btn_upload_file;
    TextView txt_data, txt_current_path, txt_warning;
    ImageView btn_back, btn_connect;
    RecyclerView rv_ftp;

    FTPFile[] mFtpFiles;
    String mCurrentPath, tmpCurrentPath;
    FtpFileAdapter adapterFtp;

    EditText et_ftp_address, et_port, et_username, et_password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp_client);

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        btn_back = findViewById(R.id.btn_back);
        txt_current_path = findViewById(R.id.txt_current_path);
        btn_connect = findViewById(R.id.btn_connect);
        txt_data = findViewById(R.id.txt_data);
        btn_create_file = findViewById(R.id.btn_create_file);
        btn_upload_file = findViewById(R.id.btn_upload_file);
        rv_ftp = findViewById(R.id.rv_ftp);
        et_ftp_address = findViewById(R.id.et_ftp_address);
        et_port = findViewById(R.id.et_port);
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        txt_warning = findViewById(R.id.txt_warning);

        //INIT
        et_ftp_address.setText(FtpConstant.SERVER_IP);
        et_port.setText(FtpConstant.SERVER_PORT);
        et_username.setText(FtpConstant.USER_NAME);
        et_password.setText(FtpConstant.PASSWORD);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        adapterFtp = new FtpFileAdapter(FtpClientActivity.this, null);
        adapterFtp.setListener(new FtpFileAdapter.OnItemClickListener() {
            @Override
            public void onFolderClicked(String name) {
                CustomProgressDialog.showDialog(FtpClientActivity.this, true);
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        tmpCurrentPath = mCurrentPath +"/"+ name;
                        mCurrentPath = FtpConfig.changeDIR(tmpCurrentPath);

                        if (FtpConfig.clientReady()) {
                            mFtpFiles = FtpConfig.getAllFiles();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CustomProgressDialog.showDialog(FtpClientActivity.this, false);
                                txt_current_path.setText(mCurrentPath);
                                txt_data.setText("Successfully Connected to " + FtpConstant.SERVER_IP);
                                updateFileList();

                            }
                        });

                    }
                });
            }

            @Override
            public void onFileClicked(String name) {
                Toast.makeText(FtpClientActivity.this, "Not implemented :(", Toast.LENGTH_SHORT).show();
            }
        });
        rv_ftp.setLayoutManager(layoutManager);
        rv_ftp.setAdapter(adapterFtp);




        FtpConfig.init();

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txt_data.setText("Connecting to " + FtpConstant.SERVER_IP );
                txt_current_path.setText("...");

                CustomProgressDialog.showDialog(FtpClientActivity.this, true);
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {

                        FtpConfig.connectServer(
                                et_ftp_address.getText().toString(),
                                Integer.parseInt(et_port.getText().toString()),
                                et_username.getText().toString(),
                                et_password.getText().toString());

                        FtpConstant.SERVER_IP = et_ftp_address.getText().toString();

                        if (FtpConfig.clientReady()) {
                            mFtpFiles = FtpConfig.getAllFiles();
                            mCurrentPath = FtpConfig.getCurrentPath();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CustomProgressDialog.showDialog(FtpClientActivity.this, false);
                                txt_current_path.setText(mCurrentPath);
                                txt_data.setText("Successfully Connected to " + FtpConstant.SERVER_IP);
                                Toast.makeText(FtpClientActivity.this, "Server Connected..", Toast.LENGTH_SHORT).show();

                                updateFileList();

                            }
                        });

                    }
                });

            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CustomProgressDialog.showDialog(FtpClientActivity.this, true);
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {

                        if (!mCurrentPath.equals("/")) {
                            tmpCurrentPath = FtpConfig.goFolderBack();

                            Log.e(TAG, "tmpCurrentPath: " + tmpCurrentPath );
                            mCurrentPath = tmpCurrentPath;

                        }

                        if (FtpConfig.clientReady()) {
                            mFtpFiles = FtpConfig.getAllFiles();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CustomProgressDialog.showDialog(FtpClientActivity.this, false);


                                txt_current_path.setText(mCurrentPath);
                                txt_data.setText("Successfully Connected to " + FtpConstant.SERVER_IP);

                                updateFileList();

                            }
                        });


                    }
                });
            }
        });

        btn_upload_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomProgressDialog.showDialog(FtpClientActivity.this, false);

                executorService.execute(new Runnable() {
                   @Override
                   public void run() {
                       File file = FileUtil.createNewFile(FtpClientActivity.this);
                       Log.e(TAG, "run: " + file.getPath());
                       FtpConfig.upload(file, new FTPDataTransferListener() {
                           @Override
                           public void started() {
                               Log.e(TAG, "started: ");
                           }

                           @Override
                           public void transferred(int i) {
                               Log.e(TAG, "transferred: " + i);
                           }

                           @Override
                           public void completed() {
                               Log.e(TAG, "completed: ");
                               if (FtpConfig.clientReady()) {
                                   mFtpFiles = FtpConfig.getAllFiles();
                               }

                               runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       CustomProgressDialog.showDialog(FtpClientActivity.this, false);
                                       txt_current_path.setText(mCurrentPath);
                                       Toast.makeText(FtpClientActivity.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();

                                       updateFileList();

                                   }
                               });
                           }

                           @Override
                           public void aborted() {
                               Log.e(TAG, "aborted: ");
                           }

                           @Override
                           public void failed() {
                               Log.e(TAG, "failed: ");
                           }
                       });
                   }
               });
            }
        });
    }

    private void updateFileList() {
        if (mFtpFiles != null) {
            txt_warning.setVisibility(View.GONE);
            adapterFtp.setFtpFiles(mFtpFiles);
            if (mFtpFiles.length == 0)
                txt_warning.setVisibility(View.VISIBLE);

        } else {
            txt_warning.setVisibility(View.VISIBLE);
        }
    }

}