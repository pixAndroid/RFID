package com.example.uhf_bt.ftp_client;


import android.util.Log;


import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import javax.net.ssl.SSLSocketFactory;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPCommunicationListener;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

/********************************************
 *     Created by DailyCoding on 17-Jan-23.  *
 ********************************************/

public class FtpConfig {
    private static final String TAG = "FtpConfig";

    private static FTPClient ftpClient;
    private static String SERVER_ADDRESS;
    private static int PORT;

    public static void init() {
        if (ftpClient == null) {
            ftpClient = new FTPClient();
        } else {
            try {
                if (ftpClient.isConnected())
                    ftpClient.disconnect(true);
                ftpClient = null;
                init();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "init: " + e.getMessage() );
            } catch (FTPIllegalReplyException e) {
                e.printStackTrace();
                Log.e(TAG, "init: " + e.getMessage() );
            } catch (FTPException e) {
                e.printStackTrace();
                Log.e(TAG, "init: " + e.getMessage() );
            }
        }
    }

    public static boolean clientReady () {
        if (ftpClient != null) {
            if (ftpClient.isConnected()) {
                return true;
            }
        }

        return false;
    }

    public static FTPClient getFtpClient() {
        if (ftpClient != null) {
            return ftpClient;
        }
        return null;
    }


    public static void connectServer(String server_ip,
                                     int port,
                                     String user_name,
                                     String password) {
        if (ftpClient != null && !ftpClient.isConnected()) {
            try {
                ftpClient.setMLSDPolicy(FTPClient.MLSD_IF_SUPPORTED);
                ftpClient.setPassive(true);
                ftpClient.setType(FTPClient.TYPE_BINARY);
                //ftpClient.setSecurity(FTPClient.SECURITY_FTPS); // enables FTPS

                ftpClient.connect(server_ip, 21);
                ftpClient.login(user_name, password);

                //CHECK COMPRESSION SUPPORTS ON SERVER
                boolean compressionSupported = ftpClient.isCompressionSupported();
                if (compressionSupported) {
                    ftpClient.setCompressionEnabled(true);
                }

                Log.e(TAG, "CompressionSupported: " +  compressionSupported);

                //TODO CURRENT DIRECTORY
//                String dir = ftpClient.currentDirectory();
//                Log.e(TAG, "connectServer: DIR ==> " + dir );

                //TODO CHANGE DIRECTORY
                //ftpClient.changeDirectory("/an/absolute/one");

                //TODO Back to the parent directory with:
                //client.changeDirectoryUp();

                //TODO rename a remote file or directory:
                //client.rename("oldname", "newname");

                //TODO move file
                //client.rename("myfile.txt", "myfolder/myfile.txt");

                //TODO DELETE FILE
                //client.deleteFile(relativeOrAbsolutePath);

                //TODO CREATE DIRECTORY
                //client.createDirectory("newfolder");

                //TODO DELETE DIR
                //client.deleteDirectory("oldfolder");

                //TODO filter parameter with the list() method, i.e.:
                //FTPFile[] list = ftpClient.list("*.jpg");

                //FTPFile[] allFiles = ftpClient.list();

               /* for (int i = 0; i < allFiles.length; i++) {
                    Log.e(TAG, "" + allFiles[i].getName() );
                }*/


                if (ftpClient.isConnected())
                    Log.e(TAG, "connectServer: FILES ==> " + ftpClient.list().length);


                /*ftpClient.addCommunicationListener(new FTPCommunicationListener() {
                    @Override
                    public void sent(String s) {
                        Log.e(TAG, "sent: " + s );
                    }

                    @Override
                    public void received(String s) {
                        Log.e(TAG, "received: " + s );
                    }
                });*/

                Log.e(TAG, "connectServer: END");

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "IOException: " + e.getMessage() );
            } catch (FTPIllegalReplyException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPIllegalReplyException: " + e.getMessage() );
            } catch (FTPException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPException: " + e.getMessage() );
            } catch (FTPAbortedException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPAbortedException: " + e.getMessage() );
            } catch (FTPDataTransferException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPDataTransferException: " + e.getMessage() );
            } catch (FTPListParseException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPListParseException: " + e.getMessage() );
            }

            Log.e(TAG, "connectServer: END");
        }
    }

    public static FTPFile[] getAllFiles() {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                return ftpClient.list();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "IOException: " + e.getMessage() );
            } catch (FTPIllegalReplyException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPIllegalReplyException: " + e.getMessage() );
            } catch (FTPException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPException: " + e.getMessage() );
            } catch (FTPAbortedException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPAbortedException: " + e.getMessage() );
            } catch (FTPDataTransferException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPDataTransferException: " + e.getMessage() );
            } catch (FTPListParseException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPListParseException: " + e.getMessage() );
            }
        }
        return null;
    }

    public static String getCurrentPath() {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                return ftpClient.currentDirectory();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "IOException: " + e.getMessage() );
            } catch (FTPIllegalReplyException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPIllegalReplyException: " + e.getMessage() );
            } catch (FTPException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPException: " + e.getMessage() );
            }
        }
        return "";
    }

    public static String goFolderBack() {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {

                ftpClient.changeDirectoryUp();

                return ftpClient.currentDirectory();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "IOException: " + e.getMessage() );
            } catch (FTPIllegalReplyException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPIllegalReplyException: " + e.getMessage() );
            } catch (FTPException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPException: " + e.getMessage() );
            }
        }
        return "";
    }

    public static String changeDIR(String pathDIR) {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.changeDirectory(pathDIR);
                return ftpClient.currentDirectory();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "IOException: " + e.getMessage() );
            } catch (FTPIllegalReplyException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPIllegalReplyException: " + e.getMessage() );
            } catch (FTPException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPException: " + e.getMessage() + pathDIR);
            }
        }
        return "";
    }


    public static void upload(File file, FTPDataTransferListener listener) {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                Log.e(TAG, "upload: prepare" );
                ftpClient.upload(file, listener);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "IOException: " + e.getMessage() );
                if (e.getMessage().equals("FTPConnection closed")
                || e.getMessage().equals("Broken pipe")) {
                    init();
                    connectServer(
                            FtpConstant.SERVER_IP,
                            Integer.parseInt(FtpConstant.SERVER_PORT),
                            FtpConstant.USER_NAME,
                            FtpConstant.PASSWORD);
                    upload(file, listener);
                }

            } catch (FTPIllegalReplyException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPIllegalReplyException: " + e.getMessage() );
            } catch (FTPException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPException: " + e.getMessage() );

                if (e.getMessage().toString().equals("Timeout.")) {
                    connectServer(
                            FtpConstant.SERVER_IP,
                            Integer.parseInt(FtpConstant.SERVER_PORT),
                            FtpConstant.USER_NAME,
                            FtpConstant.PASSWORD);
                    upload(file, listener);
                }

            } catch (FTPAbortedException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPAbortedException: " + e.getMessage() );
            } catch (FTPDataTransferException e) {
                e.printStackTrace();
                Log.e(TAG, "FTPDataTransferException: " + e.getMessage() );
            }

        } else {
            connectServer(
                    FtpConstant.SERVER_IP,
                    Integer.parseInt(FtpConstant.SERVER_PORT),
                    FtpConstant.USER_NAME,
                    FtpConstant.PASSWORD);
            upload(file, listener);
        }
    }


}
