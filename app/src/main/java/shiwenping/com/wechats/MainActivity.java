package shiwenping.com.wechats;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import shiwenping.com.adapter.MainAdapter;
import shiwenping.com.utils.DLog;
import shiwenping.com.wechats.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button startService;
    private Button sendBtn;
    private TextView number;
    private List<Page> data;
    private MainAdapter adapter;
    private ListView listView;

    private String TAG = "HaiChecker";
    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + NotificationService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }
    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // TODO Auto-generated method stub
            Toast.makeText(MainActivity.this, "disconn successful", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            Toast.makeText(MainActivity.this,"conn successful" ,Toast.LENGTH_LONG).show();
            if (!isAccessibilitySettingsOn(getApplicationContext())) {
                DLog.e("HaiChecker", "服务未开启，请开启");
                showRtspConnectedDialog();
            }else{
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void showRtspConnectedDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this,android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                .setTitle(getString(R.string.app_name))
                .setMessage(getString(R.string.message))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    }
                }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                })
                .create();
        dialog.show();
    }

    private void init(){
        //开启、停止 服务按钮
        startService = (Button) findViewById(R.id.startService);
        startService.setTag("starting");
        //设置单击事件
        startService.setOnClickListener(this);

        sendBtn = (Button) findViewById(R.id.send);
        sendBtn.setOnClickListener(this);

        number = (TextView) findViewById(R.id.number);
        listView = (ListView) findViewById(R.id.listView);
        data = new ArrayList<>();
        adapter = new MainAdapter(this,data);
        listView.setAdapter(adapter);
        binServer();
    }



    private void binServer(){
      Intent i =new Intent(this, NotificationService.class);
        bindService(i,conn,BIND_AUTO_CREATE);
    }
    private void undServer(){
        Intent i =new Intent(this, NotificationService.class);
        unbindService(conn);
    }

    @Override
    protected void onResume() {
        if (!isAccessibilitySettingsOn(getApplicationContext())) {
            DLog.e("HaiChecker", "服务未开启，请开启");
            showRtspConnectedDialog();
        }
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startService:
                DbUtils d = DbUtils.create(this);
                try {
                    List<Page> data = d.findAll(Page.class);
                    double num = 0.0f;
                    for (Page da:
                    data) {
                        try {
                            double n = Double.parseDouble(da.getNumber());
                            num = num + n;
                        }catch (Exception e){
                            DLog.e("HaiChecker",e.getMessage());
                            continue;
                        }
                    }
                    adapter.updateData(data);
                    number.setText(num+"");


                } catch (DbException e) {
                    e.printStackTrace();
                }
//                if (v.getTag().toString().equals("starting")){
//                    binServer();
//                    startService.setText("停止服务");
//                    Log.d("MainActivity","已开启服务...");
//                    v.setTag("stoping");
//                }else{
//                    undServer();
//                    startService.setText("开启服务");
//                    v.setTag("starting");
//                    Log.d("MainActivity", "已关闭服务...");
//                }
                break;
            case R.id.send:
                sendNotification();
                break;
        }
    }
    private void sendNotification(){
        NotificationManager notificationManager;
        Notification.Builder builder = new Notification.Builder(this);
        Intent mIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.baidu.com/"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mIntent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setAutoCancel(true);
        builder.setContentTitle("普通通知");
        builder.setContentInfo("setContentInfo");
        builder.setContentText("setContentText");
        builder.setTicker("Test,Test,Test,Test,Test,Test,Test,Test");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(new Random(100).nextInt(), builder.build());
    }
}
