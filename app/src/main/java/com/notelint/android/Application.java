package com.notelint.android;

import com.notelint.android.database.Database;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class Application extends android.app.Application {
    private static Application instance;

    public static Application getInstance() {
        if (instance == null) {
            throw new NullPointerException("App is not init");
        }

        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // Initialize Realm
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(Database.DATABASE_NAME + ".realm")
                .schemaVersion(Database.VERSION)
                .build();
        Realm.setDefaultConfiguration(config);
    }


}
