package com.example.whynottoday

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var tvDate: TextView
    lateinit var tvDay: TextView
    lateinit var btnAdd: LinearLayout
    lateinit var containerImportant: LinearLayout
    lateinit var containerGeneral: LinearLayout
    lateinit var ivPrev: ImageView
    lateinit var ivNext: ImageView

    lateinit var dbManager: DBManager
    lateinit var sqlDB: SQLiteDatabase
    private var paperFont: Typeface? = null
    private var currentCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        paperFont = ResourcesCompat.getFont(this, R.font.paperlogy_medium)

        tvDate = findViewById(R.id.rkm9dvygs3k)
        tvDay = findViewById(R.id.rroir2r69w0b)
        btnAdd = findViewById(R.id.rszeiw43jhxf)
        containerImportant = findViewById(R.id.rawakuad9k4)
        containerGeneral = findViewById(R.id.rc2qf6n0zmml)
        ivPrev = findViewById(R.id.r8xx1vippkv9)
        ivNext = findViewById(R.id.r8ajnwmbi7si)

        // 섹션 타이틀 포함 폰트 적용
        val staticTexts = listOf(tvDate, tvDay, findViewById<TextView>(R.id.rrp7w0ojci6s),
            findViewById<TextView>(R.id.rn7lao1l6yv8), findViewById<TextView>(R.id.rapw2kdupbc8))
        staticTexts.forEach { it.typeface = paperFont }

        dbManager = DBManager(this, "WhyNotTodayDB.db", null, 5)
        updateScreenByDate()

        ivPrev.setOnClickListener { currentCalendar.add(Calendar.DAY_OF_YEAR, -1); updateScreenByDate() }
        ivNext.setOnClickListener { currentCalendar.add(Calendar.DAY_OF_YEAR, 1); updateScreenByDate() }
        btnAdd.setOnClickListener {
            val intent = Intent(this, AddTodoActivity::class.java)
            val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            intent.putExtra("selectedDate", dbFormat.format(currentCalendar.time))
            startActivity(intent)
        }
    }

    override fun onResume() { super.onResume(); updateScreenByDate() }

    private fun dpToPx(dp: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()

    private fun updateScreenByDate() {
        val uiFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        tvDate.text = uiFormat.format(currentCalendar.time)
        val dayFormat = SimpleDateFormat("EEEE", Locale.KOREA)
        tvDay.text = dayFormat.format(currentCalendar.time)
        loadAndDisplayData(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentCalendar.time))
    }

    private fun loadAndDisplayData(searchDate: String) {
        containerImportant.removeAllViews()
        containerGeneral.removeAllViews()
        try {
            sqlDB = dbManager.readableDatabase

            val query = "SELECT * FROM todoTBL WHERE date_time LIKE '$searchDate%' ORDER BY is_important DESC"
            val cursor = sqlDB.rawQuery(query, null)

            if (cursor.count == 0) {
                showEmptyMessage()
            } else {
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("todo_id"))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow("todo_name"))
                    val important = cursor.getInt(cursor.getColumnIndexOrThrow("is_important"))
                    val done = cursor.getInt(cursor.getColumnIndexOrThrow("is_done"))
                    val timeText = formatToAmPm(cursor.getString(cursor.getColumnIndexOrThrow("date_time")))

                    // 💡 [핵심] 해당 데이터에 연결된 핑계가 있는지 확인
                    val excuseCursor = sqlDB.rawQuery("SELECT * FROM excuseTBL WHERE todo_id = $id", null)
                    val hasExcuse = excuseCursor.count > 0
                    excuseCursor.close()

                    val itemView = createTodoItemView(id, name, important, done, timeText, hasExcuse)
                    if (important == 1) containerImportant.addView(itemView) else containerGeneral.addView(itemView)
                }
            }
            cursor.close()
            sqlDB.close()
        } catch (e: Exception) { Log.e("SQL_ERROR", "조회 실패: ${e.message}") }
    }

    private fun showEmptyMessage() {
        val tvEmpty = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                .apply { gravity = Gravity.CENTER_HORIZONTAL; topMargin = dpToPx(5f) }
            text = "해야할 일이 없습니다."; textSize = 14f; setTextColor(Color.parseColor("#D7D7D7")); typeface = paperFont; gravity = Gravity.CENTER
        }
        containerGeneral.addView(tvEmpty)
    }

    private fun formatToAmPm(fullDateTime: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(fullDateTime)
            SimpleDateFormat("a h:mm", Locale.KOREA).format(date!!)
        } catch (e: Exception) { fullDateTime }
    }

    // 💡 hasExcuse 매개변수 추가
    private fun createTodoItemView(id: Int, name: String, important: Int, done: Int, time: String, hasExcuse: Boolean): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(dpToPx(311f), dpToPx(61f)).apply {
                gravity = Gravity.CENTER_HORIZONTAL; setMargins(0, dpToPx(3f), 0, dpToPx(3f))
            }
            setPadding(dpToPx(20f), 0, dpToPx(20f), 0)
            setBackgroundResource(if (important == 1) R.drawable.cr8bebedff99 else R.drawable.cr8bd7d7d74d)

            setOnClickListener {
                val intent = Intent(this@MainActivity, AddTodoActivity::class.java)
                intent.putExtra("TODO_ID", id); startActivity(intent)
            }

            // 체크박스 (동일)
            val checkbox = LinearLayout(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(24f), dpToPx(24f)).apply { marginEnd = dpToPx(12f) }
                gravity = Gravity.CENTER
                setOnClickListener {
                    val newDone = if (done == 1) 0 else 1
                    sqlDB = dbManager.writableDatabase
                    sqlDB.execSQL("UPDATE todoTBL SET is_done = $newDone WHERE todo_id = $id")
                    sqlDB.close(); updateScreenByDate()
                }
                if (done == 1) {
                    setBackgroundResource(if (important == 1) R.drawable.s7280ffsw2cr5b7280ff else R.drawable.s3a3a3asw2cr5b3a3a3a)
                    addView(ImageView(this@MainActivity).apply { layoutParams = LinearLayout.LayoutParams(dpToPx(14f), dpToPx(10f)); setImageResource(R.drawable.vector9); scaleType = ImageView.ScaleType.FIT_XY })
                } else { setBackgroundResource(if (important == 1) R.drawable.s7280ffsw2cr5 else R.drawable.s3a3a3asw2cr5) }
            }
            addView(checkbox)

            // 텍스트 영역 (동일)
            val textLayout = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
            textLayout.addView(TextView(this@MainActivity).apply { text = time; textSize = 10f; typeface = paperFont; setTextColor(Color.parseColor("#3A3A3A")) })
            textLayout.addView(TextView(this@MainActivity).apply { text = name; textSize = 15f; typeface = paperFont; setTextColor(Color.parseColor("#3A3A3A")); maxLines = 1 })
            addView(textLayout)

            // 💡 [수정] 핑계 존재 여부에 따라 이모지 투명도 조절
            addView(ImageView(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(24f), dpToPx(24f))
                setImageResource(if (important == 1) R.drawable.vector7 else R.drawable.vector8)

                // 💡 핑계가 있으면 진하게(1.0), 없으면 연하게(0.3) 표시
                alpha = if (hasExcuse) 1.0f else 0.3f

                setOnClickListener {
                    val intent = Intent(this@MainActivity, AddExcuseActivity::class.java)
                    intent.putExtra("TODO_ID", id); startActivity(intent)
                }
            })
        }
    }
}