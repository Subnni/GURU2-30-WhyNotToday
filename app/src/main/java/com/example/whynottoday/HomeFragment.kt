package com.example.whynottoday

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(), OnDateChangeListener{

    private lateinit var selectedDateTextView : TextView

    lateinit var dateButton: TextView
    lateinit var datButton: TextView
    lateinit var addButton: ImageButton
    lateinit var containerImportant: LinearLayout
    lateinit var containerGeneral: LinearLayout
    lateinit var prevButton: ImageView
    lateinit var nextButton: ImageView

    lateinit var dbManager: DBManager
    lateinit var sqlDB: SQLiteDatabase
    //    private var paperFont: Typeface? = null
    private var currentCalendar = Calendar.getInstance()


    //레이아웃 생성
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    //로직 설정
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        paperFont = ResourcesCompat.getFont(this, R.font.paperlogy_medium)

        selectedDateTextView = view.findViewById<TextView>(R.id.selectedDateTextView)

        dateButton = view.findViewById(R.id.dateTextView)
        datButton = view.findViewById(R.id.dayTextView)
        addButton = view.findViewById(R.id.todoAddImageButton)
        containerImportant = view.findViewById(R.id.containerImportant)
        containerGeneral = view.findViewById(R.id.containerGeneral)
        prevButton = view.findViewById(R.id.prevButton)
        nextButton = view.findViewById(R.id.nextButton)

        //캘린더 설정
        val manager = WeekCalendarManager(requireContext(), view, "blue", this)
        manager.initCalendar()

        onDateChange() //달력 세팅 시 오늘 날짜 선택되도록 함

        // 섹션 타이틀 포함 폰트 적용
//        val staticTexts = listOf(tvDate, tvDay,
//            findViewById<TextView>(R.id.rn7lao1l6yv8), findViewById<TextView>(R.id.rapw2kdupbc8))
//        staticTexts.forEach { it.typeface = paperFont }

        dbManager = DBManager(requireContext(), "WhyNotTodayDB.db", null, 1)
        updateScreenByDate()

        prevButton.setOnClickListener { currentCalendar.add(Calendar.DAY_OF_YEAR, -1); updateScreenByDate() }
        nextButton.setOnClickListener { currentCalendar.add(Calendar.DAY_OF_YEAR, 1); updateScreenByDate() }
        addButton.setOnClickListener {
            val intent = Intent(requireContext(), AddTodoActivity::class.java)
            val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            intent.putExtra("selectedDate", dbFormat.format(currentCalendar.time))
            startActivity(intent)
        }



    }

    //새로고침 시마다 호출(날짜 데이터 최신화)
    override fun onResume() { super.onResume(); updateScreenByDate(); onDateChange() }

    override fun onDateChange() {
        setSelectedDateAdapter() //선택한 날짜, 성취 비율 세팅
        //setHomeLayoutAdapter()
        updateScreenByDate()
    }
    private fun dpToPx(dp: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()


    //액티비티 onCreate시, 특정 날짜 클릭 시마다 호출되어 페이지 구성

    private fun setSelectedDateAdapter(){
        //날짜
        val selectedDate = CalendarUtils.selectedDate
        val format = DateTimeFormatter.ofPattern("MM. dd (E)", Locale.KOREAN)
        selectedDateTextView.text = selectedDate.format(format)
    }

    private fun updateScreenByDate() {
        val selectedDate = CalendarUtils.selectedDate //선택 날짜

        //포맷터
        val uiFormat = java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.getDefault())
        val dayFormat = java.time.format.DateTimeFormatter.ofPattern("EEEE", Locale.KOREA)
        val dbFormat = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

        dateButton.text = selectedDate.format(uiFormat)
        datButton.text = selectedDate.format(dayFormat)

        //오늘 날짜의 색상 변경
        val today = java.time.LocalDate.now()
        if (selectedDate.equals(today)) {
            dateButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_100))
            datButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_100))
        } else {
            dateButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            datButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
        }

        loadAndDisplayData(selectedDate.format(dbFormat))
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
        val tvEmpty = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                .apply { gravity = Gravity.CENTER_HORIZONTAL; topMargin = dpToPx(5f) }
            text = "해야할 일이 없습니다."; textSize = 14f; setTextColor(Color.parseColor("#D7D7D7")); gravity = Gravity.CENTER
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
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(dpToPx(311f), dpToPx(61f)).apply {
                gravity = Gravity.CENTER_HORIZONTAL; setMargins(0, dpToPx(3f), 0, dpToPx(3f))
            }
            setPadding(dpToPx(20f), 0, dpToPx(20f), 0)
            setBackgroundResource(if (important == 1) R.drawable.cr8bebedff99 else R.drawable.cr8bd7d7d74d)

            setOnClickListener {
                val intent = Intent(requireContext(), AddTodoActivity::class.java)
                intent.putExtra("TODO_ID", id); startActivity(intent)
            }

            // 체크박스 (동일)
            val checkbox = LinearLayout(requireContext()).apply {
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
                    addView(ImageView(requireContext()).apply { layoutParams = LinearLayout.LayoutParams(dpToPx(14f), dpToPx(10f)); setImageResource(R.drawable.check_image); scaleType = ImageView.ScaleType.FIT_XY })
                } else { setBackgroundResource(if (important == 1) R.drawable.s7280ffsw2cr5 else R.drawable.s3a3a3asw2cr5) }
            }
            addView(checkbox)

            // 텍스트 영역 (동일)
            val textLayout = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }
            textLayout.addView(TextView(requireContext()).apply { text = time; textSize = 10f;  setTextColor(ContextCompat.getColor(requireContext(), R.color.lightgray)) })
            textLayout.addView(TextView(requireContext()).apply { text = name; textSize = 15f;  setTextColor(ContextCompat.getColor(requireContext(), R.color.gray)); maxLines = 1 })
            addView(textLayout)

            if(done != 1){
                // 💡 [수정] 핑계 존재 여부에 따라 이모지 투명도 조절
                addView(ImageView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(dpToPx(24f), dpToPx(24f))
                    setImageResource(if (important == 1) R.drawable.important_add_excuse_image else R.drawable.general_add_excuse_image)

                    // 💡 핑계가 있으면 진하게(1.0), 없으면 연하게(0.3) 표시
                    alpha = if (hasExcuse) 1.0f else 0.3f

                    setOnClickListener {
                        val intent = Intent(requireContext(), AddExcuseActivity::class.java)
                        intent.putExtra("TODO_ID", id); startActivity(intent)
                    }
                })
            }

        }
    }

}