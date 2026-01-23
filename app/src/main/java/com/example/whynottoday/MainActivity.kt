package com.example.whynottoday

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    // 위젯 변수
    lateinit var tvDate: TextView
    lateinit var btnAdd: LinearLayout

    // DB 관련 변수
    lateinit var dbManager: DBManager
    lateinit var sqlDB: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. 위젯 연결 (XML의 실제 ID 적용)
        tvDate = findViewById(R.id.ru570fatagaa) // 01.15(목)
        btnAdd = findViewById(R.id.rszeiw43jhxf) // 추가하기 레이아웃

        // 2. DBManager 초기화 (데이터베이스 이름: whynottodayDB)
        dbManager = DBManager(this, "whynottodayDB", null, 1)

        // 3. 앱 실행 시 DB를 한 번 열어서 databases 폴더 생성을 유도함
        sqlDB = dbManager.writableDatabase
        sqlDB.close()

        // 4. 추가하기 버튼 클릭 시 화면 전환
        btnAdd.setOnClickListener {
            val intent = Intent(this, AddTodoActivity::class.java)
            startActivity(intent)
        }

        // 데이터 불러오기 함수 (나중에 구현)
        // loadData()
    }
}