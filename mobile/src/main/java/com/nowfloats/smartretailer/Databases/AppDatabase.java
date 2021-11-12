package com.nowfloats.smartretailer.Databases;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by NowFloats on 11-01-2017.
 */
@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {
    public static final String NAME = "RetailDatabase";

    public static final int VERSION = 1;
}
