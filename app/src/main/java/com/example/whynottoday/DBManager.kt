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
        // 1. 테이블 이름을 todoTBL로 수정함.
        db!!.execSQL("""
            CREATE TABLE todoTBL (
                todo_id INTEGER PRIMARY KEY AUTOINCREMENT,
                is_important INTEGER NOT NULL,
                todo_name TEXT NOT NULL,
                date_time TEXT NOT NULL,
                is_done INTEGER NOT NULL
            )
        """)

        // 2. 테이블 이름을 excuseTBL로 수정하고 외래 키 참조 대상도 todoTBL로 변경함.
        db.execSQL("""
            CREATE TABLE excuseTBL (
                excuse_id INTEGER PRIMARY KEY AUTOINCREMENT,
                todo_id INTEGER NOT NULL,
                excuse_reason TEXT NOT NULL,
                FOREIGN KEY (todo_id) REFERENCES todoTBL (todo_id) ON DELETE CASCADE
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // 기존 테이블 삭제 시에도 수정된 이름을 적용함.
        db!!.execSQL("DROP TABLE IF EXISTS excuseTBL")
        db.execSQL("DROP TABLE IF EXISTS todoTBL")
        onCreate(db)
    }
}