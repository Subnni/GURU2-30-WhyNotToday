package com.example.whynottoday

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import java.util.*

class AddTodoActivity : AppCompatActivity() {
    private lateinit var edtTodo: EditText
    private lateinit var btnImportant: LinearLayout
    private lateinit var btnGeneral: LinearLayout
    private lateinit var btnAmPm: LinearLayout
    private lateinit var tvAmPm: TextView
    private lateinit var edtHour: EditText
    private lateinit var edtMinute: EditText
    private lateinit var btnSave: LinearLayout
    private lateinit var btnDelete: LinearLayout
    private lateinit var ivBack: ImageView
    private lateinit var tvTitle: TextView // "할 일 추가" 또는 "할 일 수정"

    private lateinit var dbManager: DBManager
    private lateinit var sqlDB: SQLiteDatabase

    private var paperFont: Typeface? = null
    private var isImportant: Int = 1
    private var selectedDate: String? = ""
    private var todoId: Int = -1 // 💡 수정 모드 판별을 위한 ID 저장 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_todo)

        paperFont = ResourcesCompat.getFont(this, R.font.paperlogy_medium)
        dbManager = DBManager(this, "WhyNotTodayDB.db", null, 5)

        // 위젯 연결
        tvTitle = findViewById(R.id.rbfovzr19n7)
        edtTodo = findViewById(R.id.rnssuh3hxzu)
        btnImportant = findViewById(R.id.r2x5e1dwd9lc)
        btnGeneral = findViewById(R.id.rf94kuofpsb)
        btnAmPm = findViewById(R.id.rehavnku56k)
        tvAmPm = findViewById(R.id.ru11nqmtmoas)
        edtHour = findViewById(R.id.rcubb5q6pz1)
        edtMinute = findViewById(R.id.r68a05rvfbvl)
        btnSave = findViewById(R.id.rhq2iizm7imc)
        btnDelete = findViewById(R.id.rkc4qxc3826i)
        ivBack = findViewById(R.id.rdkgwl6s3cw9)

        applyGlobalFont()

        // 💡 Intent 데이터 수신 (날짜 또는 수정용 ID)
        selectedDate = intent.getStringExtra("selectedDate")
        todoId = intent.getIntExtra("TODO_ID", -1)

        if (todoId != -1) {
            // 💡 [수정 모드] 기존 데이터 불러오기
            tvTitle.text = "할 일 수정"
            loadExistingTodo(todoId)
        } else {
            // [추가 모드] 기본 설정
            tvTitle.text = "할 일 추가"
            edtHour.hint = "0"; edtMinute.hint = "00"
        }

        // 오전/오후 토글
        btnAmPm.setOnClickListener {
            toggleAmPm(tvAmPm.text.toString() == "오전" )
            updateSaveButtonState()
        }

        // 중요도 선택
        btnImportant.setOnClickListener { setImportance(1) }
        btnGeneral.setOnClickListener { setImportance(0) }

        // 실시간 감지
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { updateSaveButtonState() }
            override fun afterTextChanged(s: Editable?) {}
        }
        edtTodo.addTextChangedListener(watcher)
        edtHour.addTextChangedListener(watcher)
        edtMinute.addTextChangedListener(watcher)

        updateSaveButtonState()

        // 완료 버튼 (저장 또는 업데이트)
        btnSave.setOnClickListener { saveOrUpdateTodo() }

        // 💡 [삭제 기능] 수정 모드일 때 실제 삭제 수행
        btnDelete.setOnClickListener {
            if (todoId != -1) deleteTodo(todoId)
            else finish() // 추가 모드에선 그냥 닫기
        }

        ivBack.setOnClickListener { finish() }
    }

    // 💡 기존 데이터 로드 함수
    private fun loadExistingTodo(id: Int) {
        try {
            sqlDB = dbManager.readableDatabase
            val cursor: Cursor = sqlDB.rawQuery("SELECT * FROM todoTBL WHERE todo_id = $id", null)
            if (cursor.moveToFirst()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("todo_name"))
                val important = cursor.getInt(cursor.getColumnIndexOrThrow("is_important"))
                val dateTime = cursor.getString(cursor.getColumnIndexOrThrow("date_time")) // yyyy-MM-dd HH:mm:ss

                edtTodo.setText(name)
                setImportance(important)

                // 시간 데이터 파싱 (13:00 -> 오후 1:00)
                val timePart = dateTime.split(" ")[1] // HH:mm:ss
                val hour24 = timePart.split(":")[0].toInt()
                val minute = timePart.split(":")[1]

                if (hour24 >= 12) {
                    toggleAmPm(false) // 오후
                    edtHour.setText(if (hour24 > 12) (hour24 - 12).toString() else "12")
                } else {
                    toggleAmPm(true) // 오전
                    edtHour.setText(if (hour24 == 0) "12" else hour24.toString())
                }
                edtMinute.setText(minute)

                // 날짜 정보 유지
                selectedDate = dateTime.split(" ")[0]
            }
            cursor.close()
            sqlDB.close()
        } catch (e: Exception) { Log.e("SQL_ERROR", "로드 실패: ${e.message}") }
    }

    private fun setImportance(important: Int) {
        isImportant = important
        if (important == 1) {
            btnImportant.setBackgroundResource(R.drawable.s7280ffsw2cr24b7280ff33)
            btnGeneral.setBackgroundResource(R.drawable.cr24beeeeee)
        } else {
            btnImportant.setBackgroundResource(R.drawable.cr24beeeeee)
            btnGeneral.setBackgroundResource(R.drawable.s7280ffsw2cr24b7280ff33)
        }
        updateSaveButtonState()
    }

    private fun toggleAmPm(isAm: Boolean) {
        if (!isAm) {
            tvAmPm.text = "오후"
            btnAmPm.background.setTint(Color.parseColor("#D4D4D4"))
        } else {
            tvAmPm.text = "오전"
            btnAmPm.background.setTintList(null)
        }
    }

    private fun saveOrUpdateTodo() {
        val name = edtTodo.text.toString().trim()
        var h = edtHour.text.toString().toInt()
        val m = edtMinute.text.toString().padStart(2, '0')

        if (tvAmPm.text == "오후" && h < 12) h += 12
        else if (tvAmPm.text == "오전" && h == 12) h = 0

        val fullTime = "$selectedDate ${h.toString().padStart(2, '0')}:$m:00"

        try {
            sqlDB = dbManager.writableDatabase
            if (todoId == -1) {
                // 💡 추가 (INSERT)
                sqlDB.execSQL("INSERT INTO todoTBL (is_important, todo_name, date_time, is_done) VALUES ($isImportant, '$name', '$fullTime', 0)")
            } else {
                // 💡 수정 (UPDATE)
                sqlDB.execSQL("UPDATE todoTBL SET is_important=$isImportant, todo_name='$name', date_time='$fullTime' WHERE todo_id=$todoId")
            }
            sqlDB.close()
            finish()
        } catch (e: Exception) { Toast.makeText(this, "저장 실패", Toast.LENGTH_SHORT).show() }
    }

    // 💡 삭제 함수
    private fun deleteTodo(id: Int) {
        try {
            sqlDB = dbManager.writableDatabase
            sqlDB.execSQL("DELETE FROM todoTBL WHERE todo_id = $id")
            sqlDB.close()
            Toast.makeText(this, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            Log.e("SQL_ERROR", "삭제 실패: ${e.message}") }
    }

    private fun applyGlobalFont() {
        val textViews = listOf(tvTitle, findViewById<TextView>(R.id.r6jehds0ft7), edtTodo,
            findViewById<TextView>(R.id.rg8jbp1vvfq), findViewById<TextView>(R.id.r5u2i8xogh5x),
            findViewById<TextView>(R.id.r0z5ebi04adah), findViewById<TextView>(R.id.rp7p8of08czm),
            tvAmPm, edtHour, findViewById<TextView>(R.id.rfdb9m3k303b), edtMinute,
            findViewById<TextView>(R.id.rclpptr0jwel), findViewById<TextView>(R.id.roycse14q1zj),
            findViewById<TextView>(R.id.r73nmubdgpy))
        textViews.forEach { it?.typeface = paperFont }
    }

    private fun updateSaveButtonState() {
        val h = edtHour.text.toString().toIntOrNull() ?: -1
        val m = edtMinute.text.toString().toIntOrNull() ?: -1
        val isTimeValid = (h in 1..12) && (m in 0..59)
        val isInputEmpty = edtTodo.text.toString().trim().isEmpty() || edtHour.text.isEmpty() || edtMinute.text.isEmpty()

        btnSave.isEnabled = !isInputEmpty && isTimeValid
        btnSave.alpha = if (btnSave.isEnabled) 1.0f else 0.5f
    }
}