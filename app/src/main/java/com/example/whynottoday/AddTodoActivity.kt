package com.example.whynottoday

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AddTodoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 이 코드가 있어야 activity_add_todo.xml 화면이 나타남. 핵심.
        setContentView(R.layout.activity_add_todo)

        // 여기에 할 일 저장 버튼 리스너 등을 작성하면 됨.
    }
}