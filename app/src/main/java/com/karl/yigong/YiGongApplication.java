package com.karl.yigong;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import cn.jpush.android.api.JPushInterface;

public class YiGongApplication extends Application {

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        context=this;
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
    }


}
