package com.example.whynottoday

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddExcuseActivity : AppCompatActivity() {

    private lateinit var tvTodoTitle: TextView
    private lateinit var tvTodoTime: TextView
    private lateinit var etExcuse: EditText
    private lateinit var btnClose: ImageView
    private lateinit var btnDelete: LinearLayout
    private lateinit var btnComplete: LinearLayout

    private var todoId: Long = -1L   // 어떤 할 일의 핑계인지 식별용
    private lateinit var dbManager: DBManager
    private var excuseText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_excuse)

        // DB
        dbManager = DBManager(this, "WhyNotTodayDB.db", null, 1)

        // 뷰 연결
        tvTodoTitle = findViewById(R.id.tvTodoTitle)
        tvTodoTime = findViewById(R.id.tvTodoTime)
        etExcuse = findViewById(R.id.etExcuse)
        btnClose = findViewById(R.id.btnClose)
        btnDelete = findViewById(R.id.btnDelete)
        btnComplete = findViewById(R.id.btnComplete)

        // 1) todoId 받기 - 두 가지 키 다 시도
        todoId = intent.getLongExtra("todoId", -1L)
        if (todoId == -1L) {
            val intId = intent.getIntExtra("TODO_ID", -1)
            if (intId != -1) {
                todoId = intId.toLong()
            }
        }

        // 2) 제목 / 시간 / 기존 핑계 받기
        var todoTitle = intent.getStringExtra("todoTitle")
        var todoTime = intent.getStringExtra("todoTime")
        val currentExcuse = intent.getStringExtra("excuse") ?: ""

        // 3) 제목이나 시간이 비어 있으면 DB에서 가져오기
        // 2-1) 인텐트로 핑계가 안 왔으면 DB에서 가져오기
        if (excuseText.isBlank() && todoId != -1L) {
            try {
                val db = dbManager.readableDatabase

                // ★ rawQuery는 (sql문, selectionArgs) 이렇게 두 개 인자만 씀
                val cursor = db.rawQuery(
                    "SELECT excuse_reason FROM excuseTBL WHERE todo_id = ?",
                    arrayOf(todoId.toString())
                )

                if (cursor.moveToFirst()) {
                    // 첫 번째 컬럼(index 0)에 있는 문자열 꺼내기
                    excuseText = cursor.getString(0)
                }

                cursor.close()
                db.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        if (todoId != -1L && (todoTitle.isNullOrBlank() || todoTime.isNullOrBlank())) {
            try {
                val db = dbManager.readableDatabase
                val cursor = db.rawQuery(
                    "SELECT todo_name, date_time, is_important FROM todoTBL WHERE todo_id = ?",
                    arrayOf(todoId.toString())
                )

                if (cursor.moveToFirst()) {
                    todoTitle = cursor.getString(0)
                    val dateTime = cursor.getString(1) // yyyy-MM-dd HH:mm:ss
                    val timePart = dateTime.split(" ")[1].substring(0, 5) // HH:mm
                    todoTime = timePart
                }

                cursor.close()
                db.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 4) UI에 표시
        tvTodoTitle.text = todoTitle ?: ""
        tvTodoTime.text = todoTime ?: ""
        if (excuseText.isNotBlank()) {
            etExcuse.setText(excuseText)
        }


        // X 버튼: 아무 변경 없이 닫기
        btnClose.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        // 삭제 버튼: DB에서 삭제 + 결과 전달
        btnDelete.setOnClickListener {
            if (todoId == -1L) {
                Toast.makeText(this, "할 일 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = dbManager.writableDatabase
            db.execSQL("DELETE FROM excuseTBL WHERE todo_id = ?", arrayOf(todoId.toString()))
            db.close()

            val resultIntent = Intent().apply {
                putExtra("todoId", todoId)
                putExtra("excuse", "")
                putExtra("excuseDeleted", true)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        // 완료 버튼: DB에 INSERT or UPDATE + 결과 전달
        btnComplete.setOnClickListener {
            if (todoId == -1L) {
                Toast.makeText(this, "할 일 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val excuseText = etExcuse.text.toString().trim()
            if (excuseText.isEmpty()) {
                Toast.makeText(this, "핑계를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = dbManager.writableDatabase

            val cursor = db.rawQuery(
                "SELECT excuse_id FROM excuseTBL WHERE todo_id = ?",
                arrayOf(todoId.toString())
            )

            if (cursor.moveToFirst()) {
                db.execSQL(
                    "UPDATE excuseTBL SET excuse_reason = ? WHERE todo_id = ?",
                    arrayOf(excuseText, todoId.toString())
                )
            } else {
                db.execSQL(
                    "INSERT INTO excuseTBL (todo_id, excuse_reason) VALUES (?, ?)",
                    arrayOf(todoId.toString(), excuseText)
                )
            }

            cursor.close()
            db.close()

            val resultIntent = Intent().apply {
                putExtra("todoId", todoId)
                putExtra("excuse", excuseText)
                putExtra("excuseDeleted", false)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
