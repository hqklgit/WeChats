package shiwenping.com.wechats;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import shiwenping.com.utils.DLog;
import shiwenping.com.utils.Utils;

public class NotificationService extends AccessibilityService {
    private boolean isClean = false;
    private boolean isGun = false;
    private boolean isClickClean = false;
    private String groupName;
    private String number;
    private String name;
    private String className;
    @Override
    public void onDestroy() {
        DLog.d("HaiChecker","onDestroy");
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        Log.d("HaiChecker", "onCreate");

        super.onCreate();
    }
    private String TAG = "HaiChecker";
    // To check if service is enabled


    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("HaiChecker", "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("HaiChecker","onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        Log.d("HaiChecker",event.getPackageName().toString());
        int eventType = event.getEventType();
        String className = event.getClassName().toString();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:// 通知栏事件
                List<CharSequence> texts = event.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        String content = text.toString();
                        Log.d("HaiChecker",content);
                        if (content.contains("[微信红包]")) {
                            // 监听到微信红包的notification，打开通知
                            if (event.getParcelableData() != null
                                    && event.getParcelableData() instanceof Notification) {
                                Notification notification = (Notification) event
                                        .getParcelableData();
                                PendingIntent pendingIntent = notification.contentIntent;
                                try {
                                    pendingIntent.send();
                                } catch (PendingIntent.CanceledException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                Log.d("HaiChecker"," TYPE_WINDOW_CONTENT_CHANGED:"+className);

                if (isClean) {
                    DLog.d("HaiChecker", " 检测到窗口变化:" + event.getClassName().toString());
                    if (className.equals("com.tencent.mm.plugin.chatroom.ui.ChatroomInfoUI")){
                        cleanChat(true);
                    }else if (className.equals("com.tencent.mm.ui.LauncherUI")){
                        cleanChat(false);
                    }
                    return;
                }

                if (className.equals("android.widget.ListView") || className.equals("android.widget.RelativeLayout"))
                {
                    getPacket();
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                this.className = className;
                if (isClickClean)
                {
                    if (className.equals("com.tencent.mm.ui.base.h")){
                        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                        List<AccessibilityNodeInfo> cleanText = rootNode.findAccessibilityNodeInfosByText("确定删除群的聊天记录吗？");
                        if (cleanText.size() > 0){
                            List<AccessibilityNodeInfo> cleanBtn = rootNode.findAccessibilityNodeInfosByText("清空");
                            if (cleanBtn.size() > 0){
                                cleanBtn.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                isClickClean = false;
                            }
                        }
                    }
                }

                if (isClean == false && isGun == false && isClickClean == false && className.equals("com.tencent.mm.plugin.chatroom.ui.ChatroomInfoUI")){
                    if ((groupName != null && !groupName.trim().equals("")) && (number != null && !number.trim().equals("")))
                    {
                        DbUtils d = DbUtils.create(this);
                        Page p = new Page();
                        p.setName(name);
                        p.setWegroup(groupName);
                        p.setNumber(number);
                        try {
                            double a = Double.parseDouble(number);
                            if (name.contains("的红包") ) {
                                d.save(p);
                            }else{
                                return;
                            }
                        } catch (Exception e) {
                            DLog.d("HaiChecker","保存信息失败:"+e.getMessage());
                            e.printStackTrace();
                        }

                        groupName = null;
                        number = null;
                        name = null;
                    }
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    return;
                }

                if (isClean) {
                    DLog.d("HaiChecker", " 检测到窗口变化 STATE_CHANGED:" + event.getClassName().toString());
                    if (className.equals("com.tencent.mm.plugin.chatroom.ui.ChatroomInfoUI")){
                        cleanChat(true);
                    }else if (className.equals("com.tencent.mm.ui.LauncherUI")){
                        cleanChat(false);
                    }
                    return;
                }

                if (className.equals("com.tencent.mm.ui.LauncherUI")) {
                    getPacket();// 领取红包
                } else if (className
                        .equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")) {
                    DLog.d("HaiChecker","进入到打开红包界面");
                    openPacket();// 打开红包
                }

                try {
                    checkUI();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }

                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                DLog.d("HaiChecker","监听到滚动消息\n" +
                        className);
                if (className.equals("android.widget.ListView") && isGun){
                    cleanChat(true);
                }
                break;
        }
    }

    private void checkUI() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        try {

            if (nodeInfo == null)
                return;

        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("红包详情");
        List<AccessibilityNodeInfo> infoNo = nodeInfo.findAccessibilityNodeInfosByText("返回");
        List<AccessibilityNodeInfo> yuans = nodeInfo.findAccessibilityNodeInfosByText("元");
        if (yuans.size() > 0){
            name = yuans.get(0).getParent().getChild(0).getText().toString();
            DLog.d("HaiChecker","发红包的人:"+name);
        }

        List<AccessibilityNodeInfo> yuan = nodeInfo.findAccessibilityNodeInfosByText(".");
        if (yuan.size() > 0){
            number = yuan.get(0).getText().toString();
            DLog.d("HaiChecker","获取到一个红包,金额："+number);
        }


        if (list.size() > 0 && infoNo.size() > 0)
        {
            DLog.d("HaiChecker", "进入红包详情");
//            infoNo.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            isClean = true;
        }else{

        }

        }catch (NullPointerException e){

        }


    }
    private String getCurrentActivityName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);


        // get the info from the currently running task
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);


        ComponentName componentInfo = taskInfo.get(0).topActivity;
        return componentInfo.getClassName();
    }
    private void readResult(){

    }
    private void  cleanChat(boolean isDetail){
        if (isClean){
            AccessibilityNodeInfo clean = getRootInActiveWindow();
            if (!isDetail){
                List<AccessibilityNodeInfo> cleanList = clean.findAccessibilityNodeInfosByText("聊天信息");
                if (cleanList.size() > 0){
                    cleanList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    isDetail = true;
                }
            }else{
                List<AccessibilityNodeInfo> group = clean.findAccessibilityNodeInfosByText("(");
                List<AccessibilityNodeInfo> fridens = clean.findAccessibilityNodeInfosByText("清空聊天记录");
                List<AccessibilityNodeInfo> fridens_or1 = clean.findAccessibilityNodeInfosByText("聊天信息");
                if (fridens.size() > 0){
                    DLog.d("HaiChecker","获取到清空按钮，开始单击");
                    isGun = false;
                    isClickClean = true;
                    isClean = false;
                    fridens.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }else{
                    List<AccessibilityNodeInfo> listView = clean.findAccessibilityNodeInfosByViewId("android:id/list");
                    List<AccessibilityNodeInfo> groupLust = clean.findAccessibilityNodeInfosByViewId("android:id/summary");
                    if (groupLust.size() > 0){

                        groupName = groupLust.get(0).getText().toString();
                    }
                    if (listView.size() > 0){
                        DLog.d("HaiChecker","获取到ListView,开始滚动");
                        listView.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                        isGun = true;
                    }
                }
            }
        }
    }
    private void openPacket() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo
                    .findAccessibilityNodeInfosByText("红包");



            for (AccessibilityNodeInfo n : list) {

                List<AccessibilityNodeInfo> infoNo = n.findAccessibilityNodeInfosByText("手慢了");
                if (infoNo.size() > 0){
                    Log.d("HaiChecker", "手慢了数量：" + infoNo.size() + "");
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    isClean = true;
                    cleanChat(false);
                    break;
                }
                Log.d("HaiChecker", "n Page = " + n.getClassName().toString());
                AccessibilityNodeInfo n1 = n.getParent();
                if (n1==null) {
                    Log.d("HaiChecker","n1==null");
                    continue;
                }else{
                    Log.d("HaiChecker","n1=="+n1.getClassName().toString());
                }
                for (int i = 0;i < n1.getChildCount();i++){
                  AccessibilityNodeInfo view = n1.getChild(i);
                    Log.d("HaiChecker","获取按钮包名:"+view.getPackageName().toString());
                    if (view.getClassName().toString().contains("Button")){
                        view.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }else{
                        continue;
                    }
                }

            }
        }
    }

    private void getPacket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            List<AccessibilityNodeInfo> nodeInfos = rootNode
                    .findAccessibilityNodeInfosByText("领取红包");

            Collections.reverse(nodeInfos);

            for (AccessibilityNodeInfo nodeInfo : nodeInfos) {
                Log.d("HaiChecker",String.format("文本:%s  Class:%s",nodeInfo.getText().toString(),nodeInfo.getClassName().toString()));
                AccessibilityNodeInfo info = nodeInfo.getParent();
                if (info != null){
                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                break;
            }
        }
    }

    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();
    }

    @Override
    public void onInterrupt() {
        Log.d("NotificationService", "onInterrupt...");
    }



}
