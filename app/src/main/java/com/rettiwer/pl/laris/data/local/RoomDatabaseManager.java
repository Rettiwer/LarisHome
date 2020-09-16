package com.rettiwer.pl.laris.data.local;

import android.content.Context;

import com.rettiwer.pl.laris.data.local.room.RoomDao;
import com.rettiwer.pl.laris.data.remote.api.room.Room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Room.class}, version = 1)
public abstract class RoomDatabaseManager extends RoomDatabase {
    private static volatile RoomDatabaseManager INSTANCE;

    public abstract RoomDao roomDao();

    public static RoomDatabaseManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (RoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = androidx.room.Room.databaseBuilder(context.getApplicationContext(),
                            RoomDatabaseManager.class, "rooms.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
