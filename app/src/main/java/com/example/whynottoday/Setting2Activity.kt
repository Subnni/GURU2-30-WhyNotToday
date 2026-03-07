package com.example.whynottoday

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Setting2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting2)

        //공통 UI(헤더 및 네비게이션 바) 초기화
        val commonUIHandler = CommonUIHandler()
        commonUIHandler.setupListener(this)
    }
}