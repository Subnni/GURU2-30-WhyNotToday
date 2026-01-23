package com.example.whynottoday

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBManager(
    context: Context,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {

    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)
        // 외래 키 활성화 (이게 있어야 CASCADE 삭제가 작동함)
        db?.execSQL("PRAGMA foreign_keys = ON;")
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // 1. todo 테이블 생성 (ERD 기준)
        db!!.execSQL("""
            CREATE TABLE todo (
                todo_id INTEGER PRIMARY KEY AUTOINCREMENT,
                is_important INTEGER NOT NULL,
                todo_name TEXT NOT NULL,
                date_time TEXT NOT NULL,
                is_done INTEGER NOT NULL
            )
        """)

        // 2. excuse 테이블 생성 (todo_id 외래 키 연결)
        db.execSQL("""
            CREATE TABLE excuse (
                excuse_id INTEGER PRIMARY KEY AUTOINCREMENT,
                todo_id INTEGER NOT NULL,
                excuse_reason TEXT NOT NULL,
                FOREIGN KEY (todo_id) REFERENCES todo (todo_id) ON DELETE CASCADE
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS excuse")
        db.execSQL("DROP TABLE IF EXISTS todo")
        onCreate(db)
    }
}