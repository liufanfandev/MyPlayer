package com.coding.myplayer.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.coding.myplayer.util.Constant;
import com.lidroid.xutils.DbUtils;

/**
 * Created by user on 2016/10/26.
 */

public class MyPlayerApp extends Application {
    public SharedPreferences sp;
    public static DbUtils dbUtils;
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        dbUtils = DbUtils.create(getApplicationContext(),Constant.DB_NAME);
        context = getApplicationContext();
    }
}
