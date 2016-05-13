package org.projects.shoppinglist;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by Balazs on 2016. 05. 10..
 */
public class MyShopApp extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);

    }
}
