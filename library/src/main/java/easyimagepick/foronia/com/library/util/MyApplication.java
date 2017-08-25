package easyimagepick.foronia.com.library.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Foronia on 2016/5/25.
 */
public class MyApplication extends Application {
    private static MyApplication instance;
    private static Context mContext;
    private List<Activity> activityList = new LinkedList<Activity>();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mContext = getApplicationContext();

    }

    // 添加Activity到容器中
    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    // 遍历所有Activity并finish
    public void exit() {
        for (Activity activity : activityList) {
            activity.finish();
        }
//        System.exit(0);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
    public static Context getContext(){
        return mContext;
    }


    public static MyApplication getInstance(){
        return instance;
    }
}
