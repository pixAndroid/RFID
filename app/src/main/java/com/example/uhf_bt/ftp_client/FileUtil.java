package com.example.uhf_bt.ftp_client;


import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/********************************************
 *     Created by DailyCoding on 18-Jan-23.  *
 ********************************************/

public class FileUtil {
    private static final String TAG = "FileUtil";

    public static File createNewFile(Context context) {
        String path = context.getCacheDir().getPath();


        try {
            String rootPath = path + "/upload/";
            File root = new File(rootPath);
            if (!root.exists()) {
                root.mkdirs();
            }
            File f = new File(rootPath + System.currentTimeMillis()+ "_epc.txt");
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();

            FileOutputStream out = new FileOutputStream(f);

            out.write("Hello hi".getBytes());

            out.flush();
            out.close();

            return f;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static File createNewFile(Context context, List<String> stringList) {
        String path = context.getCacheDir().getPath();


        try {
            String rootPath = path + "/upload/";
            File root = new File(rootPath);
            if (!root.exists()) {
                root.mkdirs();
            }
            File f = new File(rootPath
                    + TimeUtil.convertLongTimeToDate(System.currentTimeMillis())+ "_epc.txt");
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();

            FileOutputStream out = new FileOutputStream(f);

            if (stringList != null && stringList.size() > 0) {
                for (int i = 0; i < stringList.size(); i++) {
                    String dat = stringList.get(i) + "\n";
                    out.write(dat.getBytes());
                }
            }


            out.flush();
            out.close();

            return f;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public static String getFileValue(String fileName, Context context) {

        String path = context.getCacheDir().getPath();


        try {
            String rootPath = path + "/MyFolder/";
            File root = new File(rootPath);
            if (!root.exists()) {
                root.mkdirs();
            }
            File f = new File(rootPath + "upload_test.txt");
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();

            FileOutputStream out = new FileOutputStream(f);

            out.write("Hello World".getBytes());


            //out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }








        try {
            StringBuffer outStringBuf = new StringBuffer();
            String inputLine = "";
            /*
             * We have to use the openFileInput()-method the ActivityContext
             * provides. Again for security reasons with openFileInput(...)
             */
            FileInputStream fIn = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader inBuff = new BufferedReader(isr);
            while ((inputLine = inBuff.readLine()) != null) {
                outStringBuf.append(inputLine);
                outStringBuf.append("\n");
            }
            inBuff.close();
            return outStringBuf.toString();
        } catch (IOException e) {
            Log.e(TAG, "getFileValue: " + e.getMessage() );
            return null;
        }
    }

    public static boolean appendFileValue(String fileName, String value,
                                          Context context) {
        return writeToFile(fileName, value, context, Context.MODE_APPEND);
    }

    public static boolean setFileValue(String fileName, String value,
                                       Context context) {
        return writeToFile(fileName, value, context,
                Context.MODE_WORLD_READABLE);
    }

    public static boolean writeToFile(String fileName, String value,
                                      Context context, int writeOrAppendMode) {
        // just make sure it's one of the modes we support
        if (writeOrAppendMode != Context.MODE_WORLD_READABLE
                && writeOrAppendMode != Context.MODE_WORLD_WRITEABLE
                && writeOrAppendMode != Context.MODE_APPEND) {
            return false;
        }
        try {
            /*
             * We have to use the openFileOutput()-method the ActivityContext
             * provides, to protect your file from others and This is done for
             * security-reasons. We chose MODE_WORLD_READABLE, because we have
             * nothing to hide in our file
             */
            FileOutputStream fOut = context.openFileOutput(fileName,
                    writeOrAppendMode);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            // Write the string to the file
            osw.write(value);
            // save and close
            osw.flush();
            osw.close();
        } catch (IOException e) {
            Log.e(TAG, "writeToFile: " + e.getMessage() );
            return false;
        }
        return true;
    }

    public static void deleteFile(String fileName, Context context) {
        context.deleteFile(fileName);
    }

    /*public static File createFile(String data, Context context) {
        try {
            // catches IOException below
            final String TESTSTRING = new String(data);


            FileOutputStream fOut = context.openFileOutput("samplefile.txt",
                    Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            // Write the string to the file
            osw.write(TESTSTRING);

            osw.flush();
            osw.close();

//Reading the file back...

            FileInputStream fIn = context.openFileInput("samplefile.txt");
            InputStreamReader isr = new InputStreamReader(fIn);


            char[] inputBuffer = new char[TESTSTRING.length()];

            // Fill the Buffer with data from the file
            isr.read(inputBuffer);

            // Transform the chars to a String
            String readString = new String(inputBuffer);

            // Check if we read back the same chars that we had written out
            boolean isTheSame = TESTSTRING.equals(readString);

            Log.i("File Reading stuff", "success = " + isTheSame);

        } catch (IOException ioe)
        {ioe.printStackTrace();}
    }*/

}
