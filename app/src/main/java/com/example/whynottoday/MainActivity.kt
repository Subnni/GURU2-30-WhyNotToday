package com.example.whynottoday

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //앱 시작 시 HomeFragment 표시
        if (savedInstanceState == null) { // 중복 생성 방지
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }

        //공통 UI(헤더 및 네비게이션 바) 초기화
        val commonUIHandler = CommonUIHandler()
        commonUIHandler.setupListener(this)
    }
}