package com.mandu.callcard;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class App extends Application {

    private static SharedPreferences sp;

    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences("callcard", MODE_PRIVATE);
    }

    public static SharedPreferences p(Context c) {
        if (sp == null) {
            sp = c.getApplicationContext().getSharedPreferences("callcard", Context.MODE_PRIVATE);
        }
        return sp;
    }
}
