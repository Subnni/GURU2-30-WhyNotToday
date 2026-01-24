package com.example.whynottoday

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream

class DBManager (context: Context,
                 name: String?,
                 factory: SQLiteDatabase.CursorFactory?,
                 version: Int
) :  SQLiteOpenHelper(context, name, factory, version){

    val context = context
    fun settingDB(){

        val name = "WhyNotTodayDB.db"
        val path = context.getDatabasePath(name)
        path.parentFile?.mkdirs()
        try {
            context.assets.open("databases/$name").use { input ->
                FileOutputStream(path).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DB_ERROR", "${e.message}")
        }

    }
    override fun onCreate(db: SQLiteDatabase?) {    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {    }

}