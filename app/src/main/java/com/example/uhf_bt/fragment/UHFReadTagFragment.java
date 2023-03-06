package com.example.uhf_bt.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uhf_bt.DateUtils;
import com.example.uhf_bt.FileUtils;
import com.example.uhf_bt.MainActivity;
import com.example.uhf_bt.NumberTool;
import com.example.uhf_bt.R;
import com.example.uhf_bt.Utils;
import com.example.uhf_bt.ftp_client.CustomProgressDialog;
import com.example.uhf_bt.ftp_client.FileUtil;
import com.example.uhf_bt.ftp_client.FtpClientActivity;
import com.example.uhf_bt.ftp_client.FtpConfig;
import com.example.uhf_bt.ftp_client.FtpConstant;
import com.example.uhf_bt.tool.CheckUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rscja.deviceapi.RFIDWithUHFBLE;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.interfaces.ConnectionStatus;
import com.rscja.deviceapi.interfaces.KeyEventCallback;
import com.rscja.utility.LogUtility;
import com.rscja.utility.StringUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.fragment.app.Fragment;

import it.sauronsoftware.ftp4j.FTPDataTransferListener;


public class UHFReadTagFragment extends Fragment implements View.OnClickListener {

    private String TAG = "UHFReadTagFragment";
    int lastIndex=-1;
    private ListView LvTags;
    private Button InventoryLoop, btInventory, btStop;//
    private Button btInventoryPerMinute;
    private Button btClear;
    private FloatingActionButton btn_upload_ftp;
    private TextView tv_count, tv_total, tv_time;
    private boolean isExit = false;
    private long total = 0;
    private MainActivity mContext;
    private MyAdapter adapter;
    private List<UHFTAGInfo> tempDatas = new ArrayList<>();
    private ArrayList<HashMap<String, String>> tagList;

    ExecutorService executorService;
    private List<String> epcList;

    private ConnectStatus mConnectStatus = new ConnectStatus();

    //--------------------------------------获取 解析数据-------------------------------------------------
    final int FLAG_START = 0;//开始
    final int FLAG_STOP = 1;//停止
    final int FLAG_UPDATE_TIME = 2; // 更新时间
    final int FLAG_UHFINFO = 3;
    final int FLAG_UHFINFO_LIST = 5;
    final int FLAG_SUCCESS = 10;//成功
    final int FLAG_FAIL = 11;//失败

    private long mStrTime;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FLAG_STOP:
                    if (msg.arg1 == FLAG_SUCCESS) {
                        //停止成功
                        btClear.setEnabled(true);
                        btStop.setEnabled(false);
                        InventoryLoop.setEnabled(true);
                        btInventory.setEnabled(true);
                        btInventoryPerMinute.setEnabled(true);
                    } else {
                        //停止失败
                        Utils.playSound(2);
                        Toast.makeText(mContext, R.string.uhf_msg_inventory_stop_fail, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case FLAG_UHFINFO_LIST:
                    List<UHFTAGInfo> list = ( List<UHFTAGInfo>) msg.obj;
                    addEPCToList(list);
                    break;
                case FLAG_START:
                    if (msg.arg1 == FLAG_SUCCESS) {
                        //开始读取标签成功
                        btClear.setEnabled(false);
                        btStop.setEnabled(true);
                        InventoryLoop.setEnabled(false);
                        btInventory.setEnabled(false);
                        btInventoryPerMinute.setEnabled(false);
                    } else {
                        //开始读取标签失败
                        Utils.playSound(2);
                    }
                    break;
                case FLAG_UPDATE_TIME:
                    float useTime = (System.currentTimeMillis() - mStrTime) / 1000.0F;
                    tv_time.setText(NumberTool.getPointDouble(1, useTime) + "s");
                    break;
                case FLAG_UHFINFO:
                    UHFTAGInfo info = (UHFTAGInfo) msg.obj;
                    List list1=new ArrayList<UHFTAGInfo>();
                    list1.add(info);
                    addEPCToList(list1);
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_uhfread_tag, container, false);
        initFilter(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "UHFReadTagFragment.onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();
        lastIndex=-1;
        mContext = (MainActivity) getActivity();
        init();
        selectIndex=-1;
        mContext.selectEPC=null;
        epcList = new ArrayList<>();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                FtpConfig.init();
            }
        });


        //temp epc list add
        /*for (int i = 990; i < 999; i++) {
            epcList.add("EPC:126365354575678679876545634354" + i);
        }*/


    }



    @Override
    public void onPause() {
        super.onPause();
        stop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isExit = true;
        mContext.removeConnectStatusNotice(mConnectStatus);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btClear:
                clearData();
                break;
            case R.id.btInventoryPerMinute:
                inventoryPerMinute();
                break;
            case R.id.InventoryLoop:
                startThread();
                break;
            case R.id.btInventory:
                inventory();
                break;
            case R.id.btStop:
                stop();
                break;
            case R.id.btn_upload_ftp:
                //TODO 03 UPLOAD FILE
                CustomProgressDialog.showDialog(mContext, false);
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "Connecting..", Toast.LENGTH_SHORT).show();
                            }
                        });

                        FtpConfig.connectServer(FtpConstant.SERVER_IP,
                                Integer.parseInt(FtpConstant.SERVER_PORT),
                                FtpConstant.USER_NAME,
                                FtpConstant.PASSWORD);

                        //TODO 01 CREATE FILE LOCAL
                        if (epcList.size() == 0)
                            epcList.add("No EPC tag found");
                        File file = FileUtil.createNewFile(mContext, epcList);
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

                                }

                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        epcList.clear();
                                        CustomProgressDialog.showDialog(mContext, false);
                                        Toast.makeText(mContext, "Successfully uploaded", Toast.LENGTH_SHORT).show();

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






                break;
        }
    }

    private void init() {
        isExit = false;
        mContext.addConnectStatusNotice(mConnectStatus);
        LvTags = (ListView) mContext.findViewById(R.id.LvTags);
        btInventory = (Button) mContext.findViewById(R.id.btInventory);
        InventoryLoop = (Button) mContext.findViewById(R.id.InventoryLoop);
        btStop = (Button) mContext.findViewById(R.id.btStop);
        btStop.setEnabled(false);
        btClear = (Button) mContext.findViewById(R.id.btClear);
        tv_count = (TextView) mContext.findViewById(R.id.tv_count);
        tv_total = (TextView) mContext.findViewById(R.id.tv_total);
        tv_time = (TextView) mContext.findViewById(R.id.tv_time);
        btn_upload_ftp = mContext.findViewById(R.id.btn_upload_ftp);

        InventoryLoop.setOnClickListener(this);
        btInventory.setOnClickListener(this);
        btClear.setOnClickListener(this);
        btStop.setOnClickListener(this);
        btn_upload_ftp.setOnClickListener(this);

        btInventoryPerMinute = mContext.findViewById(R.id.btInventoryPerMinute);
        btInventoryPerMinute.setOnClickListener(this);

        tagList = new ArrayList<>();
        mContext.tagList=tagList;
        adapter=new MyAdapter(mContext);
        LvTags.setAdapter(adapter);
        mContext.uhf.setKeyEventCallback(new KeyEventCallback() {
            @Override
            public void onKeyDown(int keycode) {
                Log.d(TAG, "  keycode =" + keycode + "   ,isExit=" + isExit);
                if (!isExit && mContext.uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
                    //startThread();
                    if(keycode==3){
                        startThread();
                    } else {
                        if (keycode==1) {
                            if (mContext.isScanning) {
                                stop();
                            } else {
                                startThread();
                            }
                        }else{
                            if (mContext.isScanning) {
                                stop();
                                SystemClock.sleep(100);
                            }
                            inventory();
                        }
                    }

                }
            }

           /* @Override
            public void onKeyUp(int keycode) {
                Log.d(TAG, "  keycode =" + keycode + "   ,isExit=" + isExit);
                stop();
            }*/
        });

        LvTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectIndex=position;
                adapter.notifyDataSetInvalidated();
                mContext.selectEPC=tagList.get(position).get(MainActivity.TAG_EPC);
            }
        });

        clearData();
    }

    private CheckBox cbFilter;
    private ViewGroup layout_filter;
    private Button btnSetFilter;
    private void initFilter(View view) {
        layout_filter = (ViewGroup) view.findViewById(R.id.layout_filter);

        layout_filter.setVisibility(View.GONE);
        cbFilter = (CheckBox) view.findViewById(R.id.cbFilter);
        cbFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layout_filter.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        final EditText etLen = (EditText) view.findViewById(R.id.etLen);
        final EditText etPtr = (EditText) view.findViewById(R.id.etPtr);
        final EditText etData = (EditText) view.findViewById(R.id.etData);
        final RadioButton rbEPC = (RadioButton) view.findViewById(R.id.rbEPC);
        final RadioButton rbTID = (RadioButton) view.findViewById(R.id.rbTID);
        final RadioButton rbUser = (RadioButton) view.findViewById(R.id.rbUser);
        btnSetFilter = (Button) view.findViewById(R.id.btSet);

        btnSetFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int filterBank = RFIDWithUHFBLE.Bank_EPC;
                if (rbEPC.isChecked()) {
                    filterBank = RFIDWithUHFBLE.Bank_EPC;
                } else if (rbTID.isChecked()) {
                    filterBank = RFIDWithUHFBLE.Bank_TID;
                } else if (rbUser.isChecked()) {
                    filterBank = RFIDWithUHFBLE.Bank_USER;
                }
                if (etLen.getText().toString() == null || etLen.getText().toString().isEmpty()) {
                    showToast("数据长度不能为空");
                    return;
                }
                if (etPtr.getText().toString() == null || etPtr.getText().toString().isEmpty()) {
                    showToast("起始地址不能为空");
                    return;
                }
                int ptr = Utils.toInt(etPtr.getText().toString(), 0);
                int len = Utils.toInt(etLen.getText().toString(), 0);
                String data = etData.getText().toString().trim();
                if (len > 0 && !TextUtils.isEmpty(data)) {
                    String rex = "[\\da-fA-F]*"; //匹配正则表达式，数据为十六进制格式
                    if (!data.matches(rex)) {
                        showToast("过滤的数据必须是十六进制数据");
//                        mContext.playSound(2);
                        return;
                    }

                    int l = data.replace(" ", "").length();
                    if (len <= l * 4) {
                        if(l % 2 != 0)
                            data += "0";
                    } else {
                        showToast(R.string.uhf_msg_set_filter_fail2);
                        return;
                    }

                    if (mContext.uhf.setFilter(filterBank, ptr, len, data)) {
                        showToast(R.string.uhf_msg_set_filter_succ);
                    } else {
                        showToast(R.string.uhf_msg_set_filter_fail);
                    }
                } else {
                    //禁用过滤
                    String dataStr = "00";
                    if (mContext.uhf.setFilter(RFIDWithUHFBLE.Bank_EPC, 0, 0, dataStr)
                            && mContext.uhf.setFilter(RFIDWithUHFBLE.Bank_TID, 0, 0, dataStr)
                            && mContext.uhf.setFilter(RFIDWithUHFBLE.Bank_USER, 0, 0, dataStr)) {
                        showToast(R.string.msg_disable_succ);
                    } else {
                        showToast(R.string.msg_disable_fail);
                    }
                }
                cbFilter.setChecked(false);
            }
        });

        rbEPC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rbEPC.isChecked()) {
                    etPtr.setText("32");
                }
            }
        });
        rbTID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rbTID.isChecked()) {
                    etPtr.setText("0");
                }
            }
        });
        rbUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rbUser.isChecked()) {
                    etPtr.setText("0");
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mContext.uhf.getConnectStatus() == ConnectionStatus.CONNECTED) {
            InventoryLoop.setEnabled(true);
            btInventory.setEnabled(true);
            btInventoryPerMinute.setEnabled(true);

            cbFilter.setEnabled(true);
        } else {
            InventoryLoop.setEnabled(false);
            btInventory.setEnabled(false);
            btInventoryPerMinute.setEnabled(false);

            cbFilter.setChecked(false);
            cbFilter.setEnabled(false);
        }
    }

    private void clearData() {
        total = 0;
        tv_count.setText("0");
        tv_total.setText("0");
        tv_time.setText("0s");
        tagList.clear();
        tempDatas.clear();
        adapter.notifyDataSetChanged();
        mContext.selectEPC=null;
        selectIndex=-1;
    }

    /**
     * 停止识别
     */
    private void stop() {
        cancelInventoryTask();
        mContext.isScanning=false;
    }

    private void stopInventory(){
        boolean result = mContext.uhf.stopInventory();
        ConnectionStatus connectionStatus = mContext.uhf.getConnectStatus();
        Message msg = handler.obtainMessage(FLAG_STOP);
        if (!result || connectionStatus == ConnectionStatus.DISCONNECTED) {
            msg.arg1 = FLAG_FAIL;
        } else {
            msg.arg1 = FLAG_SUCCESS;
        }
        handler.sendMessage(msg);
    }
    class ConnectStatus implements MainActivity.IConnectStatus {
        @Override
        public void getStatus(ConnectionStatus connectionStatus) {
            if (connectionStatus == ConnectionStatus.CONNECTED) {
                if (!mContext.isScanning) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    InventoryLoop.setEnabled(true);
                    btInventory.setEnabled(true);
                    btInventoryPerMinute.setEnabled(true);
                }

                cbFilter.setEnabled(true);
            } else if (connectionStatus == ConnectionStatus.DISCONNECTED) {
                stop();
                btClear.setEnabled(true);
                btStop.setEnabled(false);
                InventoryLoop.setEnabled(false);
                btInventory.setEnabled(false);
                btInventoryPerMinute.setEnabled(false);

                cbFilter.setChecked(false);
                cbFilter.setEnabled(false);
            }
        }
    }

    public void startThread() {
        if (mContext.isScanning) {
            return;
        }
        mContext.isScanning = true;
        cbFilter.setChecked(false);
        new TagThread().start();
    }


    class TagThread extends Thread {
        public void run() {
            Message msg = handler.obtainMessage(FLAG_START);
            if (mContext.uhf.startInventoryTag()) {
                mStrTime = System.currentTimeMillis();
                msg.arg1 = FLAG_SUCCESS;
            } else {
                msg.arg1 = FLAG_FAIL;
                mContext.isScanning=false;
            }
            handler.sendMessage(msg);
            long startTime=System.currentTimeMillis();
            while (mContext.isScanning) {
                List<UHFTAGInfo> list = getUHFInfo();
                if(list==null || list.size()==0){
                    SystemClock.sleep(1);
                }else{
                    Utils.playSound(1);
                    handler.sendMessage(handler.obtainMessage(FLAG_UHFINFO_LIST, list));
                }
                if(System.currentTimeMillis()-startTime>100){
                    startTime=System.currentTimeMillis();
                    handler.sendEmptyMessage(FLAG_UPDATE_TIME);
                }

            }
           stopInventory();
        }
    }

    private synchronized   List<UHFTAGInfo> getUHFInfo() {
        List<UHFTAGInfo> list=null;
        if(mContext.isSupportRssi){
            //旧主板才需要调用readTagFromBufferList_EpcTidUser 输出 RSSI
            list = mContext.uhf.readTagFromBufferList_EpcTidUser();
        }else {
            //读写器主板版本 2.20-2.29 readTagFromBufferList 函数支持输出Rssi，无需调用readTagFromBufferList_EpcTidUser
           list = mContext.uhf.readTagFromBufferList();
        }
        return list;
    }



    //TODO 02 LIST OF TAGS
    void insertTag(UHFTAGInfo info, int index,boolean exists){
        String data=info.getEPC();

        //PREPARE LIST
        epcList.add("EPC:" + data);

        /*info.getEPC();
        info.getTid();
        info.getUser();
        info.getRssi();
        info.getPc();*/


        if(!TextUtils.isEmpty(info.getTid())){
            StringBuilder stringBuilder=new StringBuilder();
            stringBuilder.append("EPC:");
            stringBuilder.append(info.getEPC());
            stringBuilder.append("\n");
            stringBuilder.append("TID:");
            stringBuilder.append(info.getTid());
            if(!TextUtils.isEmpty(info.getUser())){
                stringBuilder.append("\n");
                stringBuilder.append("USER:");
                stringBuilder.append(info.getUser());
            }
            data=stringBuilder.toString();
        }
        HashMap<String,String> tagMap=null;
        if(exists){
            tagMap = tagList.get(index);
            tagMap.put(MainActivity.TAG_COUNT, String.valueOf(Integer.parseInt(tagMap.get(MainActivity.TAG_COUNT), 10) + 1));
        }else {
            tagMap = new HashMap<>();
            tagMap.put(MainActivity.TAG_EPC, info.getEPC());
            tagMap.put(MainActivity.TAG_COUNT, String.valueOf(1));
            tempDatas.add(index,info);
            tagList.add(index, tagMap);
            tv_count.setText(String.valueOf(tagList.size()));
        }
        tagMap.put(MainActivity.TAG_USER, info.getUser());
        tagMap.put(MainActivity.TAG_DATA, data);
        tagMap.put(MainActivity.TAG_TID, info.getTid());
        tagMap.put(MainActivity.TAG_RSSI, info.getRssi()==null?"":info.getRssi());
        tv_total.setText(String.valueOf(++total));
        adapter.notifyDataSetChanged();
    }

    //TODO 01
    private void addEPCToList(List<UHFTAGInfo> list) {
        for(int k=0;k<list.size();k++){
            boolean[] exists=new boolean[1];
            UHFTAGInfo info=list.get(k);
            int idx=CheckUtils.getInsertIndex(tempDatas,info,exists);
            insertTag(info,idx,exists[0]);
        }
    }

    private Timer mTimer = new Timer();
    private TimerTask mInventoryPerMinuteTask;
    private long period = 6 * 1000; // 每隔多少ms
    private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "BluetoothReader" + File.separator;
    private String fileName;
    private void inventoryPerMinute() {
        cancelInventoryTask();
        btInventoryPerMinute.setEnabled(false);
        btInventory.setEnabled(false);
        InventoryLoop.setEnabled(false);
        btStop.setEnabled(true);
        mContext.isScanning = true;
        fileName = path + "battery_" + DateUtils.getCurrFormatDate(DateUtils.DATEFORMAT_FULL) + ".txt";
        mInventoryPerMinuteTask = new TimerTask() {
            @Override
            public void run() {
                String data = DateUtils.getCurrFormatDate(DateUtils.DATEFORMAT_FULL) + "\t电量：" + mContext.uhf.getBattery() + "%\n";
                FileUtils.writeFile(fileName, data, true);
                inventory();
            }
        };
        mTimer.schedule(mInventoryPerMinuteTask, 0, period);
    }

    private void cancelInventoryTask() {
        if(mInventoryPerMinuteTask != null) {
            mInventoryPerMinuteTask.cancel();
            mInventoryPerMinuteTask = null;
        }
    }

    private void inventory() {
        mStrTime = System.currentTimeMillis();
        UHFTAGInfo info = mContext.uhf.inventorySingleTag();
        if (info != null) {
            Message msg = handler.obtainMessage(FLAG_UHFINFO);
            msg.obj = info;
            handler.sendMessage(msg);
        }
        handler.sendEmptyMessage(FLAG_UPDATE_TIME);
    }

    private Toast mToast;
    public void showToast(String text) {
        if(mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getContext(), text, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void showToast(int resId) {
        showToast(getString(resId));
    }
    //-----------------------------
    private int  selectIndex=-1;
    public final class ViewHolder {
        public TextView tvEPCTID;
        public TextView tvTagCount;
        public TextView tvTagRssi;
    }

    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        public MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }
        public int getCount() {
            // TODO Auto-generated method stub
            return tagList.size();
        }
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return tagList.get(arg0);
        }
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.listtag_items, null);
                holder.tvEPCTID = (TextView) convertView.findViewById(R.id.TvTagUii);
                holder.tvTagCount = (TextView) convertView.findViewById(R.id.TvTagCount);
                holder.tvTagRssi = (TextView) convertView.findViewById(R.id.TvTagRssi);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvEPCTID.setText((String) tagList.get(position).get(MainActivity.TAG_DATA));
            holder.tvTagCount.setText((String) tagList.get(position).get(MainActivity.TAG_COUNT));
            holder.tvTagRssi.setText((String) tagList.get(position).get(MainActivity.TAG_RSSI));

            if (position == selectIndex) {
                convertView.setBackgroundColor(mContext.getResources().getColor(R.color.blue));
            }
            else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }
            return convertView;
        }

    }



}
