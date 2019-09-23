package com.karl.yigong;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.karl.yigong.beans.PushSMSArgs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import cn.jpush.android.api.JPushInterface;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * 自定义接收器
 * <p>
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
public class MyReceiver extends BroadcastReceiver {

    private static final String TAG = "tag";

    private Gson gson;

    public MyReceiver(){
         gson=new Gson();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Bundle bundle = intent.getExtras();
            Logger.d(TAG, "[MyReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));

            if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
                String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
                Logger.e(TAG, "[MyReceiver] 接收Registration Id : " + regId);

            } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
                String content = bundle.getString(JPushInterface.EXTRA_MESSAGE);
                Logger.d(TAG, "[MyReceiver] 接收到推送下来的自定义消息: " + content);
                //processCustomMessage(context, bundle);

                String phoneNum = bundle.getString(JPushInterface.EXTRA_EXTRA);
                PushSMSArgs args=  gson.fromJson(phoneNum,PushSMSArgs.class);
                Logger.d(TAG, "[MyReceiver] 接收到推送下来的消息发送号码: " + args.getPhone());
                //发送短信
                sendSMS(args.getPhone(), content);


            } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
                Logger.d(TAG, "[MyReceiver] 接收到推送下来的通知");
                int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
                //bundle.get(JPushInterface.EXTRA_ALERT);推送内容
                Logger.d(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);

                //获取到的推送的通知类型
                String str_Test = bundle.getString(JPushInterface.EXTRA_ALERT);
                Log.e("tag", "测试：" + str_Test);

                //在这里自定义通知声音
                processCustomMessage(context, bundle);

                //这里通过EventBus来想我需要更新数据的界面发送更新通知
//                EventBus.getDefault().postSticky(new MessageEvent(str_Test));

            } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
                Logger.d(TAG, "[MyReceiver] 用户点击打开了通知");


            } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
                Logger.d(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
                //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..

            } else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
                boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
                Logger.w(TAG, "[MyReceiver]" + intent.getAction() + " connected state change to " + connected);
            } else if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
                int resultCode = getResultCode();
                if (resultCode == Activity.RESULT_OK) {
                    System.out.println("发送短信Success");
                } else {
                    System.out.println("发送短信失败");
                }
            } else {
                Logger.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
            }
        } catch (Exception e) {

        }
    }

    /**
     * 发送消息
     * 需要请求相关发短信权限
     *
     * @param phoneNum 电话号码
     * @param content  信息内容
     */
    private void sendSMS(String phoneNum, String content) {

        String number = phoneNum;

        try {
            if (TextUtils.isEmpty(number)) {
                showToast("请输入手机号");
                return;
            }
            if (TextUtils.isEmpty(content)) {
                showToast("请输入内容");
                return;
            }
            ArrayList<String> messages = SmsManager.getDefault().divideMessage(content);
            for (String text : messages) {
                SmsManager.getDefault().sendTextMessage(number, null, text, null, null);
            }
            Log.d("MainActivity", "1");
            showToast("短信发送中....");
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void showToast(String tip) {
        Toast.makeText(YiGongApplication.context, tip, Toast.LENGTH_LONG).show();
    }


    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            } else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
            } else if (key.equals(JPushInterface.EXTRA_EXTRA)) {
                if (TextUtils.isEmpty(bundle.getString(JPushInterface.EXTRA_EXTRA))) {
                    Logger.i(TAG, "This message has no Extra data");
                    continue;
                }

                try {
                    JSONObject json = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
                    Iterator<String> it = json.keys();

                    while (it.hasNext()) {
                        String myKey = it.next();
                        sb.append("\nkey:" + key + ", value: [" +
                                myKey + " - " + json.optString(myKey) + "]");
                    }
                } catch (JSONException e) {
                    Logger.e(TAG, "Get message extra JSON error!");
                }

            } else {
                sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
            }
        }
        return sb.toString();
    }


    /**
     * 自定义推送的声音
     *
     * @param context
     * @param bundle
     */
    private void processCustomMessage(Context context, Bundle bundle) {

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
        //这一步必须要有而且setSmallIcon也必须要，没有就会设置自定义声音不成功
        notification.setAutoCancel(true).setSmallIcon(R.mipmap.ic_launcher);
        String alert = bundle.getString(JPushInterface.EXTRA_ALERT);
        if (alert != null && !alert.equals("")) {
            notification.setSound(
                    Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.weixin));
        }

        //最后刷新notification是必须的
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification.build());

    }
}
