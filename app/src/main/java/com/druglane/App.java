package com.druglane;

import android.app.Activity;
import android.app.Application;
import android.app.Application;
import android.os.Bundle;

public class App extends Application implements Application.ActivityLifecycleCallbacks {
    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;
    public static boolean inForeground = false;

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            inForeground = true;
            System.out.println("appn in foreground");
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            inForeground = false;
            System.out.println("app is in background");
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }



    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }
}
