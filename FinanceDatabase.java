package com.example.financemanager;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// ✅ Version 1 from to 2
@Database(entities = {FinanceTable.class}, version = 2)
public abstract class FinanceDatabase extends RoomDatabase {
    public abstract FinanceDao getDao();
    public static volatile FinanceDatabase INSTANCE;

    public static FinanceDatabase getInstance(Context context){
        if (INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context,FinanceDatabase.class, "finance")
                    .allowMainThreadQueries()

                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}